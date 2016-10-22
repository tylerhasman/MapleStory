function enter(){
	if (!pm.isQuestStarted(21001)) {
		pi.warp(914000220, 2);
		return true;
	} else {
		pm.warp(914000500, 2);
		return true;
	}
}