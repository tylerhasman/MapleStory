
var MessageType = Java.type("constants.MessageType");

var first_port = em.getChannel().getMap(101000300);
var second_port = em.getChannel().getMap(200000111);

var docked_first_boat = [em.getChannel().getMap(101000301), em.getChannel().getMap(200090001)];
var docked_second_boat = [em.getChannel().getMap(200000112), em.getChannel().getMap(200090001)];

var moving_first_boat = [em.getChannel().getMap(200090010), em.getChannel().getMap(200090011)];
var moving_second_boat = [em.getChannel().getMap(200090000), em.getChannel().getMap(200090001)];

function onBoatDock(port, players){
	for each(pl in players){
		pl.sendMessage(MessageType.PINK_TEXT, "We have arrived!");
	}
}

function onBoatLaunch(boat, players){
	for each(pl in players){
		pl.sendMessage(MessageType.PINK_TEXT, "The ship has launched!");
	}
}

load("scripts/event/boats.js");