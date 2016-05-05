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
 Rini (Orbis Boat Loader) 2012001
 **/
var status = 0;
function start() {
    var em = cm.getEventManager("Boats");
    if (em.getProperty("entry") == "true") {
        status = -1; 
        action(1, 0, 0);
    } else {
        cm.sendOk("很抱歉現在不能上船。\r\n請等待下一班。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == -1) {
        //cm.sendOk("等你準備好在來吧。");
        cm.dispose();
    } else if (mode == 0) {
        cm.sendOk("等你準備好在來搭船吧。");
        cm.dispose();
    } else {
        if (mode == 1) { 
            status++; 
        } else { 
            status--; 
        } 
        if (status == 0) { 
            if (cm.haveItem(4031047)) {
                cm.sendYesNo("你要搭往返回維多利亞港魔法森林的船?"); // 點選 NPC 後觸發
            } else {
                cm.sendOk("你身上沒有船票，抱歉你不能上船。");
                cm.dispose();
            }
        } else if (status == 1) { 
            cm.gainItem(4031047, -1)
            cm.warp(200000112, 0);// back to Orbis jetty
            cm.dispose();
        }
    }
}