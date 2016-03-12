/*
	Slyn - Before Takeoff To Orbis(260000110)
*/

var gm;

function start() {
    status = -1;
    gm = cm.getEventManager("Geenie");
    action(1, 0, 0);
}

function action(mode, type, selection) {
    status++;
    if(mode == 0) {
	cm.sendOk("你會得到在那一刻你的目的地。來吧，跟其他的人，你知道它之前，你會在那裡已經.");
	cm.dispose();
	return;
    }
    if(status == 0) {
	cm.sendYesNo("你想要離開等候室...\r\n但是船票不會退還 是否要離開房間呢??");
    } else if(status == 1) {
	cm.warp(260000100);
	cm.dispose();
    }
}
