

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
		
		
		if(isValidJob(job)){
			if(cm.hasItem(getRelevantItem(job))){
				cm.sendNext("Oh, isn't that a letter from #r"+getRelevantNpcName(job)+"#k?");
			}else if(cm.hasItem(4031012, 1)){
			
				cm.sendOk("Well done");
				cm.dispose();
				
			}else{
				cm.sendOk("I can show you the way once your ready for it.");
				cm.dispose();
			}
		}else if(cm.getCharacter().getLevel() < 30){
			cm.sendOk("I can show you the way once your ready for it.");
			cm.dispose();
		}else{
			cm.sendOk("I've already taken you as far as I can.");
			cm.dispose();
		}
		
	}else if(status == 1){
		 cm.sendNextPrev("So you want to prove your skills? Very well...");
	}else if(status == 2){
		 cm.sendAcceptDecline("I will give you a chance if you're ready.");
	}else if(status == 3){
		cm.sendOk("You will have to collect me #b30 #t4031013##k. Good luck.");
		cm.getCharacter().saveLocation("2nd_job");
		cm.warp(getRelevantMap(job), 0);
		cm.dispose();
	}
	
}

function getRelevantMap(job){
	map = 108000000;
	
	return map + job;
}

function getRelevantItem(job){
	itemId = 4031008;
	jobId = job / 100 - 1;
	return itemId + jobId;
}

function getRelevantNpcName(job){
	if(job == 400){//Jeez
		return "#kthe #r#p"+getRelevantNpc(job)+"#";
	}
	return "#p"+getRelevantNpc(job)+"#";
}

function getRelevantNpc(job){
	if(job == 400){
		return 1052001;
	}else if(job == 300){
		return 1012100;	
	}else if(job == 200){
		return 1032001;
	}else if(job == 100){
		return 1022000;
	}
}

function isValidJob(job){
	return job == 100 || job == 200 || job == 300 || job == 400;
}