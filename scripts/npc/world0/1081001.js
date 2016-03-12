/* Author: Xterminator
	NPC Name: 		Pison
	Map(s): 		Victoria Road : Lith Harbor (104000000)
	Description: 		Florina Beach Tour Guide
*/
var status = -1;
var returnmap ;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status <= 1) {
        cm.sendNext("你不回去 #m"+104000000+"# 那真是太棒了!\r\n看看我在這邊還不是過得好好，和你講話彷彿回到了以前呢!");
	    cm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    cm.sendSimple("所以你想離開 #b#m110000000##k? 如果你想我可以幫助你回到 #b#m"+104000000+"##k. 但是需要1500楓幣 r\n\r\n#L0##b 我願意付 1500 楓幣.#l");
    } else if (status == 1) {
	if (selection == 0) {
	    cm.sendYesNo("你確定你想回到 #b#m"+104000000+"##k? 好吧，我們得走快點了");
	}
    } else if (status == 2) {
	if (cm.getMeso() < 1500) {
	    cm.sendNext("你沒有足夠的楓幣滾吧!");
	    cm.dispose();
	} else {
	    cm.gainMeso(-1500);
	    returnmap = cm.getPlayer().getSavedLocation("FLORINA");
	    cm.warp(returnmap, 0);
		//cm.clearSavedLocation("FLORINA");
	    cm.dispose();
	}
}
}