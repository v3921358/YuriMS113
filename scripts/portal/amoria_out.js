function enter(pi) {
    var map = pi.getPlayer().getSavedLocation("AMORIA");
    pi.warp(map);
    return true;
}