/*
 * @Name         艾德華
 * @Author:      Weber Tsai
 * @NPC:         9900001
 * @Purpose:     跟GM一起可以升級
 */
 

function start() {
    cm.sendYesNo("你想要升等嗎？跟GM拿取獎勵，再來找我吧！");
}

function action(i, am, pro) {
    if(cm.getPlayer().getParty() != null && cm.haveItem(5540000, 1)){
    cm.getPlayer().levelUp(true);
	cm.gainItem(5540000, -1);
    cm.dispose();
	} else if (cm.getPlayer().getParty() == null){
	cm.sendNext("你沒有組隊!!");	
	cm.dispose();
	return;
	} else {
	cm.sendNext("你沒有GM加持的東西！！ 找GM拿取，再來找我！");	
	cm.dispose();
	return;
	}
}