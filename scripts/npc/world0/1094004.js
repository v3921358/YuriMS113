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
/*
    Author: Yuri
    NPC: Bush - Abel Glasses Quest
*/
var status = 0;
var item;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        status--;
    if (status == 0) {
        if (cm.isQuestStarted(2186) == 1) {
            var rand = randint(0, 2);
            if (rand == 0) {
                item = 4031853;
            } else if (rand == 1) {
                item = 4031854;
            } else {
                item = 4031855;
            }
            if (!cm.haveItem(item)) {
                cm.gainItem(item, 1);
                if (item == 4031853) {
                    cm.sendNext("找到了安培爾的眼鏡了.");
                } else {
                    cm.sendOk("找到了一附眼鏡, 但看起來不像安培爾的眼鏡. 安培爾的眼鏡是黑色鏡片...");
                }
            } else {
                cm.sendOk("什麼都沒有.....");
                cm.dispose();
            }
        } else {
            cm.sendOk("這草叢看起來很礙眼...");
            cm.dispose();
        }
        cm.dispose();
    }
}

function randint(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
}
