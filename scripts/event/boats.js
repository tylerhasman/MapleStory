
/**

	Generic script written for the boats in MS
	It won't work by itself. Another script will have to implement the following variables:
	
	first_port : MapleMap
	second_port : MapleMap
	
	- Contains lists of all the maps on the boats
	
	docked_first_boat : MapleMap[]
	docked_second_boat : MapleMap[]
	
	moving_first_boat : MapleMap[]
	moving_second_boat : MapleMap[]
	
	Some notes:
	If you want to add special events to your boats (eg. balrogs)
	You can listen for some special events by implementing the following methods:
	
	onBoatDock(port_map, players) -- All players will be moved to the dock before this is called
	onBoatLaunch(boat_maps, players) -- All players will be moved to the boat before this is called
	
	Written by Tyler
	
*/

var ArrayList = Java.type("java.util.ArrayList");

var STEP_FIRST_PORT_WAIT = 0;
var STEP_FIRST_PORT_OPEN = 1;
var STEP_FIRST_PORT_CLOSE = 2;
var STEP_TRAVEL_FROM_FIRST = 3;
var STEP_ARRIVE_SECOND_PORT = 4;
var STEP_SECOND_PORT_WAIT = 5;//From here on its basically a mirrored version
var STEP_SECOND_PORT_OPEN = 6;
var STEP_SECOND_PORT_CLOSE = 7;
var STEP_TRAVEL_FROM_SECOND = 8;
var STEP_ARRIVE_FIRST_PORT = 9;//This takes as back to the beginning

//var invasionTime = 180000; //The time that spawn balrog - 3 mins

var step = -1;

function onLoad(){
	step = STEP_FIRST_PORT_WAIT;
	update_maps();
	em.scheduleTask(function() { update(); }, get_step_time());
}

function warp_map(map, target){
	
	list = new ArrayList();
	
	for each(player in map.getPlayers()){
		player.changeMap(target);
		list.add(player);
	}
	return list;
}

function warp_maps(maps, target){
	list = new ArrayList();
	for each(map in maps){
		players = warp_map(map, target);
		list.addAll(players);
	}
	return list;
}

function update_boats_to_players(flag, players){
	for each(pl in players) {
		pl.updateBoat(flag);
	}
}

function update_boats_to_map(flag, map){
	update_boats_to_players(flag, map.getPlayers());
}

function update(){
	step++;
	
	if(step > STEP_ARRIVE_FIRST_PORT){
		step = 0;//RESET
	}
	
	update_maps();
	
	if(step == STEP_TRAVEL_FROM_FIRST){
		players = warp_maps(docked_first_boat, moving_first_boat[0]);	
		onBoatLaunch(moving_first_boat, players);	
	}else if(step == STEP_TRAVEL_FROM_SECOND){
		players = warp_maps(docked_second_boat, moving_second_boat[0]);
		onBoatLaunch(moving_second_boat, players);
	}else if(step == STEP_ARRIVE_SECOND_PORT){
		players = warp_maps(moving_first_boat, second_port);
		onBoatDock(second_port, players);
	}else if(step == STEP_ARRIVE_FIRST_PORT){
		players = warp_maps(moving_second_boat, first_port);
		onBoatDock(first_port, players);
	}
	
	em.scheduleTask(function() { update(); }, get_step_time());
}

/*function get_step_time(){
	switch(step){
		case STEP_SECOND_PORT_WAIT:
		case STEP_FIRST_PORT_WAIT:
			return 60000;//1 min
		case STEP_SECOND_PORT_OPEN:
		case STEP_FIRST_PORT_OPEN:
			return 780000;//13 mins
		case STEP_SECOND_PORT_CLOSE:
		case STEP_FIRST_PORT_CLOSE:
			return 60000;//1 min
		case STEP_TRAVEL_FROM_SECOND:
		case STEP_TRAVEL_FROM_FIRST:
			return 600000;//10 mins
		case STEP_ARRIVE_SECOND_PORT:
		case STEP_ARRIVE_FIRST_PORT:
			return 0;//Instantly execute and move to next step
	}
}*/


function get_step_time(){
	switch(step){
		case STEP_SECOND_PORT_WAIT:
		case STEP_FIRST_PORT_WAIT:
			return 60000 * 2;//1 min
		case STEP_SECOND_PORT_OPEN:
		case STEP_FIRST_PORT_OPEN:
			return 60000;//1 mins
		case STEP_SECOND_PORT_CLOSE:
		case STEP_FIRST_PORT_CLOSE:
			return 60000;//1 min
		case STEP_TRAVEL_FROM_SECOND:
		case STEP_TRAVEL_FROM_FIRST:
			return 60000;//1 mins
		case STEP_ARRIVE_SECOND_PORT:
		case STEP_ARRIVE_FIRST_PORT:
			return 0;//Instantly execute and move to next step
	}
}

function update_maps(){
	if(step == STEP_FIRST_PORT_WAIT || step == STEP_ARRIVE_FIRST_PORT){
		second_port.setMetadata("boat", false);
		first_port.setMetadata("boat", true);
		update_boats_to_map(true, first_port);
	}else if(step == STEP_TRAVEL_FROM_FIRST || step == STEP_TRAVEL_FROM_SECOND){
		second_port.setMetadata("boat", false);
		first_port.setMetadata("boat", false);
		update_boats_to_map(false, first_port);
		update_boats_to_map(false, second_port);
	}else if(step == STEP_ARRIVE_SECOND_PORT || step == STEP_SECOND_PORT_WAIT){
		second_port.setMetadata("boat", true);
		first_port.setMetadata("boat", false);
		update_boats_to_map(true, second_port);
	}else if(step == STEP_FIRST_PORT_CLOSE || step == STEP_SECOND_PORT_CLOSE){
		second_port.setMetadata("boat_closed", true);
		first_port.setMetadata("boat_closed", true);
	}else if(step == STEP_FIRST_PORT_OPEN || step == STEP_SECOND_PORT_OPEN){
		second_port.setMetadata("boat_closed", false);
		first_port.setMetadata("boat_closed", false);
	}
}

//Required to be an event
function get_event_name(){
	return "Boats";
}
