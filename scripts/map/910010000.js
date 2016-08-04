load("scripts/constants/HenesysPQ.js");


function onPlayerEnter(chr){
	map = chr.getMap();
	
	if(map.getReactorByName("fullmoon").getReactorData().getState() == 0){
		map.setMonsterSpawnsEnabled(false);
		for each (var obj in map.getObjects()){
			if(obj instanceof MapleMonster){
				obj.kill();
			}
		}
	}else{
		map.setMonsterSpawnsEnabled(true);
	}
	
	if(map.getClock() == null){
		map.createClock(10 * 60, "clockRunout");
	}
	
}

function clockRunout(){
	map = msm.getMap();
	
	for each(player in map.getPlayers()){
		player.changeMap(exitMapFail);
	}
}

function killAllClearMap(){
	map = msm.getMap();
	
	map.setMonsterSpawnsEnabled(false);
	for each (var obj in map.getObjects()){
		if(obj instanceof MapleMonster){
			obj.kill();
		}
	}
}

function initialize(){
	killAllClearMap();
}

function onMonsterKilled(monster, killer){
	if(monster.getId() == bunnyId){
		killAllClearMap();
		msm.getMap().broadcastMessage(MessageType.PINK_TEXT, "The moon bunny is feeling sick and has left!");
	}
}

function onReactorActivated(reactor){
	
	var id = reactor.getId();
	
	if(id >= 9108000 && id <= 9108005){
		onSeedPlant(reactor);
	}
	
}

function onPlayerExit(chr){
	
	party = chr.getParty();
	
	if(party != null){
		if(party.isLeader(chr)){
			removeAllPlayers(party);
		}else{
			if(msm.getPlayers().size() < 3){
				removeAllPlayers(party);
			}
		}
	}
	
}

function onPlayerLeaveParty(player, party){
	player.changeMap(exitMapFail);
	
	if(party.getMembers().size() < 3){
		removeAllPlayers(party);
	}
}

function removeAllPlayers(party){
	for each (member in party.getMembers()){
		if(member.getMapId() == PQMapId){
			member.changeMap(exitMapFail);
			member.sendMessage(MessageType.PINK_TEXT, "Either the party leader disbanded or there were less than 3 people in the map.");
		}
	}
}