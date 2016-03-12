/*
	NPC Name: 		Geras
	Map(s): 		Orbis: Station<To Ariant> (200000151)
	Description: 		Orbis Ticketing Usher
*/
var status = 0;

function start() {
    status = -1;
    geenie = cm.getEventManager("Geenie");
    action(1, 0, 0);
}

function action(mode, type, selection) {
    status++;
    if(mode == 0) {
	cm.sendNext("你有一些經濟的負擔而無法搭船對吧?");
	cm.dispose();
	return;
    }
    if (status == 0) {
	if(geenie == null) {
	    cm.sendNext("找不到此腳本請回報GM");
	    cm.dispose();
	} else if (geenie.getProperty("entry").equals("true")) {
        cm.sendYesNo("你要搭往納希沙漠的船?");
	} else if(geenie.getProperty("entry").equals("false") && geenie.getProperty("docked").equals("true")) {
	    cm.sendNext("很抱歉本班船準備起飛,乘坐時間表可以通過售票展台查看.");
	    cm.dispose();
	} else {
	    cm.sendNext("很抱歉本班船準備起飛,乘坐時間表可以通過售票展台查看.");
	    cm.dispose();
	}
    } else if(status == 1) {
	if(!cm.haveItem(4031576)) {
	    cm.sendNext("不! 你沒有#b#t4031576##k 所以我不能放你走!.");
	} else {
	    cm.gainItem(4031576, -1);
	    cm.warp(200000152, 0);
	}
	cm.dispose();
    }
}