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
	NPC Name: 		Shanks
	Map(s): 		Maple Road : Southperry (60000)
	Description: 		Brings you to Victoria Island
*/
var status = 0;

function start() {
    cm.sendYesNo("搭上了這艘船，你可以前往更大的大陸冒險。 只要給我 #e150 楓幣#n，我會帶你去 #b維多利亞島#k。 不過，一旦離開了這裡，就不能再回來囉。 你想要去維多利亞島嗎？");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0 && type != 1)
            status -= 2;
        else if(type == 1 || (mode == -1 && type != 1)){
            if(mode == 0)
                cm.sendOk("恩... 我猜你還有想在這做的事？");
            cm.dispose();
            return;
        }
    }
    if (status == 1) {
        if (cm.haveItem(4031801))
            cm.sendNext("好, 現在給我 150 楓幣... 欸.. 你手上的是什麼？ 是路卡斯的推薦信嗎？ 嘿，你應該告訴我你有這一封信，能被路卡斯推薦，我想你應該會有很大的潛力成為一個傑出的冒險家。沒辦法，我免費送你過去吧！");
        else
            cm.sendNext("Bored of this place? Here... 先給我 #e150 楓幣#n first...");
    } else if (status == 2) {
        if (cm.haveItem(4031801))
            cm.sendNextPrev("既然你有推薦信，我不會收你任何的費用。收起來，我們將前往維多利亞島，坐好，旅途中可能會有點動盪！");
        else
        if (cm.getLevel() > 6) {
            if (cm.getMeso() < 150) {
                cm.sendOk("什麼？你說你想搭免費的船？ 你真是個怪人！");
                cm.dispose();
            } else
                cm.sendNext("哇! #e150#n 楓幣我收到了！ 好，準備出發去維多利亞港囉！");
        } else {
            cm.sendOk("讓我看看... 我覺得你還不夠強壯。 你至少要達到7等我才能讓你到維多利亞港囉。");
            cm.dispose();
        }
    } else if (status == 3) {
        if (cm.haveItem(4031801))
            cm.gainItem(4031801, -1);
        cm.warp(2010000);
        cm.dispose();
    }
}