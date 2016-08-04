
var status;

function start(){
	status = -1;
	action(1, 0, -1);
}

function action(mode, type, selection){
	if(mode == 1){
		status++;
	}else{
		status--;
	}
	
	if(status == 0){
		cm.dispose();
	}
}