

var status = -1;

function start(mode, type, selection, end){

	if(!end){
		qm.forceStartQuest();
		qm.getClient().getCharacter().showInfoText("You have earned the <Beginner Adventurer> title. You can receive a Medal from NPC Dalair.");
		qm.dispose();
	}else{
		status++;
		if(mode != 1){
			qm.dispose();
		}else if(status == 0){
			qm.sendNext("Congratulations on earning your honorable #b<Beginner Adventurer>#k title. I wish you the best of luck in your future endeavors! Keep up the good work.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n #v1142107:# #t1142107# 1");
		}else if(status == 1){
			if(qm.canHold(1142107)){
				qm.giveItem(1142107, 1);
				qm.forceCompleteQuest();
			}else{
				qm.sendOk("Please make room in your EQUIP inventory");
			}
			
			qm.dispose();
		}
	}
	
}