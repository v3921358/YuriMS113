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
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;
import client.command.CommandProcessor;
import client.command.CommandExecute;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GeneralchatHandler extends net.AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        String text = slea.readMapleAsciiString();
        try {
            try {
                if (!CommandProcessor.processCommand(c, text)) {
                    if (c.getPlayer().gmLevel() == 5 && !chr.isHidden()) {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.sendYellowTip("<超級管理員> " + c.getPlayer().getName() + ": " + text));
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, false, 1));
                    } else if (c.getPlayer().gmLevel() == 4 && !chr.isHidden()){
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.sendYellowTip("<領導者>" + c.getPlayer().getName() + ": " + text));
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, false, 1));
                    } else if (c.getPlayer().gmLevel() == 3 && !chr.isHidden()){
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.sendYellowTip("<巡邏者>" + c.getPlayer().getName() + ": " + text));
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, false, 1));
                    } else if (c.getPlayer().gmLevel() == 2 && !chr.isHidden()){
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.sendYellowTip("<老實習生>" + c.getPlayer().getName() + ": " + text));
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, false, 1));
                    } else if (c.getPlayer().gmLevel() == 1 && !chr.isHidden()){
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.sendYellowTip("<新實習生>" + c.getPlayer().getName() + ": " + text));
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, false, 1));
                    } else {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, c.getPlayer().getGMChat(), slea.readByte()));
                    }
                }
            } catch (RemoteException ex) {
                Logger.getLogger(GeneralchatHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CommandExecute ex) {
                Logger.getLogger(GeneralchatHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(GeneralchatHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
    }
