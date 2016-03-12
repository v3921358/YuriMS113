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
NPC:        Muirhat - Nautilus' Port
Created By: Cyndicate, shortened by Moogra
Function:   No specific function, useless text.
*/

function start() {
	if (cm.isQuestStarted(2175)) {
	cm.sendOk("你準備幹黑魔法師的手下了嗎?? 我將把你傳送過去...");
    } else {
    cm.sendOk("這黑魔師真它媽的該死!!");
    cm.dispose();
}

function action(mode, type, selection) {
    cm.warp(912000000,0);
    cm.dispose();
}
