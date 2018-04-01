
var MapleJob = Java.type("maplestory.player.MapleJob");

var status = -1;

function start(){
	cm.sendYesNo("Are you ready to return to #bGold Richie#k?");
}

function action(mode, type, selection){
	if(mode != 1){
		cm.dispose();
		return;
	}
	
	cm.warp(390009999);
	cm.dispose();
	
}