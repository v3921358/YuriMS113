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
/* guild creation npc */
var status = 0;
var sel;

importPackage(Packages.tools.packets);

function start() {
    cm.sendSimple("你想要做什麼？\r\n#b#L0#創立公會#l\r\n#L1#解散公會#l\r\n#L2#增加公會規模#l#k");
}

function action(mode, type, selection) {
    
    
    if (mode == -1) {
        cm.dispose();
    } else {

        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }

        if (mode == 1)
            status++;
        else
            status--;

        if (status == 1) {
            sel = selection;
            if (selection == 0) {
                if (cm.getPlayer().getGuildId() > 0) {
                    cm.sendOk("你已經有公會了，不能創了。");
                    cm.dispose();
                } else {
                    cm.sendYesNo("建立公會需要 #b 1500000 楓幣#k，你確定要繼續？");
                }
            } else if (selection == 1) {
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
                    cm.sendOk("只有公會長才能解除公會哦。");
                    cm.dispose();
                } else {
                    cm.sendYesNo("你確定要解散公會嗎？所有的公會紀錄都會消失哦～ 你確定要繼續？");
                }
            } else if (selection == 2) {
                if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
                    cm.sendOk("只有公會長才能擴增公會規模。");
                    cm.dispose();
                } else {
                    cm.sendYesNo("擴增公會規模需要 #b5#k 花費 #b 500000 楓幣#k，你確定要繼續？");
                }
            }
        } else if (status == 2) {
            if (sel == 0 && cm.getPlayer().getGuildId() <= 0) {
                cm.sendOk("公會已經創立好了，你確認看看。");
                cm.getPlayer().genericGuildMessage(1);
                cm.dispose();
            } else if (cm.getPlayer().getGuildId() > 0 && cm.getPlayer().getGuildRank() == 1) {
                if (sel == 1) {
                    cm.sendOk("公會已經解除了，你確認看看。");
                    cm.getPlayer().disbandGuild();
                    cm.dispose();
                } else if (sel == 2) {
                    cm.sendOk("擴展公會規模完畢，你確認看看。");
                    cm.getPlayer().increaseGuildCapacity();
                    cm.dispose();
                }
            }
        }
    }
}
