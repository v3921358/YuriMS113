var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)                           						
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple ("親愛的旅客#r#h ##k有何貴幹?\r\n\#r#L0##b天空之城#k\r\n#L1##g桃花仙境#k\r\n#L2##d靈藥幻境#k");
        } else if (status == 1) {
            switch(selection) {
         case 0:
		if(cm.getPlayer().getMeso() >= 1500) {
		cm.gainMeso(-1500);
		cm.warpBack(200090310,200000100,80);
		cm.dispose();
		}else {
		cm.sendOk("滾!你沒有足夠的楓幣");
		cm.dispose();
		 break;
	    }
		case 1:
		if(cm.getPlayer().getMeso() >= 1500) {
		cm.gainMeso(-1500);
		cm.warpBack(200903000,250000000,80);
		cm.dispose();
		}else {
		cm.sendOk("滾!你沒有足夠的楓幣");
		cm.dispose();
		 break;
	    }
		case 2:
		if(cm.getPlayer().getMeso() >= 1500) {
		cm.gainMeso(-1500);
		cm.warp(251000000);
		cm.dispose();
		}else {
		cm.sendOk("滾!你沒有足夠的楓幣");
		cm.dispose();
		 break;
	    }	
		}
		cm.dispose();
    }
}
}