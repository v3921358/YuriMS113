/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.server.channel.handlers;

import client.BuddylistEntry;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamily;
import client.SkillFactory;
import client.inventory.MapleInventoryType;
import constants.skills.GM;
import gm.server.GMServer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import net.AbstractMaplePacketHandler;
import net.server.PlayerBuffValueHolder;
import net.server.Server;
import net.server.channel.Channel;
import net.server.channel.CharacterIdChannelPair;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;
import tools.DatabaseConnection;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.AlliancePacket;
import tools.packets.BuddyPacket;
import tools.packets.FamilyPacket;
import tools.packets.GuildPacket;
import tools.packets.LoginPacket;

public final class PlayerLoggedinHandler extends AbstractMaplePacketHandler {

    @Override
    public final boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        final int cid = slea.readInt();

        final Server server = Server.getInstance();

        MapleCharacter player = c.getWorldServer().getPlayerStorage().getCharacterById(cid);

        if (player == null) {
            try {
                player = MapleCharacter.loadCharFromDB(cid, c, true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            player.newClient(c);
        }

        c.setPlayer(player);
        c.setAccID(player.getAccountID());
        c.loadAccountData(player.getAccountID());

        int state = c.getLoginState();

        boolean allowLogin = true;
        Channel cserv = c.getChannelServer();

        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.LOGIN_NOTLOGGEDIN) {
            for (String charName : c.loadCharacterNames(c.getWorld())) {
                for (Channel ch : c.getWorldServer().getChannels()) {
                    if (ch.isConnected(charName)) {
                        allowLogin = true;
                    }
                }
                break;
            }
        }

        if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
            c.setPlayer(null);
            c.getSession().close(true);
            return;
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);

        cserv.addPlayer(player);
        List<PlayerBuffValueHolder> buffs = server.getPlayerBuffStorage().getBuffsFromStorage(cid);
        if (buffs != null) {
            player.silentGiveBuffs(buffs);
        }

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        PreparedStatement pss = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT Mesos FROM dueypackages WHERE RecieverId = ? and Checked = 1");
            ps.setInt(1, player.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    pss = DatabaseConnection.getConnection().prepareStatement("UPDATE dueypackages SET Checked = 0 where RecieverId = ?");
                    pss.setInt(1, player.getId());
                    pss.executeUpdate();
                    pss.close();
                } catch (SQLException e) {
                }
                c.announce(MaplePacketCreator.sendDueyMSG((byte) 0x1B));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pss != null) {
                    pss.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                //ignore
            }
        }

        c.announce(MaplePacketCreator.getCharInfo(player));
        if (!player.isHidden()) {
            player.toggleHide(true);
        }

        player.sendKeymap();
        player.sendMacros();
        player.getMap().addPlayer(player);
        World world = server.getWorld(c.getWorld());
        world.getPlayerStorage().addPlayer(player);

        int buddyIds[] = player.getBuddylist().getBuddyIds();
        world.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
        for (CharacterIdChannelPair onlineBuddy : server.getWorld(c.getWorld()).multiBuddyFind(player.getId(), buddyIds)) {
            BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
            ble.setChannel(onlineBuddy.getChannel());
            player.getBuddylist().put(ble);
        }
        c.announce(BuddyPacket.updateBuddylist(c, player.getBuddylist().getBuddies()));
        c.announce(FamilyPacket.loadFamily(player));
        if (player.getFamilyId() > 0) {
            MapleFamily f = world.getFamily(player.getFamilyId());
            if (f == null) {
                f = new MapleFamily(player.getId());
                world.addFamily(player.getFamilyId(), f);
            }
            player.setFamily(f);
            c.announce(FamilyPacket.getFamilyInfo(f.getMember(player.getId())));
        }
        if (player.getGuildId() > 0) {
            MapleGuild playerGuild = server.getGuild(player.getGuildId(), player.getMGC());
            if (playerGuild == null) {
                player.deleteGuild(player.getGuildId());
                player.resetMGC();
                player.setGuildId(0);
            } else {
                server.setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.announce(GuildPacket.showGuildInfo(player));
                int allianceId = player.getGuild().getAllianceId();
                if (allianceId > 0) {
                    MapleAlliance newAlliance = server.getAlliance(allianceId);
                    if (newAlliance == null) {
                        newAlliance = MapleAlliance.loadAlliance(allianceId);
                        if (newAlliance != null) {
                            server.addAlliance(allianceId, newAlliance);
                        } else {
                            player.getGuild().setAllianceId(0);
                        }
                    }
                    if (newAlliance != null) {
                        c.announce(AlliancePacket.getAllianceInfo(newAlliance));
                        c.announce(AlliancePacket.getGuildAlliances(newAlliance, c));
                        server.allianceMessage(allianceId, AlliancePacket.allianceMemberOnline(player, true), player.getId(), -1);
                    }
                }
            }
        }
        player.showNote();
        if (player.getParty() != null) {
            MaplePartyCharacter pchar = player.getMPC();
            pchar.setChannel(c.getChannel());
            pchar.setMapId(player.getMapId());
            pchar.setOnline(true);
            world.updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, pchar);
        }
        player.updatePartyMemberHP();

        if (player.getInventory(MapleInventoryType.EQUIPPED).findById(1122017) != null) {
            player.equipPendantOfSpirit();
        }
        if (player.isGM()) {
            player.Hide(true);
            GMServer.addInGame(player.getName(), c.getSession());
        }
        player.checkMessenger();
        player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) (player.getLinkedLevel() / 10), 20, -1);
        player.checkBerserk();
        player.expirationTask();
        player.setRates();
    }
}