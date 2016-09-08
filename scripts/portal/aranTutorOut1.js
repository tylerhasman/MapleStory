function enter() {
	if (pm.isQuestStarted(21000)) {
		//lol nexon does this xD
		pm.teachSkill(20000017, 0, -1);
		pm.teachSkill(20000018, 0, -1);
		//nexon sends updatePlayerStats MapleStat.AVAILABLESP 0
		pm.teachSkill(20000017, 1, 0);
		pm.teachSkill(20000018, 1, 0);
		//actually nexon does enableActions here :P
		pm.warp(914000200, 1);
		return true;
	} else {
		pm.sendMessage("PINK_TEXT", "You can only exit after you accept the quest from Athena Pierce, who is to your right.");
		return false;
	}
}