function start() {
    if(cm.hasItem(4031045)){
        if (is_open())
            cm.sendYesNo("Do you want to go to Orbis?");
        else{
            cm.sendOk("The boat to Orbis is already travelling, please be patient for the next one.");
            cm.dispose();
        }
    }else{
        cm.sendOk("Make sure you got a Orbis ticket to travel in this boat. Check your inventory.");
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
        cm.warp(101000301);
        cm.giveItem(4031045, -1);
        cm.dispose();
    }
    else{
        cm.sendOk("The boat to Orbis is ready to take off, please be patient for the next one.");
        cm.dispose();
    }
}	

function is_open(){
	return !cm.getMap().getMetadata("boat_closed", false) && cm.getMap().getMetadata("boat", false);
}