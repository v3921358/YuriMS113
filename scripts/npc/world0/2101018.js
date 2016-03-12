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
function start() {
    if((cm.getPlayer().getLevel() < 19 || cm.getPlayer().getLevel() > 30) && !cm.getPlayer().isGM()){
        cm.sendNext("你的等級低於20或者超過30,很抱歉你不能參加此活動!!");
        cm.dispose();
        return;
    }
    action(1,0,0);
}

function action(mode, type, selection){
    status++;
    if (status == 4){
        cm.getPlayer().saveLocation("MIRROR");
        cm.warp(980010000, 3);
        cm.dispose();
    }
    if(mode != 1){
        if(mode == 0 && type == 0)
            status -= 2;
        else{
			cm.sendNextPrev("看來你還沒準備好,準備好再來找我!!#k");
            cm.dispose();
            return;
        }
    }
    if (status == 0)
        cm.sendNext("我已經準備了一個巨大的節日，在這裡的沙漠是為了楓之谷的偉大戰士。這就是所謂的 #b納希競技大會#k.");
    else if (status == 1)
        cm.sendNextPrev("準備好就按下頁...#k");
    else if (status == 2)
        cm.sendSimple("如果你是從一個堅強勇敢的 #b戰士#k, 請問您有興趣嗎?? \r\n#b#L0# 我很想參加這個偉大的比賽#l");
    else if (status == 3)
        cm.sendNext("好吧，現在我給你的戰鬥舞台。我想看到你取得勝利！");
}