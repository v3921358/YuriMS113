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
-- Odin JavaScript --------------------------------------------------------------------------------
	Regular Cab - Victoria Road : Ellinia (101000000)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var maps = Array(120000000, 102000000, 100000000, 103000000, 101000000);
var cost = Array(1200, 1000, 1000, 1200, 1200);
var costBeginner = Array(120, 100, 100, 120, 120);
var selectedMap = -1;
var job;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.dispose();
            return;
        } else if (status >= 2 && mode == 0) {
            cm.sendNext("有很多看到在這個鎮上了。回來找我們，當你需要去不同的鎮.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendNext("您好~! 維多利亞港計程車. 想要往其他村莊安全又快速的移動嗎? 如果是這樣 為了優先考量滿足顧客, 請使用 #b維多利亞港計程車#k 特別免費! 親切的送你到想要到達的地方");
        } else if (status == 1) {
            if (cm.getJobId()==0) {
                var selStr = "我們有特殊90%折扣，對於新手選擇你的目的地#b \n\r請選擇目的地.#b";
                for (var i = 0; i < maps.length; i++) {
                    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + costBeginner[i] + " mesos)#l";
                }
            } else {
                var selStr = "請選擇目的地.#b";
                for (var i = 0; i < maps.length; i++) {
                    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + cost[i] + " mesos)#l";
                }
            }
            cm.sendSimple(selStr);
        } else if (status == 2) {
			if (cm.getJobId()==0) {
            cm.sendYesNo("你在這裡沒有任何東西做，是吧? #b#m" + maps[selection] + "##k 他將花費你的 #b"+ costBeginner[selection] + " 楓幣#k.");
            selectedMap = selection;
			} else {
			cm.sendYesNo("你在這裡沒有任何東西做，是吧? #b#m" + maps[selection] + "##k 他將花費你的 #b"+ cost[selection] + " 楓幣#k.");
			selectedMap = selection;
			}
        } else if (status == 3) {
            if (cm.getJobId()==0) {
                if (cm.getMeso() < costBeginner[selection]) {
                    cm.sendNext("很抱歉由於你沒有足夠的楓幣 所以你將無法乘坐出租車!");
                    cm.dispose();
                } else {
                    cm.gainMeso(-costBeginner[selectedMap]);
					cm.warp(maps[selectedMap], 0);
					cm.dispose();
                }
            } else {
                if (cm.getMeso() < cost[selection]) {
                    cm.sendNext("很抱歉由於你沒有足夠的楓幣 所以你將無法乘坐出租車!");
                    cm.dispose();
                } else
                cm.gainMeso(-cost[selectedMap]);
                cm.warp(maps[selectedMap], 0);
                cm.dispose();
            }
        }
    }
}