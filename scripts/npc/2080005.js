var status = -1;
var fm_map = 910000000;
var option = -1;
var job = null;

var options = ["Warp to the Free Market", "Job Advance", "Change Appearance", "Warp to a Map"];

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
		msg = "Hi #r#h ##k,\r\nWhat can I do for you?\r\n";
		for(i = 0; i < options.length;i++){
			msg += "#b#L"+i+"#"+options[i]+"#l\r\n";
		}
		cm.sendSimple(msg);
	}else if(status == 1){
		if(option != -1){
			status = -1;
			action(1, 0, 0);
		}else{
			option = selection;
			do_option(selection);
		}
	}else if(status == 2){
		if(option == 1){
			job = cm.handleJobAdvanceSelect(selection);
			
			if(job == null){
				status = -1;
				action(1, 0, 0);
			}else{
				cm.sendYesNo("Are you sure you wish to become a #b"+job.getName()+"#k?");
			}
		}
	}else if(status == 3){
	
		if(option == 1 && job != null){
			if(job.getLevelRequirement() <= cm.getClient().getCharacter().getLevel()){
				cm.getClient().getCharacter().changeJob(job);	
			}
			cm.dispose();
		}
	
	}else{
		cm.dispose();
	}

}

function do_option(selection){
	chr = cm.getClient().getCharacter();
	cl = cm.getClient();
	if(selection == 0){
	
		if(chr.getMapId() == fm_map){
			cm.sendPrev("You are already in the Free Market!");
		}else{
			chr.setFmReturnMap(chr.getMapId());
			chr.changeMap(fm_map);
			cm.dispose();
		}
	
	}else if(selection == 1){
		cm.sendJobAdvance();
	}else if(selection == 2){
		cm.openNpc("all_hair", 2080005);
	}else if(selection == 3){
		cm.openNpc("map_search", 2080005);
	}else{
		cm.dispose();
	}

}