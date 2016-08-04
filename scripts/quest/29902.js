var status = -1;

function start(mode, type, selection) {
		if (qm.forceStartQuest()) qm.showInfoText("You have earned the <Veteran Adventurer> title. You can receive a Medal from NPC Dalair.");
		qm.dispose();
}


function end(mode, type, selection) {
    status++;
    if (mode != 1) 
        qm.dispose();
    else {
        if (status == 0) 
            qm.sendNext("Congratulations on earning your honorable #b<Veteran Adventurer>#k title. I wish you the best of luck in your future endeavors! Keep up the good work.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n #v1142109:# #t1142109# 1");
        else if (status == 1) {
			if (qm.canHold(1142109)) {
				qm.giveItem(1142109, 1);
				qm.forceCompleteQuest();
			} else 
				qm.sendNext("Please make room in your inventory");//NOT GMS LIKE
			
			qm.dispose();        
		}
    }
	
}