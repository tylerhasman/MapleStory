
var MapleJob = Java.type("maplestory.player.MapleJob");

var status;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection){
	if(mode == -1){
		cm.dispose();
		return;
	}
	
	if(status == 0 && mode == 0){
		cm.dispose();
		return;
	}
	
	if(mode == 1){
		status++;
	}else{
		status--;
	}
	
	if(cm.getCharacter().getJob() == MapleJob.NOBLESSE){
		cygnusAction(mode, type, selection);
	}else{
		legendAction(mode, type, selection);
	}

}

function cygnusAction(mode, type, selection){ 

	if(status == 0){
		cm.sendSimple("Wait! You'll figure the stuff out by the time you reach Lv. 10 anyway, but if you absolutely want to prepare beforehand, you may view the following information.\r\n\r\n Tell me, what would you like to know?\r\n#b#L0#About you#l\r\n#L1#Mini Map#l\r\n#L2#Quest Window#l\r\n#L3#Inventory#l\r\n#L4#Regular Attack Hunting#l\r\n#L5#How to Pick Up Items#l\r\n#L6#How to Equip Items#l\r\n#L7#Skill Window#l\r\n#L8#How to Use Quick Slots#l\r\n#L9#How to Break Boxes#l\r\n#L10#How to Sit in a Chair#l\r\n#L11#World Map#l\r\n#L12#Quest Notifications#l\r\n#L13#Enhancing Stats#l\r\n#L14#Who are the Cygnus Knights?#l");
	}else if(status == 1){
		if(selection == 0){
			cm.sendOk("I serve under Shinsoo, the guardian of Empress Cygnus. My master, Shinsoo, has ordered me to guide everyone who comes to Maple World to join Cygnus Knights. I will be assisting and following you around until you become a Knight or reach Lv. 11. Please let me know if you have any questions.");
			cm.dispose();
		}else if(selection >= 1 && selection <= 13){
			cm.sendGuideHint(selection, 5000);
			cm.dispose();
		}else if(selection == 14){
			cm.sendOk("The Black Mage is trying to revive and conquer our peaceful Maple World. As a response to this threat, Empress Cygnus has formed a knighthood, now known as Cygnus Knights. You can become a Knight when you reach Lv. 10.");
			cm.dispose();
		}
	}

}

function legendAction(mode, type, selection){


}