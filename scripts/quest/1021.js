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
/* Author: Xterminator (Modified by XxOsirisxX)
	NPC Name: 		Roger
	Map(s): 		Maple Road : Lower level of the Training Camp (2)
	Description: 		Quest - Roger's Apple
*/
importPackage(Packages.client);

var status = -1;

function start(mode, type, selection) {
    status++;
    if (status == 0)
        qm.sendNext("嗨, 怎麼了嗎? 我是羅傑，可以教你一些有用的知識");
    else if (status == 1)
        qm.sendNextPrev("你問我為什麼在這嗎? 哈哈哈!\r我想要教導那些剛進楓之谷的冒險者們。");
    else if (status == 2)
        qm.sendAcceptDecline("所以..... 讓我們來玩點有趣的~!");
    else if (status == 3) {
        if (qm.c.getPlayer().getHp() >= 50) {
            qm.c.getPlayer().addHP(-25);
        }
        if (!qm.haveItem(2010007))
            qm.gainItem(2010007, 1);
            qm.forceStartQuest();
            qm.sendNext("嚇到了嗎? 如果血量變成0，你將會遇到大麻煩。我等等會給你一顆#rRoger's Apple#k。 請務必收下啊。 使用後你會變得更強壯。 打開消耗欄，雙擊一下蘋果 很簡單的，按一下鍵盤的 #bI#k就能了喔！");
    } else if (status == 4) {
        qm.sendNextPrev("怎麼？你還沒把我給你的蘋果吃掉！？吃完你會發現HP上升了。請將血量回覆到100%再來找我吧。");
    } else if (status == 5) {
        qm.showInfo("UI/tutorial.img/28");
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0)
            status -= 2;
        else{
            qm.dispose();
            return;
        }
    }
    if (status == 0)
        if (qm.c.getPlayer().getHp() < 50) {
            qm.sendNext("哈囉，你還沒把我給你的蘋果吃掉啊，趕快吃了再來找我吧。");
            qm.dispose();
        } else
            qm.sendNext("你看～是不是很簡單？ 你可以在右側的欄位設定#b熱鍵#k。 哈哈，你聽不懂對吧？ 喔，每隔一段時間，血量就會恢復了。 雖然很花時間，但好好運用的話可以幫助不少的。");
    else if (status == 1)
        qm.sendNextPrev("好了！ 我已經教你很多了，該給你禮物了。 這是一些未來能幫助你在楓之谷世界的一些物品，請在緊急的時候使用它們。");
    else if (status == 2)
        qm.sendNextPrev("恩，我能教你的就只有這些了 我知道很難過，但是我們真的該說再見了。 好好照顧自己吧\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2010000# 3 #t2010000#\r\n#v2010009# 3 #t2010009#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 10 經驗值");
    else if (status == 3) {
        if(qm.isQuestCompleted(1021))
            qm.dropMessage(1,"Unknown Error");
        else if(qm.canHold(2010000) && qm.canHold(2010009)){
            qm.gainExp(10);
            qm.gainItem(2010000, 3);
            qm.gainItem(2010009, 3);
            qm.forceCompleteQuest();
        }else
            qm.dropMessage(1,"你的背包已滿");
        qm.dispose();
    }
}
