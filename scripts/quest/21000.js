var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendNext("No, Aran... We can't leave a kid behind. I know it's a lot to ask, but please reconsider. Please!");
		    qm.dispose();
			return;
		}
	}
	if (status == 0){
		qm.sendAcceptDecline("Oh, no! I think there's still a child in the forest! Aran, I'm very sorry, but could you rescue the child? I know you're injured, but I don't have anyone else to ask!");
	}else if (status == 1) {
		qm.forceStartQuest();
		qm.sendNext("#bThe child is probably lost deep inside the forest!#k We have to escape before the Black Mage finds us. You must rush into the forest and bring the child back with you!");
	} else if (status == 2) {
		qm.sendNextPrev("Don't panic, Aran. If you wish to check the status of the \r\nquest, press #bQ#k and view the Quest window.");
	} else if (status == 3) {
		qm.sendNextPrev("Please, Aran! I'm begging you. I can't bear to lose another person to the Black Mage!");
	} else if (status == 4) {
		qm.showEffect("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
		qm.dispose();
	}
}


function end(mode, type, selection) {
	qm.forceCompleteQuest();
	qm.dispose();
}