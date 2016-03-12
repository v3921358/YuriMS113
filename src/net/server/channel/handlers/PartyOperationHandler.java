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

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PartyOperationHandler extends AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int operation = slea.readByte();
        MapleCharacter player = c.getPlayer();
        World world = c.getWorldServer();
        MapleParty party = player.getParty();
        MaplePartyCharacter partyplayer = player.getMPC();

        switch (operation) {
            case 1: { // create
                if (player.getParty() == null) {
                    partyplayer = new MaplePartyCharacter(player);
                    party = world.createParty(partyplayer);
                    player.setParty(party);
                    player.setMPC(partyplayer);
                    c.announce(MaplePacketCreator.partyCreated(player.getPartyId()));
                } else {
                    c.announce(MaplePacketCreator.broadcastMsg(5, "你已經有組隊了。"));
                }
                break;
            }
            case 2: {
                if (party != null && partyplayer != null) {
                    if (partyplayer.equals(party.getLeader())) {
                        world.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                        if (player.getEventInstance() != null) {
                            player.getEventInstance().disbandParty();
                        }
                    } else {
                        world.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                        if (player.getEventInstance() != null) {
                            player.getEventInstance().leftParty(player);
                        }
                    }
                    player.setParty(null);
                }
                break;
            }
            case 3: {//join
                int partyid = slea.readInt();
                if (c.getPlayer().getParty() == null) {
                    party = world.getParty(partyid);
                    if (party != null) {
                        if (party.getMembers().size() < 6) {
                            partyplayer = new MaplePartyCharacter(player);
                            world.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                            player.receivePartyMemberHP();
                            player.updatePartyMemberHP();
                        } else {
                            c.announce(MaplePacketCreator.partyStatusMessage(17));
                        }
                    } else {
                        c.announce(MaplePacketCreator.broadcastMsg(5, "你邀請的人已經有一個組對了。"));
                    }
                } else {
                    c.announce(MaplePacketCreator.broadcastMsg(5, "你不能加入組隊，因為你已經有組隊了。"));
                }
                break;
            }
            case 4: {//invite
                String name = slea.readMapleAsciiString();

                MapleCharacter invited = world.getPlayerStorage().getCharacterByName(name);

                if (invited != null) {
                    if (invited.getParty() == null) {
                        if (invited.isGM()) {
                            c.announce(MaplePacketCreator.partyStatusMessage(32));
                        } else if (party.getMembers().size() < 6) {
                            invited.getClient().announce(MaplePacketCreator.partyInvite(player));
                        } else {
                            c.announce(MaplePacketCreator.partyStatusMessage(17));
                        }
                    } else {
                        c.announce(MaplePacketCreator.partyStatusMessage(16));
                    }
                } else {
                    c.announce(MaplePacketCreator.partyStatusMessage(19));
                }
                break;
            }
            case 5: { // expel
                int cid = slea.readInt();
                if (partyplayer.equals(party.getLeader())) {
                    MaplePartyCharacter expelled = party.getMemberById(cid);
                    if (expelled != null) {
                        world.updateParty(party.getId(), PartyOperation.EXPEL, expelled);
                        if (player.getEventInstance() != null) {
                            if (expelled.isOnline()) {
                                player.getEventInstance().disbandParty();
                            }
                        }
                    }
                }
                break;
            }

            case 6: {
                int newLeader = slea.readInt();
                if (party != null) {
                    MaplePartyCharacter newLeadr = party.getMemberById(newLeader);
                    party.setLeader(newLeadr);
                    world.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newLeadr);
                }
                break;
            }
        }
    }
}
