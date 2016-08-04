

function enter(){
	
	pm.playPortalSound();
	
	map = pm.getCharacter().getMapId();
	
	if(map == 130000000){
		pm.warp(130000100, 5);
	}else if(map == 130000200){
		pm.warp(130000100, 4); 
	}else if(map == 140010100){
		pm.warp(140010110, 0);
	}else if(map == 120000101){
		pm.warp(120000105, 0);
	}else if(map == 103000003){
		pm.warp(103000008, 0);
	}else if(map == 100000201){
		pm.warp(100000204, 0);
	}else{
		pm.warp(map + 1, 0);
	}
	

}