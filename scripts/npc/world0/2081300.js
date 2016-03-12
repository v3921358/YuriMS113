/*  NPC : 瑞吉爾
	弓箭手 4轉 任務腳本
	地圖代碼 (240010501)
*/

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 0) {
	cm.dispose();
	return;
    }
    if (mode == 1)
	status++;
    else
	status--;

    if (status == 0) {
	if (!(cm.getJobId() == 311 || cm.getJobId() == 321)) {
	    cm.sendOk("為什麼你要見我??還有你想要問我關於什麼事情??");
	    cm.dispose();
	    return;
	} else if (cm.getPlayer().getLevel() < 120) {
	    cm.sendOk("你等級尚未到達120級.");
	    cm.dispose();
	    return;
	} else {
		if (cm.getJobId() == 311){
		    cm.sendSimple("恭喜你有資格4轉. \r\n請問你想4轉嗎??\r\n#b#L0#我想成為箭神.#l\r\n#b#L1#像我想一下...#l");
		} else if(cm.getJobId() == 321){
		    cm.sendSimple("恭喜你有資格4轉. \r\n請問你想4轉嗎??\r\n#b#L0#我想成為神射手.#l\r\n#b#L1#像我想一下...#l");
	    } else {
		cm.sendOk("好吧假如你想要4轉麻煩再來找我");
		cm.dispose();
		return;
	    }
	}
    } else if (status == 1) {
	if (selection == 1) {
		cm.sendOk("好吧假如你想要4轉麻煩再來找我");
	    cm.dispose();
	    return;
	}
	if (cm.getPlayer().getRemainingSp() <= (cm.getLevel() - 120) * 3) {
	    cm.sendOk("你的技能點數還沒點完..");
	    cm.dispose();
	    return;
	} else {
	    if (cm.canHold(2280003)) {
		cm.gainItem(2280003, 1);

		if (cm.getJobId() == 311) {
		    cm.changeJobById(312);
		    cm.teachSkill(3120005,0,10,-1);
		    cm.teachSkill(3121007,0,10,-1);
		    cm.teachSkill(3121002,0,10,-1);
		    cm.sendNext("恭喜你轉職為 #b箭神#k.我送你一些神秘小禮物^^");
		} else {
		    cm.changeJobById(322);
		    cm.teachSkill(3221006,0,10,-1);
		    cm.teachSkill(3220004,0,10,-1);
		    cm.teachSkill(3221002,0,10,-1);
		    cm.sendNext("恭喜你轉職為 #b神射手#k.我送你一些神秘小禮物^^");
		}
	    } else {
		cm.sendOk("你沒有多的欄位請清空再來嘗試一次!");
		cm.dispose();
		return;
	    }
	}
    } else if (status == 2) {
	if (cm.getJobId() == 312) {
	    cm.sendNext("不要忘記了這一切都取決於你練了多少.");
	} else {
	    cm.sendNext("不要忘記了這一切都取決於你練了多少.");
	}
    } else if (status == 3) {
	cm.sendNextPrev("我已你為榮.");
	cm.dispose();
    }
}