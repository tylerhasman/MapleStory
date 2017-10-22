
var ItemInfoProvider = Java.type("maplestory.inventory.item.ItemInfoProvider");

var ItemFactory = Java.type("maplestory.inventory.item.ItemFactory");

var MapleMonster = Java.type("maplestory.life.MapleMonster");

var ticket = 5220001;
var map = 970000102;

var status;

function start(){
	status = -1;
	action(1, 0, -1);
}

function getMapInstance(){
	return cm.getClient().getChannel().getMapFactory().getMap(map);
}

function isAvailable(){
	return getMapInstance().getPlayers().size() == 0;
}

function action(mode, type, selection){
	
	if(mode == 1){
		status++;
	}else{
		cm.dispose();
		return;
	}
	if(status == 0){
		
		if(cm.getCharacter().getMapId() == 970000102){
			cm.sendOk("Looks like time is up! I hope you grabbed everything while you could.");
			cm.dispose();
			return;
		}
		
		cm.sendNext("Hello I am a magic claw machine but I don't work for free.");
	}else if(status == 1){
		if(cm.hasItem(ticket, 1)){
			cm.sendYesNo("Would you like to play? It'll cost one #i"+ticket+" #");
		}else{
			cm.sendOk("You need a #i"+ticket+" # to play");
			cm.dispose();
		}
	}else if(status == 2){

		if(!isAvailable()){
			cm.sendOk("It looks like I am currently in use right now, please check back later.");
			cm.dispose();
			return;
		}
	
		if(cm.hasItem(ticket, 1)){
			cm.giveItem(ticket, -1);
			
			getMapInstance().setMetadata("returnMap", cm.getCharacter().getMapId());
			
			cm.warp(map);
			
			for each (var obj in getMapInstance().getObjects()){
				getMapInstance().removeObject(obj.getObjectId());
			}
			
			for(i = 0; i < 5;i++){
				
				toy = getMapInstance().spawnMonster(3230305, 16, 102);
				
				toy.addCustomDrop(ItemInfoProvider.getRandomGachaponItem(""));//Global gacha pool
				
			}
			
			cm.sendOk("Woosh! Collect all the toys while you can!");
			
			cm.dispose();
			
		}else{
			cm.dispose();
		}
		
	}
	
}