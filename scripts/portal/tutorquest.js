
function enter() {
	if(pm.getCharacter().getMapId() == 130030001){
		if(pm.isQuestStarted(20010)){
			pm.warp(130030002);
		}else{
			pm.guideTalk("Talk to #bKimu#k before leaving!");
		}
	}else if(pm.getCharacter().getMapId() == 130030002){
		if(pm.isQuestCompleted(20011)){
			pm.warp(130030003);
		}else{
			pm.guideTalk("Talk to #bKizan#k before leaving!");
		}
	}else if(pm.getCharacter().getMapId() == 130030003){
		if(pm.isQuestCompleted(20012)){
			pm.warp(130030004);
		}else{
			pm.guideTalk("Talk to #bKinu#k before leaving!");
		}
	}else if(pm.getCharacter().getMapId() == 130030004){
		if(pm.isQuestCompleted(20013)){
			pm.warp(130030005);
		}else{
			pm.guideTalk("Talk to #bKia#k before leaving!");
		}
	}
}