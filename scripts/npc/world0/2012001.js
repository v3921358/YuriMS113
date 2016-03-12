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

function start() {
    var em = cm.getEventManager("Boats");
    if (em.getProperty("entry") == "true")
        cm.sendYesNo("你要搭往返回維多利亞港魔法森林的船?");
    else {
        //if (em.getProperty("entry") == "false" && em.getProperty("docked") == "true")
            cm.sendOk("很抱歉我們在整理船艙.\r\n船五分鐘後就要開走了.\r\n請等待下一班.");
        cm.dispose();
    }
}

function action(mode, type, selection){
	cm.sendNext("旅途愉快^^");
    cm.warp(200000112);
    cm.dispose();
}