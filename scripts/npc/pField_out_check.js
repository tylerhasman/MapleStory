

function start(){
	cm.sendYesNo("Are you sure you want to leave this place? Once you leave you won't be able to return.");
}

function action(mode, type, selection){
	cm.sendOk(mode+" "+type+" "+selection);
	
	cm.dispose();
}