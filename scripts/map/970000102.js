
var MonsterType = Java.type("maplestory.map.MapleMapObjectType");

function onPlayerEnter(chr){
	msm.getMap().createClock(15, "onClockEnd", chr);
}

function onClockEnd(chr){
	var returnMap = chr.getMap().getMetadata("returnMap", 100000000);
	
	chr.changeMap(returnMap);
	
	cm.openNpc(9201067);
}

function monstersLeft(map){
	return map.countObjectsOfType(MonsterType.MONSTER);
}

function onMonsterKilled(monster, killer){
	if(monstersLeft(monster.getMap()) == 0){
		monster.getMap().deleteClock();
		onClockEnd(killer);
	}
}