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
import static javax.swing.text.StyleConstants.Family;
import net.AbstractMaplePacketHandler;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.FamilyPacket;

/**
 *
 * @author Jay Estrella
 */
public final class AcceptFamilyHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        //System.out.println(slea.toString());
        int inviterId = slea.readInt();
//        String inviterName = slea.readMapleAsciiString();
        MapleCharacter inviter = c.getWorldServer().getPlayerStorage().getCharacterById(inviterId);
        if (inviter != null) {
            inviter.getClient().announce(FamilyPacket.sendFamilyJoinResponse(true, c.getPlayer().getName()));
        }
        c.announce(FamilyPacket.sendFamilyMessage(0, 0));
    }
}
