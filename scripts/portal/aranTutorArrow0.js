function enter() { 

	if(pm.isQuestStarted(21000) || pm.isQuestCompleted(21000)){
		pm.block();
		return;
	}

	pm.showEffect("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");      

	pm.block();
	return true; 
} 