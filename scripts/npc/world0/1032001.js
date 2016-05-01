var status = 0;
var jobId;
var jobName;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }
    if (mode == 0 && status == 2) {
        cm.txtPrint("mode:" + mode);
        cm.txtPrint("type:" + type);
        cm.txtPrint("selection:" + selection);
        cm.txtPrint("status:" + status);
        cm.sendOk("沒關系，等你作好了決定在來找我吧.");
        cm.dispose();
        return;
    }
    if (mode == 1)
        status++;
    else
        status--;
    if (status == 0) {
        if (cm.getJobId() == 0) {
            if (cm.getPlayer().getLevel() >= 8) {
                cm.sendNext("想成為#r法師#k？有一些條件要滿足。因為我們不可能接受每個人成為法師...你的等級至少要8等，還有智力至少要#b25#k以上。\r\n" +
                        "讓我看看你是不是能成為#r法師#k.");
            } else {
                cm.sendOk("你要多努力一點才能成為#r法師#k.")
                cm.dispose();
            }
        } else {
            if (cm.getPlayer().getLevel() >= 30 && cm.getJobId() == 200) { // 法師
                if (cm.haveItem(4031012, 1)) {
                    if (cm.haveItem(4031012, 1)) {
                        status = 20;
                        cm.sendNext("我看到你完成了測試. 想要繼續轉職請點下一頁!");
                    } else {
                        if (!cm.haveItem(4031009)) {
                            cm.gainItem(4031009, 1);
                        }
                        cm.sendOk("請去找 #r法師轉職教官#k.")
                        cm.dispose();
                    }
                } else {
                    status = 10;
                    cm.sendNext("我看你已經做得很好了。我允許你的漫長法師之路進入下一階段。");
                }
            } else if (cm.getPlayer().getLevel() >= 70) {
                if (cm.haveItem(4031059, 1)) {
                    cm.gainItem(4031057, 1);
                    cm.gainItem(4031059, -1);
                    cm.warp(211000001, 0);
                    cm.sendOk("你完成了一個考驗，現在去找 #b羅貝亞#k.");
                } else {
                    cm.sendOk("嗨, #b#h0##k! 我需要一個 #b黑符#k. 快去找異次元空間拿給我.");
                }
                cm.dispose();
            } else {
                cm.sendOk("你好,我是法師轉職官.");
                cm.dispose();
            }
        }
    } else if (status == 1) {
        cm.sendNextPrev("哦...！你看起來像某些人，絕對是可以成為我們的一部分... ...所有你需要的是一點點陰險的頭腦，和...是啊...所以，你有什麼感想？想成為#r法師#k？ \r\n 一旦作好絕定就不能改變了喔。");
    } else if (status == 2) {
        cm.sendYesNo("你真的要成為一位 #r法師#k ?");
    } else if (status == 3) {
        if (cm.canHold(1372043)) {
            if (cm.getJobId() == 0) {
                cm.changeJobById(200); // 法師
                cm.resetStats();
                cm.gainItem(1372005, 1);
            }
            cm.sendOk("好，從這裡出來，你是我們的一部分了！你會過著流浪的生活...但只要保持耐心，你會生活在上流社會的。雖然不是很多，但我會給你一些我的能力......哈哈哈！");
        } else {
            cm.sendNext("你的裝備欄滿了，去確認一下吧。");
        }
        cm.dispose();
    } else if (status == 11) {
        cm.sendNextPrev("你可以選擇你要轉職成為一位 #r巫師(火,毒)#k, #r巫師(冰,雷)#k 或 #r僧侶#k.");
    } else if (status == 12) {
        cm.askAcceptDecline("但是我必須先測試你,你準備好了嗎 ?");
    } else if (status == 13) {
        cm.gainItem(4031009, 1);
        cm.warp(101020000);
        cm.sendOk("請去找 #b法師轉職教官#k . 他會幫助你的.");
        cm.dispose();
    } else if (status == 21) {
        cm.sendSimple("你想要成為什麼 ? #b\r\n#L0#巫師(火,毒)#l\r\n#L1#巫師(冰,雷)#l\r\n#L2#僧侶#l#k");
    } else if (status == 22) {
        if (selection == 0) {
            jobName = "巫師(火,毒)";
            jobId = 210; // FP
        } else if (selection == 1) {
            jobName = "巫師(冰,雷)";
            jobId = 220; // IL
        } else {
            jobName = "僧侶";
            jobId = 230; // CLERIC
        }
        cm.sendYesNo("你真的要成為一位 #r" + jobName + "#k?");
    } else if (status == 23) {
        cm.changeJobById(jobId);
        cm.gainItem(4031012, -1);
        cm.sendOk("轉職成功 ! 請去開創天下吧.");
        cm.dispose();
    }
}