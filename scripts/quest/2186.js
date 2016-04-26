
/*
 Author: YuriMS V1.13
 Quest: Abel Glasses Quest
 */
var sec = false;
var status = -1;
function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        qm.dispose();
    } else {
        if (qm.haveItem(4031853)) {
            if (qm.canHold(2030019)) {
                if (status == 0) {
                    if (qm.haveItem(4031854) || qm.haveItem(4031855)) {
                        sec = true;
                        qm.sendOk("我的眼鏡....我的眼鏡....\r\n" + qm.showGiveItem() + "#v2030019:# #t2030019# 10個" +
                                qm.showGiveExp() + " 1700 exp");
                    } else {
                        qm.sendOk("我的眼鏡....我的眼鏡....\r\n" + qm.showGiveItem() + "#v2030019:# #t2030019# 5個" +
                                qm.showGiveExp() + " 1000 exp");
                    }
                } else if (status == 1) {
                    if (qm.haveItem(4031854))
                        qm.gainItem(4031854, -1);
                    if (qm.haveItem(4031855))
                        qm.gainItem(4031855, -1);
                    qm.gainItem(4031853, -1);
                    qm.forceCompleteQuest();
                    if (sec) {
                        qm.gainItem(2030019, 10);
                        qm.gainExp(1700);
                    } else {
                        qm.gainItem(2030019, 5);
                        qm.gainExp(1000);
                    }
                    qm.dispose();
                }
            } else {
                qm.sendOk("你的物品欄己經滿了.");
                qm.dispose();
            }
        } else if (!qm.haveItem(4031853) && (qm.haveItem(4031854) || qm.haveItem(4031855))) {
            qm.sendOk("很抱歉這不是我的眼鏡.");
            qm.dispose();
        }
    }
}
