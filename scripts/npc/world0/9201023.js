/*
	NPC Name: 		Hera
	Map(s): 		Towns
	Description: 		Wedding Village Entrance
*/

var status = -1;

function start() {
    cm.sendSimple("你好~ 今天天氣很不錯，需要我拯救您這個單身老人在楓之谷的世界嗎?? \n\r #b#L0# 我想要去結婚小鎮.#l \n\r #L1# 我已經結婚了!! 我想要領取戀人之心椅子 #l");
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else if (status == 1 && mode == 0) {
        cm.sendOk("難道你真的會想念這個令人難以置信的機會呢？這是一個非常美麗的地方。也許你還沒有遇到你愛的人？究竟它是。如果你愛上一個人那是不可能無視這個可愛的新聞.");
        cm.dispose();
        return;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        switch (selection) {
            case 0:
                cm.sendNext("哦！多麼美好的一天！這個世界是多麼的美好〜！這個世界似乎是充滿愛的，不是嗎？我可以從這裡感受到愛的精神填補了婚禮!");
                break;
            case 1:
                cm.sendOk("很抱歉親愛的這個選項只針對有結婚的人所設計的，如果你想要椅子請先結婚!.");
                cm.dispose();
                break;
        }
    } else if (status == 1) {
        cm.sendYesNo("你曾經去過的婚禮村莊？這是一個了不起的地方，愛情是無極限的。恩愛夫妻可以結婚還有，如何浪漫它是什麼？如果你想在那裡，我會告訴你的方式.");
    } else if (status == 2) {
        cm.sendNext("你做了一個正確的決定！你可以感受到愛的精神在婚禮村發揮到淋漓盡致。當你想回來，你的目的地將在這裡，所以不要擔心.");
    } else if (status == 3) {
	   cm.getPlayer().saveLocation("AMORIA");
	   cm.warp(680000000, 0);
       cm.dispose();
		}
    }