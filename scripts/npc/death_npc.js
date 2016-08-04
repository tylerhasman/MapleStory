
var choice;
var status;

function start(){
	choice = -1;
	cm.sendSimple("What would you like to do?\r\n#b#L0#Revive at the closest town#l\r\n#L1#Revive here for 10% of your mesos and exp.#l");
	
}

function action(mode, type, selection){
	
	if(mode == -1){
		start();
		return;
	}
	
	if(selection >= 0){
			choice = selection;
			
			if(selection == 0){
				cm.getClient().getCharacter().reviveAtClosestTown();
				cm.dispose();
			}else if(selection == 1){
				
				meso_cost = parseInt(cm.getClient().getCharacter().getMeso() * 0.1);
				exp_cost = parseInt(cm.getClient().getCharacter().getExp() * 0.1);
				
				cm.sendYesNo("Are you sure you wish to revive here?\r\nIt will cost "+meso_cost+" mesos and "+exp_cost+" exp");
			}
	}else{
		if(choice == 1){
			if(mode == 1){
				cm.getClient().getCharacter().revive();
				cm.dispose();
			}else if(mode == 0){
				start();
			}
		}
	}
	
	if(type == 4 && selection == -1 && mode == 0){
		start();
	}
	
}