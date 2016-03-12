function enter(pi) {
	if (pi.isQuestStarted(2073)) {
		pi.warp(900000000,0);
		return true;
	} else {
		pi.playerMessage(5,"似乎有很強的力量阻擋著...");
		return false;
	}
}