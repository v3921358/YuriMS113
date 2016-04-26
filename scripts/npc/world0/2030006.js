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
var status = 0;
var qChars = new Array ("Q1: 請問1等到2等需要多少經驗值?#10#12#15#20#3",
    "Q1: 請問本服的名字是?? #SMMS#YuriMS#ABCMS#FuckMS#2",
    "Q1: 1+1 =??#2#22#田#=#1",
    "Q1: 請問東區東區是誰哪個團體的??#巴豆妖#八三么#三八么#死人么#2",
    "Q1: 請問see you again是在紀念下列哪位明星?#馮迪索#保羅沃克#巨石強森#周杰倫#2");
var qItems = new Array( "Q2: 十元買早餐八元買豆乾 下列何者哪個字正確??#幹#乾#野#炸#1",
    "Q2: 1+2+3+.....+10?#45#100#70#55#4",
    "Q2: 5*0=?#500#5000#5#0#4",
    "Q2: FaceBook創始人是誰?#馬克.祖克柏#賈伯斯#小賈斯汀#周杰倫#1",
    "Q2: 英雄團的五大英雄為什麼從250等降低到10等?#天下沒有沒吃的午餐#受到黑橘的詛咒#受到黑魔法師的詛咒#不管是誰跟黑魔法師對戰都會變10等#3");
var qMobs = new Array(  "Q3: 請問,新楓之谷傳統登入的畫面中出現在畫面裡的怪物,何者沒有出現?#藍寶#綠水靈#緞帶肥肥#刺菇菇#4",
    "Q3: 請問精靈遊俠的種族是什麼?#妖精#精靈#惡魔#人類#2",
    "Q3: 法師1轉找誰轉職?#漢奸#漢堡#漢斯#汗濕#3",
    "Q3: 未來新葉城在哪個國家的maplestory有?#韓版#台版#國際版#日版#3",
    "Q3: 聖騎士使各技能+2的招示名稱為何?#戰鬥指令#戰鬥命令#決戰指令#決戰命令#2",
    "Q3: 請問日本古代神社沒有賣何種小吃？#章魚燒#炒麵#蕎麥麵#黑輪#3");
var qQuests = new Array("Q4: 楓谷傳說三職下列何者為非?#精靈遊俠#重砲指揮官#惡魔殺手#巴哈矮人#4",
    "Q4: 請問遊戲橘子2014年第一季年收入為多少??#24.66億元#24.60億元#24億元#25億元#1");
var qTowns = new Array( "Q5: 為何微軟不出win9直接出win10??#因為他爽#因為WIN9是廢物#因為你值得#避免與Windows95的衝突#4",
    "Q5: 下列哪個人死了??#保羅沃克#谷歌#臉書#馬英九#1",
    "Q5: 台版楓之谷的創立時間是??#2005年5月31日#2005年5月11日#2004年7月23日#2005年6月23日#1",
    "Q5: 某計算機有24K記憶體，若第一個位元組之位址為0000，則最後一個位元組之位址(十六進位)為?#6000#5FFF#5DBF#5DC0#2");
var correctAnswer = 0;

function start() {
	if (cm.haveItem(4031058, 1)) {
		cm.sendOk("#h #,你已經有了 #t4031058# 不要讓廢我時間.");
		cm.dispose();
	}
    if (!(cm.haveItem(4031058, 1))) {
        cm.sendNext("歡迎光臨 #h #, 我是 #p2030006#.\r\n看來你已經走了很遠到達了這個階段.");
    }
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendOk("下次再見.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1)
            cm.sendNextPrev("#h #, 如果你給我 #b黑暗水晶#k 我將會讓你試著回答5個問題,若您5個問題都答對您將得到 #v4031058# #b智慧項鍊#k.");
        else if (status == 2) {
            if (!cm.haveItem(4005004)) {
                cm.sendOk("#h #, 你沒有 #b黑暗水晶#k");
                cm.dispose();
            } else {
                cm.gainItem(4005004, -1);
                cm.sendSimple("測驗開始 #b接受挑戰吧!#k.\r\n\r\n" + getQuestion(qChars[Math.floor(Math.random() * qChars.length)]));
                status = 2;
            }
        } else if (status == 3) {
            if (selection == correctAnswer)
                cm.sendOk("#h # 你答對了.\n準備答下一題??");
            else {
                cm.sendOk("你答錯了的答案!.\r\n很抱歉你必須在給我一個 #b黑暗水晶#k 才可以再挑戰!");
                cm.dispose();
            }
        } else if (status == 4)
            cm.sendSimple("測驗開始 #b接受挑戰吧!#k.\r\n\r\n" + getQuestion(qItems[Math.floor(Math.random() * qItems.length)]));
        else if (status == 5) {
            if (selection == correctAnswer)
                cm.sendOk("#h # 你答對了.\n準備答下一題??");
            else {
                cm.sendOk("你答錯了的答案!.\r\n很抱歉你必須在給我一個 #b黑暗水晶#k 才可以再挑戰!");
                cm.dispose();
            }
        } else if (status == 6) {
            cm.sendSimple("測驗開始 #b接受挑戰吧!#k.\r\n\r\n" + getQuestion(qMobs[Math.floor(Math.random() * qMobs.length)]));
            status = 6;
        } else if (status == 7) {
            if (selection == correctAnswer)
                cm.sendOk("#h # 你答對了.\n準備答下一題??");
            else {
                cm.sendOk("你答錯了的答案!.\r\n很抱歉你必須在給我一個 #b黑暗水晶#k 才可以再挑戰!");
                cm.dispose();
            }
        } else if (status == 8)
            cm.sendSimple("測驗開始 #b接受挑戰吧!#k.\r\n\r\n" + getQuestion(qQuests[Math.floor(Math.random() * qQuests.length)]));
        else if (status == 9) {
            if (selection == correctAnswer) {
                cm.sendOk("#h # 你答對了.\n準備答下一題??");
                status = 9;
            } else {
                cm.sendOk("你答錯了的答案!.\r\n很抱歉你必須在給我一個 #b黑暗水晶#k 才可以再挑戰!");
                cm.dispose();
            }
        } else if (status == 10) {
            cm.sendSimple("最後一個問題.\r\n測驗開始 #b接受挑戰吧!#k.\r\n\r\n" + getQuestion(qTowns[Math.floor(Math.random() * qTowns.length)]));
            status = 10;
        } else if (status == 11) {
            if (selection == correctAnswer) {
                cm.gainItem(4031058, 1);
				cm.warp(211000001, 0);
                cm.sendOk("恭喜 #h #, 你太強大了.\r\n拿著這個 #v4031058# 去找你的轉職教官吧!.");
                cm.dispose();
            } else {
                cm.sendOk("太可惜了,差一題就可以通關了!! 多多加油><.\r\n很抱歉你必須在給我一個 #b黑暗水晶#k 才可以再挑戰!");
                cm.dispose();
            }
        }
    }
}
function getQuestion(qSet){
    var q = qSet.split("#");
    var qLine = q[0] + "\r\n\r\n#L0#" + q[1] + "#l\r\n#L1#" + q[2] + "#l\r\n#L2#" + q[3] + "#l\r\n#L3#" + q[4] + "#l";
    correctAnswer = parseInt(q[5],10);
    correctAnswer--;
    return qLine;
}