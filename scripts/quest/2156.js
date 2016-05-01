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
/*	
 Author : 		kevintjuh93
 Description: 		Quest - Junior Adventurer
 Quest ID : 		29901
 */

var status = -1;

function end(mode, type, selection) {
    status++;

    if (mode != 1) {
        qm.dispose();
    } else {
        if (status == 0) {
            qm.sendNext("想不到你真的找到了#t2210006#. 謝謝你，這是給你的獎勵。\r\n" +
                    qm.showGiveFame() + " 3 名聲值\r\n" +
                    qm.showGiveExp() + " 7500 EXP\r\n" +
                    qm.showGiveMeso() + " 30000 楓幣");
            return;
        } else if (status == 1) {
            qm.getPlayer().gainFame(3);
            qm.gainExp(7500);
            qm.gainMeso(30000);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }

}
