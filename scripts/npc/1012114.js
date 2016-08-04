
load("scripts/constants/HenesysPQ.js");

var status = 0;
var choice = -1;

var PacketFactory = Java.type("maplestory.server.net.PacketFactory");

function start(){
	party = cm.getCharacter().getParty();
	if(party == null){
		return;
	}
	
	if(party.isLeader(cm.getCharacter())){
		if(cm.getCharacter().getMap().getMetadata("clear", "0").equals("1")){
			cm.sendNext("Mmmm ... this is delicious. Please come see me next time for more #b#t4001101##k. Have a safe trip home!");
			complete();
		}else{
			cm.sendSimple("Growl! I am Growlie, always ready to protect this place. What brought you here?\r\n#b#L0# Please tell me what this place is all about.#l\r\n#L1# I have brought #t4001101#.#l\r\n#L2# I would like to leave this place.#l");
		}
	}else{
		cm.sendSimple("Growl! I am Growlie, always ready to protect this place. What brought you here?\r\n#b#L0# Please tell me what this place is all about.#l\r\n#L2# I would like to leave this place.#l");
	}
	
}

function clearMap(map){
	map.setMonsterSpawnsEnabled(false);
	for each (var obj in map.getObjects()){
		if(obj instanceof MapleMonster){
			if(obj.getId() == 9300061){
				continue;
			}
			obj.kill();
		}
	}
}

function complete(){
	cm.giveItem(4001101, -10);
	for each(member in cm.getCharacter().getParty()){
		snapshot = member.getShapshot();
		if(snapshot.isOnline() && snapshot.getMapId() == PQMapId){
			snapshot.getLiveCharacter().giveExp(completionExpReward);
			snapshot.getLiveCharacter().changeMap(exitMapSuccess);
		}
	}
	
	cm.getCharacter().getMap().setMetadata("clear", "1");
	clearMap(cm.getCharacter().getMap());
	cm.getCharacter().getMap().broadcastPacket(PacketFactory.showEffect("quest/party/clear"));
	cm.getCharacter().getMap().broadcastPacket(PacketFactory.playSound("Party1/Clear"));
	cm.dispose();
}


function action(mode, type, selection){
	
	if(mode == -1){
		cm.dispose();
		return;
	}
	
	if(mode == 0 && status == 0){
		cm.dispose();
		return;
	}
	
	if(mode > 0){
		status++;
	}else{
		status--;
	}
	
	if(status == 1){
		if(choice == -1)
			choice = selection;
		if(choice == 0){
			cm.sendNext("This place can be best described as the prime spot where you can taste the delicious rice cakes made by Moon Bunny every full moon.");
		}else if(choice == 1){
			if(cm.itemAmount(4001101) >= 10){
				cm.sendNext("Oh... isn't this rice cake made by Moon Bunny? Please hand me the rice cake.");
			}else{
				cm.sendOk("I advise you to check and make sure that you have indeed gathered up #b10 #t4001101#s#k.");
				cm.dispose();
			}
		}else if(choice == 2){
			cm.sendYesNo("Are you sure you want to leave?");
		}
	}else if(status == 2){
		if(choice == 0){
			cm.sendNextPrev("Gather up the primrose seeds from the primrose leaves all over this area, and plant the seeds at the footing near the crescent moon to see the primrose bloom. There are 6 types of primroses, and all of them require different footings. It is imperative that the footing fits the seed of the flower.");
		}else if(choice == 1){
			cm.sendNext("Mmmm ... this is delicious. Please come see me next time for more #b#t4001101##k. Have a safe trip home!");
			complete();
		}else if(choice == 2){
			if(mode == 1){
				cm.getCharacter().leaveParty();
			}else{
				cm.sendOk("You better collect some delicious rice cakes for me then, because time is running out, Growl !");
			}
			cm.dispose();
		}
	}else if(status == 3){
		if(choice == 0){
			cm.sendNextPrev("When the flowers of primrose blooms, the full moon will rise, and that's when the Moon Bunnies will appear and start pounding the mill. Your task is to fight off the monsters to make sure that Moon Bunny can concentrate on making the best rice cake possible.");
		}else{
			cm.dispose();
		}
	}else if(status == 4){
		if(choice == 0){
			cm.sendNextPrev("I would like for you and your party members to cooperate and get me 10 rice cakes. I strongly advise you to get me the rice cakes within the allotted time.");
		}else{
			cm.dispose();
		}
	}else{
		for each(member in cm.getCharacter().getParty()){
			snapshot = member.getShapshot();
			if(snapshot.isOnline() && snapshot.getMapId() == PQMapId){
				snapshot.getLiveCharacter().changeMap(exitMapSuccess);
			}
		}
		cm.dispose();
	}
	
}