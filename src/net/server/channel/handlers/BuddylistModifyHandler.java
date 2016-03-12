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

import client.BuddyList;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import static client.BuddyList.BuddyOperation.ADDED;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.AbstractMaplePacketHandler;
import net.server.channel.Channel;
import net.server.world.World;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.BuddyPacket;

public class BuddylistModifyHandler extends AbstractMaplePacketHandler {

    private final int BUDDY_ADD = 1,
            BUDDY_ACCPET = 2,
            BUDDY_DELETE = 3;

    private static class CharacterIdNameBuddyCapacity extends CharacterNameAndId {

        private final int buddyCapacity;

        public CharacterIdNameBuddyCapacity(int id, String name, int buddyCapacity) {
            super(id, name);
            this.buddyCapacity = buddyCapacity;
        }

        public int getBuddyCapacity() {
            return buddyCapacity;
        }
    }

    private void nextPendingRequest(MapleClient c) {
        CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            c.announce(BuddyPacket.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
        }
    }

    private CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(String name) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        CharacterIdNameBuddyCapacity ret;
        try (PreparedStatement ps = con.prepareStatement("SELECT id, name, buddyCapacity FROM characters WHERE name LIKE ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                ret = null;
                if (rs.next()) {
                    ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), rs.getInt("buddyCapacity"));
                }
            }
        }
        return ret;
    }

    private int getBuddyCount(int chrId, int pending) {
        int count = 0;

        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = ?")) {
            ps.setInt(1, chrId);
            ps.setInt(2, pending);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("BuddyListModifyHandler: getBuudyCount From DB is Error.");
            } else {
                count = rs.getInt("buddyCount");
            }
            rs.close();

        } catch (SQLException ex) {
            FilePrinter.printError("BuddyListModifyHandler.txt", ex);
        }
        return count;
    }

    private int getBuddyPending(int chrId, int buddyId) {
        int pending = -1;
        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?")) {
            ps.setInt(1, chrId);
            ps.setInt(2, buddyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pending = rs.getInt("pending");
                }
            }
        } catch (SQLException ex) {
            FilePrinter.printError("BuddyListModifyHandler.txt", ex);
        }

        return pending;
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        int mode = slea.readByte();

        MapleCharacter chr = c.getPlayer();
        BuddyList chrBuddylist = chr.getBuddylist();

        switch (mode) {

            case BUDDY_ADD: {

                String playerAddName = slea.readMapleAsciiString();
                String playerAddGroup = slea.readMapleAsciiString();

                if (playerAddGroup.length() > 16 || playerAddName.length() < 3 || playerAddName.length() > 15) {
                    return; //hax.
                }

                BuddylistEntry buddyEntry = chrBuddylist.get(playerAddName);

                if (buddyEntry != null && buddyEntry.isVisible() == false && playerAddGroup.equals(buddyEntry.getGroup())) {
                    c.announce(MaplePacketCreator.broadcastMsg(1, "玩家 \"" + buddyEntry.getName() + "\" 已是你的好友"));
                } else if (chrBuddylist.isFull()) {
                    c.announce(MaplePacketCreator.broadcastMsg(1, "好友名單已滿"));
                } else if (buddyEntry == null) {

                    World worldServ = c.getWorldServer();
                    Channel channelServ = c.getChannelServer();

                    CharacterIdNameBuddyCapacity chrBuddyCap = null;

                    MapleCharacter playerAdd = channelServ.getPlayerStorage().getCharacterByName(playerAddName);
                    int channel = -1;

                    if (playerAdd != null) {
                        channel = c.getChannel();
                        chrBuddyCap = new CharacterIdNameBuddyCapacity(playerAdd.getId(),
                                playerAdd.getName(),
                                playerAdd.getBuddylist().getCapacity());
                    } else {
                        try {
                            channel = worldServ.find(playerAddName);
                            chrBuddyCap = getCharacterIdAndNameFromDatabase(playerAddName);
                        } catch (SQLException ex) {
                            Logger.getLogger(BuddylistModifyHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (chrBuddyCap != null) {
                        
                        BuddyAddResult buddyAddResult = null;
                        if (channel != -1) {
                            buddyAddResult = worldServ.requestBuddyAdd(playerAddName, c.getChannel(), chr.getId(), chr.getName());
                        } else {

                            int buddyCount = getBuddyCount(chrBuddyCap.getId(), 0);

                            if (buddyCount == -1 || buddyCount >= chrBuddyCap.getBuddyCapacity()) {
                                buddyAddResult = BuddyAddResult.BUDDYLIST_FULL;
                            }

                            int pending = getBuddyPending(chrBuddyCap.getId(), playerAdd.getId());

                            if (pending == 0) {
                                buddyAddResult = BuddyAddResult.ALREADY_ON_LIST;
                            }
                        }
                        if (buddyAddResult == BuddyAddResult.BUDDYLIST_FULL) {
                            c.announce(MaplePacketCreator.broadcastMsg(1, "\"" + playerAddName + "\"的好友已滿"));
                        } else {
                            int displayChannel;
                            displayChannel = -1;
                            if (buddyAddResult == BuddyAddResult.ALREADY_ON_LIST && channel != -1) {
                                displayChannel = channel;
                                notifyRemoteChannel(c, channel, chrBuddyCap.getId(), ADDED);
                            } else if (buddyAddResult != BuddyAddResult.ALREADY_ON_LIST && channel == -1) {
                                try {
                                    chrBuddylist.addBuddy(chrBuddyCap.getId(), chr.getId(), 1);
                                } catch (SQLException ex) {
                                    Logger.getLogger(BuddylistModifyHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            chrBuddylist.put(new BuddylistEntry(chrBuddyCap.getName(), playerAddGroup, chrBuddyCap.getId(), displayChannel, true));
                            c.announce(BuddyPacket.updateBuddylist(c, chrBuddylist.getBuddies()));

                        }
                    } else {
                        c.announce(MaplePacketCreator.broadcastMsg(1, "玩家 \"" + playerAddName + "\" 不存在"));
                    }
                } else {
                    buddyEntry.changeGroup(playerAddGroup);
                    c.announce(BuddyPacket.updateBuddylist(c, chrBuddylist.getBuddies()));
                }
                break;
            }
            case BUDDY_ACCPET: {

                int playerAccpetedId = slea.readInt();

                if (!chrBuddylist.isFull()) {

                    int playerAccpetedChannel = c.getWorldServer().find(playerAccpetedId);
                    String playerAccpetedName = null;
                    MapleCharacter playerAccpeted = c.getChannelServer().getPlayerStorage().getCharacterById(playerAccpetedId);

                    if (playerAccpeted == null) {
                        playerAccpetedName = MapleCharacter.findNameFromDB(playerAccpetedId);
                    } else {
                        playerAccpetedName = playerAccpeted.getName();
                    }

                    if (playerAccpetedName != null) {
                        chrBuddylist.put(new BuddylistEntry(playerAccpetedName, "預設", playerAccpetedId, playerAccpetedChannel, true));
                        c.announce(BuddyPacket.updateBuddylist(c, chrBuddylist.getBuddies()));
                        notifyRemoteChannel(c, playerAccpetedChannel, playerAccpetedId, ADDED);
                    }
                }
                nextPendingRequest(c);
                break;
            }

            case BUDDY_DELETE: {
                int playerDeleteId = slea.readInt();
                if (chrBuddylist.containsVisible(playerDeleteId)) {
                    notifyRemoteChannel(c, c.getWorldServer().find(playerDeleteId), playerDeleteId, BuddyOperation.DELETED);
                }
                chrBuddylist.remove(playerDeleteId);
                c.announce(BuddyPacket.updateBuddylist(c, chrBuddylist.getBuddies()));
                nextPendingRequest(c);
                break;
            }
        }
    }

    private void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, BuddyOperation operation) {
        MapleCharacter player = c.getPlayer();
        if (remoteChannel != -1) {
            c.getWorldServer().buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation);
        }
    }
}
