function onPlayerEnter(chr){
	
	if(msm.getMap().getMetadata("boat", false)){
		chr.updateBoat(true);
	}
	
	
}