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

function start(mode, type, selection) {
    if (qm.forceStartQuest())
        qm.showInfoText("你已經得到了 <初階冒險家> 勳章。您可以找達利額領取勳章!。");
    qm.dispose();
}

function end(mode, type, selection) {
    status++;
    
    if (mode != 1){
        qm.sendOk("沒關係勳章會等著你的。");
        qm.dispose();
    }
    else {
        if (status == 0){
            qm.sendNext("恭喜你得到尊貴的 #b<初階冒險家>#k 勳章. 我祝你在你未來的努力萬事如意！持續保持著現在的表現。\r\n\r\n"+qm.showGiveItem()+"\r\n#v1142108:# #t1142108#");
            return;
        }
        else if (status == 1) {
            if (qm.canHold(1142108)) {
                qm.gainItem(1142108);
                qm.forceCompleteQuest();
            } else {
                qm.sendNext("你的裝備欄空間不足。");//NOT GMS LIKE
            }
            qm.dispose();
        }
    }

}