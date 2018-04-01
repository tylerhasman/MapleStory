function enter(){
	if(pm.getCharacter().getMapId() == 390009999){
		if(pm.getCharacter().getJob().isBeginnerJob()){
			pm.sendHint("Please finish #bGold Richie's#k quest first.", 250, 15);
		}else{
			pm.openNpc("pField_out_check", 9010000);
		}
	}
}