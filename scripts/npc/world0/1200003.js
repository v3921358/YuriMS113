/* Dawnveil
    To Rien
	Puro
    Made by Daenerys
*/
function start() {
    cm.sendYesNo("你想要去維多利亞港必須支付#b 80 楓幣#k 到那邊約一分鐘.");
}

function action(mode, type, selection) {
    if (mode == 0) {
	cm.sendOk("等你考慮好再來找我吧!");
	cm.dispose();
	} else {
    if(cm.getPlayer().getMeso() >= 80) {
	cm.gainMeso(-80);
	cm.warpBack(200090070,104000000,80);
    }
    cm.dispose();
}
}