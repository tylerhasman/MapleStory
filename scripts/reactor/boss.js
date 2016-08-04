
var zakumReactor = 2111001;

function destroy() {

	reactor = rm.getReactor();
	pos = reactor.getPosition();
	pos.y -= 50;
	
	if(reactor.getId() == zakumReactor){
		
		rm.changeMusic("Bgm06/FinalFight");
		rm.spawnUntargetableMonster(8800000, pos);
		for(i = 0; i < 8;i++){
			rm.spawnMonster(8800003 + i, pos);
		}

	}

}