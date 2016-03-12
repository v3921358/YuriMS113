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
import server.MapleItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class NPCShopHandler extends AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        MapleCharacter chr = c.getPlayer();
        /*
         36 00 
         00 
         00 00 
         81 84 1E 00 
         32 00 
         96 00 00 00
        
         00 00 00 81 84 1E 00 01 00 96 00 00 00
         */

        byte bmode = slea.readByte();
        if (chr == null) {
            return;
        }

        switch (bmode) {
            case 0: {
                short slot = slea.readShort();// slot
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                chr.getShop().buy(c, slot, itemId, quantity);
                break;
            }
            case 1: {
                short slot = slea.readShort();
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                chr.getShop().sell(c, MapleItemInformationProvider.getInstance().getInventoryType(itemId), slot, quantity);
                break;
            }
            case 2: {
                byte slot = (byte) slea.readShort();
                chr.getShop().recharge(c, slot);
                break;
            }
            case 3: {
                chr.setShop(null);
                chr.setConversation(0);
                break;
            }
            default: {
                chr.setConversation(0);
                break;
            }
            
        }

    }
}
