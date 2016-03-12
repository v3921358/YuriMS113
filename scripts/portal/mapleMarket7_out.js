function enter(pi) {
    var map = pi.getPlayer().getSavedLocation("EVENT");
    if( map == undefined )
        map = 100000000;
    pi.warp(map);
    return true;
}