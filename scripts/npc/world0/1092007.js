var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.sendOk("等你準備好在來吧。");
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            cm.sendOk("等你準備好在來吧。");
            cm.dispose();
        }
        
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (cm.isQuestStarted(2175)) {
            if (status == 0) {
                cm.sendYesNo("你準備幹黑魔法師的手下了嗎?? 我將把你傳送過去...");
            } else if (status == 1) {
                cm.sendOk("好，我會送你到黑魔法師弟子的區域。\r\n我會送你過去，尋找周圍的肥肥。\r\n你可以透過肥肥們找到它。");
            } else if (status == 2) {
                cm.warp(912000000, 0);
                cm.dispose();
            }
        } else {
            cm.sendOk("該死的黑魔師!!");
            cm.dispose();
        }
    }
}
