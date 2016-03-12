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
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import client.autoban.AutobanManager.AutoBanTimestampType;

public final class HealOvertimeHandler extends AbstractMaplePacketHandler {

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        /*
         00 14 00 00 
         0A 00 
         00 00 
         00
         */
        MapleCharacter chr = c.getPlayer();
        AutobanManager abm = chr.getAutobanManager();

        long cs_timestamp = slea.readInt();
        long ss_timestamp = System.currentTimeMillis();

        short healHP = slea.readShort();

        if (healHP != 0) {
            //long cs_offset_time = cs_timestamp - (abm.getTimestamp(AutoBanTimestampType.CS_HEAL_HP) == 0 ? cs_timestamp - 2000 : abm.getTimestamp(AutoBanTimestampType.CS_HEAL_HP));
            long ss_offset_time = ss_timestamp - abm.getTimestamp(AutoBanTimestampType.SS_HEAL_HP);

            if (ss_offset_time < 1500) {
                AutobanFactory.FAST_HP_HEALING.addPoint(abm, "HP回復過快,時間-SS:" + String.valueOf(ss_offset_time));
            }
            if (healHP > 140) {
                AutobanFactory.HIGH_HP_HEALING.autoban(chr, "回復HP: " + healHP + "; 最高為 140.");
                return;
            }

            if (ss_offset_time >= 4000) {
                chr.addHP(healHP);
            }

            chr.checkBerserk();
            //abm.updateTimestamp(AutoBanTimestampType.CS_HEAL_HP, cs_timestamp);
            abm.updateTimestamp(AutoBanTimestampType.SS_HEAL_HP, ss_timestamp);
        }

        short healMP = slea.readShort();

        if (healMP != 0 && healMP < 5000) {
            //long cs_offset_time = cs_timestamp - (abm.getTimestamp(AutoBanTimestampType.CS_HEAL_MP) == 0 ? cs_timestamp - 3000 : abm.getTimestamp(AutoBanTimestampType.CS_HEAL_MP));
            long ss_offset_time = ss_timestamp - abm.getTimestamp(AutoBanTimestampType.SS_HEAL_MP);

            if (ss_offset_time < 1500) {
                AutobanFactory.FAST_MP_HEALING.addPoint(abm, "MP回復過快,時間-SS:" + String.valueOf(ss_offset_time));
            }
            if (ss_offset_time >= 4000) {
                chr.addMP(healMP);
            }

            //abm.updateTimestamp(AutoBanTimestampType.CS_HEAL_MP, cs_timestamp);
            abm.updateTimestamp(AutoBanTimestampType.SS_HEAL_MP, ss_timestamp);
        }
    }
}
