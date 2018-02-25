

var status = -1;

function start(){
	action(1, 0, 0);
}

function action(mode, type, selection){
	if(mode == 1){
		status++;
	}else if(mode == 0){
		status--;
	}else{
		cm.dispose();
		return;
	}
	
	if(status < 0){
		cm.dispose();
		return;
	}
	
	job = cm.getCharacter().getJob().getId();
	
	if(status == 0){
		
		if(cm.hasItem(4031013, 30)){
			cm.sendOk("You collected #b30 #t4031013##k! Take this to #r"+getRelevantNpcName(job)+"#k and she will help you out!");
		}else{
			cm.sendYesNo("You haven't collected me #b30 #t4031013##k yet. Would you like to return back to your previous map? You can come back here anytime and try again");
		}
		
	}else if(status == 1){
		if(cm.hasItem(4031013, 30)){
			cm.giveItem(4031013, -99999);
			cm.giveItem(getRelevantItem(job), -1);
			cm.giveItem(4031012, 1);
			
			
		}
		back = cm.getCharacter().getSavedLocation("2nd_job");
		cm.warp(back == -1 ? 100000000 : back);
		cm.dispose();
	}
	
}

function getRelevantMap(job){
	if(job == 400){
		return 108000400;
	}
}

function getRelevantItem(job){
	if(job == 400){
		return 4031011;
	}
}

function getRelevantNpcName(job){
	if(job == 400){//Jeez
		return "#kthe #r#p"+getRelevantNpc(job)+"#";
	}
	return "#p"+getRelaventNpc(job)+"#";
}

function getRelevantNpc(job){
	if(job == 400){
		return 1052001;
	}
}

function isValidJob(job){
	return job == 100 || job == 200 || job == 300 || job == 400;
}