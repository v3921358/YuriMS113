/*
    楓之谷GM
    ID: 9000030
*/

var status = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}


function action(mode, type, selection) {
    
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        
        if(!cm.haveItem(5072000, 1)) {
            cm.sendNext("嗨我是SyncMS的GM，領廣播找我就對了!");
            cm.gainItem(5072000, 5, true);
        }
        else
            cm.sendNext("你身上還有廣播，用完再找我拿");

        
    } else {
        cm.sendSimple("祝您遊戲愉快");
        cm.dispose();
    }
}