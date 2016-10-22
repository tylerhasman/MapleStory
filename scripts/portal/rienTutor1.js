
function enter(){
	
	if(pm.isQuestCompleted(21010)){
		pm.warp(140090200, 1);
	}else if(pm.isQuestStarted(21010)){
		if(pm.hasItem(2000022, 1)){
			pm.guideTalk("Drink the potion Puka gave you!");
		}else{
			pm.endQuestScript(21010, 1202001);
		}
	}else{
		pm.startQuestScript(21010, 1202001);
	}
	
}