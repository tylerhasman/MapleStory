var status = -1;
var s_rate;

var RateType = Java.type("maplestory.world.RateManager.RateType");

var types = [RateType.EXP, RateType.MESO, RateType.DROP, RateType.QUEST];

function start(){
	action(1, 0, 0);
}

function action(mode, type, selection){

	if(mode == 1){
		status++;
	}else{
		status--;
	}
	
	if(status == 0){
		option = -1;
		msg = "Hi #r#h ##k,\r\nWhich rate would you like to set?\r\n";
		for(i = 0; i < types.length;i++){
			current = cm.getCharacter().getRate(types[i]);
			global = cm.getClient().getWorld().getRates().getGlobalRate(types[i]);
			msg += "#b#L"+i+"#"+types[i].name()+ " (current "+current+"x) (global "+global+"x) #l\r\n";
		}
		cm.sendSimple(msg);
	}else if(status == 1){
		
		rate = types[selection];
		s_rate = rate;
		
		cm.sendGetNumber("Enter new rate", cm.getCharacter().getRate(rate), 1, 9999);
	}else if(status == 2){
	
		cm.getCharacter().setRate(s_rate, selection);
		cm.sendOk("Your "+s_rate+" rate has been set to "+selection);
		cm.dispose();
	
	}else{
		cm.dispose();
	}

}
