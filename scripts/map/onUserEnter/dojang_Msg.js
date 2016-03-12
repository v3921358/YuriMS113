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
	Author: Traitor
	Map(s):	Mu Lung Dojo Entrance
	Desc:   Sends the entrance message or the taunt message from that dojo guy
*/
var messages = Array("你的勇氣的挑戰睦隆道場是難能可貴的！", "如果你想品嚐失利的苦澀，進來吧！", "我會讓你後悔徹底挑戰睦隆道場！快點！");

function start(ms) {
    if (ms.getPlayer().getMap().getId() == 925020000) {
        ms.getPlayer().startMapEffect(messages[(Math.random() * messages.length) | 0], 5120024);
    } else {
        ms.getPlayer().resetEnteredScript(); //in case the person dcs in here we set it at dojang_tuto portal
        ms.getPlayer().startMapEffect("哈！讓我們來看看你得到了什麼！我不會讓你離開，除非你先打敗我！", 5120024);
    }
}
