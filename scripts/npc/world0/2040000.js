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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Mel - Ludibrium Ticketing Place(220000100)
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
    1.2 - Shortened by Moogra
    1.1 - Price as GMS [sadiq]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

var cost = 6000;
var status = 0;

function start() {
    cm.sendYesNo("你好,我是碼頭服務員喬伊。你想離開玩具之城到天空之城嗎? 從這站到艾納斯大陸的#b天空之城#k的船隻\r需要花費#b"+cost+" 楓幣#k 購買#b#t4031045##k 才可以啟航.");
}

function action(mode, type, selection) {
    if(mode == -1)
        cm.dispose();
    else {
        if(mode == 1)
            status++;
        if(mode == 0) {
            cm.sendNext("你有一些經濟的負擔而無法搭船對吧?");
            cm.dispose();
            return;
        }
        if(status == 1) {
            if(cm.getMeso() >= cost && cm.canHold(4031045)) {
                cm.gainItem(4031045,1);
                cm.gainMeso(-cost);
            } else
                cm.sendOk("請問你有 #b"+cost+" 楓幣#k? 如果有的話,我勸您檢查下身上其他欄位看是否有沒有滿了.");
            cm.dispose();
        }
    }
}
