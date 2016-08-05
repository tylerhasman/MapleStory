/**

This script will not function on it's own.

You must first load it into another script after creating a variable like so:

var medal_id = [medal item id];

*/
var status = -1;

function start(mode, type, selection){
	qm.forceStartQuest();
	qm.getClient().getCharacter().showInfoText("You have earned the <#t"+medal_id+"#> title. You can receive a Medal from NPC Dalair.");
	qm.dispose();
}

function end(mode, type, selection){
	status++;
	if(mode != 1){
		qm.dispose();
	}else if(status == 0){
		qm.sendNext("Congratulations on earning your honorable #b<#t"+medal_id+"#>#k title. I wish you the best of luck in your future endeavors! Keep up the good work.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n #v"+medal_id+":# #t"+medal_id+"# 1");
	}else if(status == 1){
		if(qm.canHold(medal_id)){
			qm.giveItem(medal_id, 1);
			qm.forceCompleteQuest();
		}else{
			qm.sendOk("Please make room in your EQUIP inventory");
		}
		
		qm.dispose();
	}
}