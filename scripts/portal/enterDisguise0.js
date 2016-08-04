

function enter() {
	if(pm.isQuestStarted(20301) || pm.isQuestStarted(20302) || pm.isQuestStarted(20303) || pm.isQuestStarted(20304) || pm.isQuestStarted(20305)) {
		if(pm.hasItem(4032179)) {
			pm.playPortalSound();
			pm.warp(130010000, "east00");
		} else {
			pm.getPlayer().sendMessage("PINK_TEXT", "Due to the lock down you can not enter without a permit.");
		}
	} else {
		pm.playPortalSound();
		pm.warp(130010000, "east00");
	}
}