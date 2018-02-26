var required = 4031047;

function start() {
    if(cm.hasItem(required)){
        if (is_open())
            cm.sendYesNo("Do you want to go to Ellinia?");
        else{
            cm.sendOk("The boat to Ellinia is already travelling, please be patient for the next one.");
            cm.dispose();
        }
    }else{
        cm.sendOk("Make sure you got a Ellinia ticket to travel in this boat. Check your inventory.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode <= 0) {
	cm.sendOk("Okay, talk to me if you change your mind!");
	cm.dispose();
	return;
    }
    if (is_open()) {
        cm.warp(200000112);
        cm.giveItem(required, -1);
        cm.dispose();
    }
    else{
        cm.sendOk("The boat to Ellinia is ready to take off, please be patient for the next one.");
        cm.dispose();
    }
}	

function is_open(){
	return !cm.getMap().getMetadata("boat_closed", false) && cm.getMap().getMetadata("boat", false);
}