
var balloons = 1102215;
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
		if(cm.hasItem(balloons)){
			cm.sendOk("Do you like the balloons I gave you?");
			cm.dispose();
		}else{
			cm.sendYesNo("Hey, hows it going? I have some extra balloons, would you like some?");
		}
	}else{
		cm.giveItem(balloons, 1);
	}
	

}