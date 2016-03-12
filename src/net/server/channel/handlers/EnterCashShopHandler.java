/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

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
import net.server.Server;
import tools.FilePrinter;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.MTSCSPacket;;

/**
 *
 * @author Flav
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        /*
         20 00 
         12 46 CC 04 00
         */
        try {

            if (!Server.getCashShopEnabled() && !c.getPlayer().isGM()) {
                c.announce(MaplePacketCreator.enableActions());
                c.announce(MaplePacketCreator.blockedMessage2(2));
                return;
            }
            MapleCharacter mc = c.getPlayer();

            if (mc.getCashShop().isOpened()) {
                return;
            }

            Server.getInstance().getPlayerBuffStorage().addBuffsToStorage(mc.getId(), mc.getAllBuffs());
            mc.cancelBuffEffects();
            mc.cancelExpirationTask();
            c.announce(MTSCSPacket.openCashShop(c, false));
            c.announce(MTSCSPacket.showCashShopAcc(c));
            mc.saveToDB();
            mc.getCashShop().open(true);
            mc.getMap().removePlayer(mc);
            c.announce(MTSCSPacket.showGifts(mc.getCashShop().loadGifts()));
            c.announce(MTSCSPacket.showCashInventory(c));
            c.announce(MTSCSPacket.showCash(mc));
            c.announce(MTSCSPacket.enableCSUse());
            c.announce(MTSCSPacket.showWishList(mc));
        } catch (Exception e) {
            FilePrinter.printError("EnterCashShop.txt", e);
        }
    }
}
