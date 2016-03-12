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
/* Author: Xterminator
	NPC Name: 		Sera
	Map(s): 		Maple Road : Entrance - Mushroom Town Training Camp (0), Maple Road: Upper level of the Training Camp (1), Maple Road : Entrance - Mushroom Town Training Camp (3)
	Description: 		First NPC
*/

var status = -1;

function start() {
    if (cm.c.getPlayer().getMapId() == 0 || cm.c.getPlayer().getMapId() == 3)
        cm.sendYesNo("歡迎來到楓之谷世界，這個新手訓練專門為新手而設計的，你想要進入新手訓練嗎？ 有些人選擇不進入這個訓練，但是我還是會建議你進來這裡。");
    else
        cm.sendNext("This is the image room where your first training program begins. In this room, you will have an advance look into the job of your choice.");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(mode == 0 && status == 0){
            cm.sendYesNo("你想要直接開始冒險嗎？");
            return;
        }else if(mode == 0 && status == 1 && type == 0){
            status -= 2;
            start();
            return;
        }else if(mode == 0 && status == 1 && type == 1)
            cm.sendNext("當你做好決定，再來跟我說ㄧ聲吧！");
        cm.dispose();
        return;
    }
    if (cm.c.getPlayer().getMapId() == 0 || cm.c.getPlayer().getMapId() == 3){
        if(status == 0){
            cm.sendNext("好的那我要帶進入新手訓練課程了。 Please follow your instructor's lead.");
        }else if(status == 1 && type == 1){
            cm.sendNext("It seems like you want to start your journey without taking the training program. Then, I will let you move on to the training ground. Be careful~");
        }else if(status == 1){
            cm.warp(1);
            dispose();
        }else{
            cm.warp(40000);
            dispose();
        }
    }else
    if(status == 0)
        cm.sendPrev("Once you train hard enough, you will be entitled to occupy a job. You can become a Bowman in Henesys, a Magician in Ellinia, a Warrior in Perion, and a Thief in Kerning City...");
    else
        cm.dispose();
}