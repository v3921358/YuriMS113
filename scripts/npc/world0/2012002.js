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
 * @Author: Moogra
 * @NPC ID: 2012002
 * @NPC   : Erin (On Orbis Boat )
 */

var status = 0;
function start() {
    status = -1; 
        action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        //cm.sendOk("等你準備好在來吧。");
        cm.dispose();
    } else if (mode == 0) {
        cm.sendOk("選的好，船快出發了，在等一下。");
        cm.dispose();
    } else {
        if (mode == 1) { 
            status++; 
        } else { 
            status--; 
        } 
        if (status == 0) { 
            cm.sendYesNo("你想要離開船嗎? \n\r 船票不會歸還喔。"); // 點選 NPC 後觸發
        } else if (status == 1) { 
            cm.sendNext("好吧,照顧好自己，下次見。");
        }else if (status == 2) { 
            cm.warp(200000111, 0);// back to Orbis jetty
            cm.dispose();
        }
    }
}

