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

import java.util.List;
import client.MapleClient;
import client.autoban.AutobanManager;
import server.movement.LifeMovementFragment;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class MovePlayerHandler extends AbstractMovementPacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        
        slea.skip(33);
        
        final List<LifeMovementFragment> res = parseMovement(slea);
        
        AutobanManager abm = c.getPlayer().getAutobanManager();
        
        if (res != null) {
            updatePosition(res, c.getPlayer(), 0);
            abm.updatePosition(c.getPlayer().getPosition());
            
            if( abm.getFastMoveCount() > 5 ) {
                c.disconnect(false, false);
                return;
            }
            c.getPlayer().getMap().movePlayer(c.getPlayer(), c.getPlayer().getPosition());
            if (c.getPlayer().isHidden()) {
                c.getPlayer().getMap().broadcastGMMessage(c.getPlayer(), MaplePacketCreator.movePlayer(c.getPlayer(), res), false);
            } else {
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.movePlayer(c.getPlayer(), res), false);
            }
        }
    }
}
