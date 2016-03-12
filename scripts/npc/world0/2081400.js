/*  NPC : 海倫
	盜賊 4轉 任務腳本
	地圖代碼 (240010501)
 */

var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }

    if (status == 0) {
	if (!(cm.getJobId() == 411 || cm.getJobId() == 421 || cm.getJobId() == 433)) {
	    cm.sendOk("為什麼你要見我??還有你想要問我關於什麼事情??");
	    cm.dispose();
	    return;
	} else if (cm.getPlayer().getLevel() < 120) {
	    cm.sendOk("你等級尚未到達120級.");
	    cm.dispose();
	    return;
	} else {
		if (cm.getJobId() == 411){
		    cm.sendSimple("恭喜你有資格4轉. \r\n請問你想4轉嗎??\r\n#b#L0#我想成為夜使者.#l\r\n#b#L1#像我想一下...#l");
		} else if (cm.getJobId() == 421){
		    cm.sendSimple("恭喜你有資格4轉. \r\n請問你想4轉嗎??\r\n#b#L0#我想成為暗影神偷.#l\r\n#b#L1#像我想一下...#l");
	    } else {
		cm.sendOk("好吧假如你想要4轉麻煩再來找我");
		cm.safeDispose();
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

		if (cm.getJobId() == 411) {
		    cm.changeJobById(412);
		    cm.teachSkill(4120002,0,10,-1);
		    cm.teachSkill(4121006,0,10,-1);
		    cm.teachSkill(4120005,0,10,-1);
		    cm.sendNext("恭喜你轉職為 #b夜使者#k.我送你一些神秘小禮物^^");
		} else if (cm.getJobId() == 421) {
		    cm.changeJobById(422);
		    cm.teachSkill(4220002,0,10,-1);
		    cm.teachSkill(4221007,0,10,-1);
		    cm.teachSkill(4220005,0,10,-1);
		    cm.sendNext("恭喜你轉職為 #b暗影神偷#k.我送你一些神秘小禮物^^");
	    } else {
		cm.sendOk("你沒有多的欄位請清空再來嘗試一次!");
		cm.safeDispose();
		return;
	    }
	}
	}
    } else if (status == 2) {
	cm.sendNextPrev("不要忘記了這一切都取決於你練了多少.");
    } else if (status == 3) {
	cm.dispose();
    }
}