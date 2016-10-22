
function onPlayerEnter(chr){
	
	if(chr.getMaxHp() < 30000){
		chr.setMaxHp(30000);
		chr.setMaxMp(30000);
		chr.setHp(30000);
		chr.setMp(30000);
	}
	
}