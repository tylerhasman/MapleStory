
var status = -1;

function start(){
	action(1, 0, -1);
}

function action(mode, type, selection){
	if(mode == 1){
		status++;
	}else{
		cm.dispose();
		return;
	}
	if(status == 0){
		cm.sendGetNumber("How much EXP would you like?\r\n", 50, 1, 9999999);
	}else if(status == 1){
		exp = selection;
		
		if(exp <= 0){
			cm.dispose();
			return;
		}
		
		cm.giveExp(exp);
		cm.dispose();
	}
}