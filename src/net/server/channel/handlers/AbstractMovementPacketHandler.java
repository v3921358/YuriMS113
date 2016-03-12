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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import net.AbstractMaplePacketHandler;
import server.maps.AnimatedMapleMapObject;
import server.maps.MapleMapObjectType;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import server.movement.StaticLifeMovement;
import tools.data.input.LittleEndianAccessor;

public abstract class AbstractMovementPacketHandler extends AbstractMaplePacketHandler {

    protected List<LifeMovementFragment> parseMovement(LittleEndianAccessor lea) {
        List<LifeMovementFragment> res = new ArrayList<>();
        byte numCommands = lea.readByte();
        for (byte i = 0; i < numCommands; i++) {
            byte command = lea.readByte();
            switch (command) {

                case 0: // normal move
                case 5:
                case 15:
                case 17: { // Float
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final short unk = lea.readShort();
                    short fh = 0;
                    if (command == 15 || command == 16) {
                        fh = lea.readShort();
                    }
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    StaticLifeMovement mov = new StaticLifeMovement(command, new Point(xpos, ypos), duration, newstate, unk);
                    mov.setUnk(unk);
                    mov.setFh(fh);
                    mov.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(mov);
                    break;
                }
                case 1:
                case 2:
                case 6:
                case 12:
                case 13:
                case 16:
                case 18:
                case 19:
                case 20:
                case 22: {
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    StaticLifeMovement mov = new StaticLifeMovement(command, null, duration, newstate, 0);
                    mov.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(mov);
                    break;
                }

                case 3:
                case 4:
                case 7:
                case 8:
                case 9:
                case 11: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short unk = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();
                    StaticLifeMovement mov = new StaticLifeMovement(command, new Point(xpos, ypos), 0, newstate, 0);
                    mov.setUnk(unk);
                    res.add(mov);
                    break;
                }
                case 10: // Change Equip
                {
                    final byte newstate = 0;
                    final short duration = 0;
                    final int wui = lea.readByte();
                    final StaticLifeMovement mov = new StaticLifeMovement(command, null, duration, newstate, 0);
                    mov.setWui(wui);
                    res.add(mov);
                    break;
                }
                case 14: {

                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    int fh = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    StaticLifeMovement mov = new StaticLifeMovement(command, null, duration, newstate, 0);
                    mov.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(mov);
                    break;
                }
                default:
                    System.out.println(" movement:  Remaining : " + (numCommands - res.size()) + " New type of movement ID : " + command + ", packet : " + lea.toString(true));
                    return null;
            }
        }
        return res;
    }

    protected void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
        for (final LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof StaticLifeMovement) {
                    final Point position = ((LifeMovement) move).getPosition();
                    if (position != null) {
                        position.y += yoffset;
                        target.setPosition(position);
                    }
                }
                target.setStance(((LifeMovement) move).getNewstate());
                target.setNewFH(((LifeMovement) move).getNewFh());
            }
        }
    }
}
