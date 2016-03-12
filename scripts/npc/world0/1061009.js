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
/* Door of Dimension
	Enter 3rd job event
*/

function start() {
    if (!(cm.haveItem(4031059))) {
		if (cm.getPlayer().getMapId() == 100040106 && cm.getJobId() == 210 || cm.getJobId() == 220 || cm.getJobId() == 230){
		cm.warp(108010200, 0);
		cm.spawnMobOnMap(9001001,1,-276,-3,108010201);
		cm.sendOk("法師3轉的試煉即將開始!!");
		cm.dispose();
		} else if (cm.getPlayer().getMapId() == 105070001 && cm.getJobId() == 110 || cm.getJobId() == 120 || cm.getJobId() == 130) {
		cm.warp(108010300, 0);
		cm.spawnMobOnMap(9001000,1,-276,-3,108010301);
		cm.sendOk("劍士3轉的試煉即將開始!!");
		cm.dispose();
		} else if (cm.getPlayer().getMapId() == 105040305 && cm.getJobId() == 310 || cm.getJobId() == 320) {
		cm.warp(108010100, 0);
		cm.spawnMobOnMap(9001002,1,-276,-3,108010101);
		cm.sendOk("弓箭手3轉的試煉即將開始!!");
		cm.dispose();
		} else if (cm.getPlayer().getMapId() == 107000402 && cm.getJobId() == 410 || cm.getJobId() == 420) {
		cm.warp(108010400, 0);
		cm.spawnMobOnMap(9001003,1,-276,-3,108010401);
		cm.sendOk("盜賊3轉的試煉即將開始!!");
		cm.dispose();
		} else if (cm.getPlayer().getMapId() == 107000402 && cm.getJobId() == 510 || cm.getJobId() == 520) {
		cm.warp(108010500, 0);
		cm.spawnMobOnMap(9001004,1,-276,-3,108010501);
		cm.sendOk("海盜3轉的試煉即將開始!!");
		cm.dispose();
		} else {
			cm.sendOk("錯誤請重新嘗試");
			cm.dispose();
			}
    cm.dispose();
}
}