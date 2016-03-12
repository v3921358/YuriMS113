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
/**
	Dolphin - Aquarium(230000000)
**/

var status = 0;
var menu;
var payment = false;

function start() {
    if (cm.haveItem(4031242)) 
        menu = "#L0##b我將使用 #t4031242##k 傳送到 #b#m230030200##k.#l\r\n#L1#到 #b#m251000000##k 之後支付 #b10000楓幣#k.#l";
    else {
        menu = "#L0#到 #b#m230030200##k 之後支付 #b1000楓幣#k.#l\r\n#L1#到 #b#m251000000##k 之後支付 #b10000楓幣#k.#l";
        payment = true;
    }
    cm.sendSimple ("海洋都連接到彼此。地方，你無法通過步行到達可以輕鬆達到國外。關於如何服用 #b海豚計程車#k 我們今天要去哪??\r\n"+menu);
}

function action(mode, type, selection) {
    if (mode < 1) 
        cm.dispose();
    else {
        if (selection == 0) {
            if(payment) {
                if(cm.getPlayer().getMeso() < 1000) {
                    cm.sendOk("我認為你沒有足夠的楓幣....");
                    cm.dispose();
                } else
                    cm.gainMeso(-1000);
            } else
            cm.gainItem(4031242,-1);
            cm.warp(230030200);
            cm.dispose();
            return;
        } else if (cm.getPlayer().getMeso() < 10000) {
            cm.sendOk("我認為你沒有足夠的楓幣....");
            cm.dispose();
            return;
        }
        cm.gainMeso(-10000);
        cm.warp(251000100);
        cm.dispose();
    }
}