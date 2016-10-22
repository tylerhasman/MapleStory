var status = -1;

function start() {
	cm.getCharacter().enableUI();
	cm.getCharacter().unlockUI();
	action(1, 0, 0);	
}

function action(mode, type, selection) {  
	if(mode == -1){
		cm.dispose();
		return;
	}else if(mode == 1){
		status++;
	}else{
		status--;
	}
	
	if(cm.getCharacter().getMapId() == 140090000){
		if(status == 0){
			cm.sendNext("You've finally awoken...!", 8);
		}else if(status == 1){
			cm.sendNextPrev("And you are...?", 2);
		} else if (status == 2) {
			cm.sendNextPrev("The hero who fought against the Black Mage... I've been waiting for you to wake up!", 8);
		} else if (status == 3) {
			cm.sendNextPrev("Who... Who are you? And what are you talking about?", 2);
		} else if (status == 4) {
			cm.sendNextPrev("And who am I...? I can't remember anything... Ouch, my head hurts!", 2);
		} else if (status == 5) {
			cm.showIntro("Effect/Direction1.img/aranTutorial/face");
			cm.showIntro("Effect/Direction1.img/aranTutorial/ClickLilin");
			//cm.updateAreaInfo(21019, "helper=clear");
			cm.dispose();
		}
	}

}