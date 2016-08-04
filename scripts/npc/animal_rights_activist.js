
var status = -1;
var oldMap = 0;

function start(){
	oldMap = cm.getCharacter().getMapId();
	action(1, 0, -1);
}

function action(mode, type, selection){
	if(mode == 1){
		status++;
	}else{
		cm.dispose();
		return;
	}
	
	if(status == 0){
		cm.sendYesNo("Hello I am an animal rights activist. Are you sure you would like to kill everything on the map?");
	}else if(status == 1){
		cm.sendYesNo("Are you sure? These animals are wonderful creatures.");
	}else if(status == 2){
		cm.sendSimple("You are a heartless bastard.\r\n#b#L0#Fuck you Bruce#l");
	}else if(status == 3){
		cm.warp(0);
		cm.sendSimple("You need to chill out. Why not hang around here?\r\n#b#L0#No fuck you, take me back#l");
	}else if(status == 4){
		cm.warp(oldMap);
		cm.sendNext("Wow fine, I see there is no getting through to you. Ill kill the monsters.");
	}else{
		for each(monster in cm.getCharacter().getMap().getMonsters()){
			monster.kill(cm.getCharacter());
		}
		cm.dispose();
	}
	

}