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

status = -1;
var sel;
empty = [false, false, false];

function start() {
    if((cm.getPlayer().getLevel() < 19 || cm.getPlayer().getLevel() > 30) && !cm.getPlayer().isGM()){
        cm.sendNext("你的等級低於20或者超過30,很抱歉你不能參加此活動!!");
        cm.dispose();
        return;
    }
    var text = "你想要做什麼??#b";
    for(var i = 0; i < 3; i += 1)
        if (cm.getPlayerCount(980010100 + (i * 100)) > 0)
            if(cm.getPlayerCount(980010101 + (i * 100)) > 0)
                continue;
            else
                text += "\r\n#L" + i + "# 戰鬥房間 " + (i + 1) + "([" + cm.getPlayerCount(980010100 + (i * 100)) + "/" + cm.getPlayer().getAriantSlotsRoom(i) + "] 玩家" + cm.getPlayer().getAriantRoomLeaderName(i) + "/Lv 20~29 )#l";
        else{
            empty[i] = true;
            text += "\r\n#L" + i + "# 戰鬥房間 " + (i + 1) + "( 空房 )#l";
            if(cm.getPlayer().getAriantRoomLeaderName(i) != "")
                cm.getPlayer().removeAriantRoom(i);
        }
    cm.sendSimple(text + "\r\n#L3# 我想了解更多競技規則.#l");
}

function action(mode, type, selection){
    status++;
    if(mode != 1){
        if(mode == 0 && type == 0)
            status -= 2;
        else{
            cm.dispose();
            return;
        }
    }
    if (status == 0){
        if(sel == undefined)
            sel = selection;
        if(sel == 3)
            cm.sendNext("你這個菜鳥,有什麼需要我幫忙的??");
        else{
            if(cm.getPlayer().getAriantRoomLeaderName(sel) != "" && empty[sel])
                empty[sel] = false;
            else if(cm.getPlayer().getAriantRoomLeaderName(sel) != ""){
                cm.warp(980010100 + (sel * 100));
                cm.dispose();
                return;
            }
            if(!empty[sel]){
                cm.sendNext("另一種戰鬥已經首次創建競技場戰鬥。我勸你要么建立一個新的，或者加入戰鬥競技場包括已經成立。");
                cm.dispose();
                return;
            }
            cm.sendGetNumber("最多有多少參與者可以參加這場比賽？ (2~6 人)", 0, 2, 6);
        }
    }else if (status == 1){
        if(sel == 3)
            cm.sendNextPrev("它是很簡單的 實際上. 你會拿到這 #b#t2270002##k 道具, 你的使命就是要消除怪物數量 然後用 #b#t2270002##k 吸收能量.");
        else{
            if(cm.getPlayer().getAriantRoomLeaderName(sel) != "" && empty[sel])
                empty[sel] = false;
            if(!empty[sel]){
                cm.sendNext("另一種戰鬥已經首次創建競技場戰鬥。我勸你要么建立一個新的，或者加入戰鬥競技場包括已經成立.");
                cm.dispose();
                return;
            }
            cm.getPlayer().setAriantRoomLeader(sel, cm.getPlayer().getName());
            cm.getPlayer().setAriantSlotRoom(sel, selection);
            cm.warp(980010100 + (sel * 100));
            cm.dispose();
        }
    }else if (status == 2)
        cm.sendNextPrev("這很簡單。如果你吸收怪物的力量 #b#t2270002##k, 那麼你會有 #b#t4031868##k, 你必須防止別人吸收才能贏得勝利!!");
    else if (status == 3)
        cm.sendNextPrev("但有個規則. #r你不能使用寵物這一點#k 明白嗎??~!");
    else if (status == 4)
        cm.dispose();
}
