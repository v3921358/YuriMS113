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
import net.server.channel.handlers.DueyHandler;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.maps.PlayerNPCs;
import tools.packets.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class NPCTalkHandler extends AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        MapleCharacter chr = c.getPlayer();

        if (chr != null && !chr.isAlive()) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }

        int oid = slea.readInt();
        MapleMapObject obj = chr.getMap().getMapObject(oid);

        if (obj instanceof MapleNPC) {

            MapleNPC npc = (MapleNPC) obj;

            if (chr.getConversation() != 0) {
                chr.dropMessage(-1, "你現在不能攻擊或不能跟npc對話,請在對話框打 @解卡/@ea 來解除異常狀態");
                return;
            }

            if (npc.getId() == 9010009) {
                c.announce(MaplePacketCreator.sendDuey((byte) 8, DueyHandler.loadItems(chr)));

            } else if (npc.hasShop()) {

                if (chr.getShop() != null) {
                    return;
                }

                if (npc.hasShop()) {
                    chr.setConversation(1);
                    npc.sendShop(c);
                }

            } else {

                if (c.getCM() != null || c.getQM() != null) {
                    c.announce(MaplePacketCreator.enableActions());
                    return;
                }

                NPCScriptManager.getInstance().start(c, npc.getId(), null, null);
            }
        } else if (obj instanceof PlayerNPCs) {
            NPCScriptManager.getInstance().start(c, ((PlayerNPCs) obj).getId(), null, null);
        }
    }
}
