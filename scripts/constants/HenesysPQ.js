var seedBlue = 4001100;
var seedPink = 4001097;
var seedPurple = 4001096;
var seedYellow = 4001099;
var seedBrown = 4001098;
var seedGreen = 4001095;
var riceCake = 4001101;

var bunnyId = 9300061;

var henesysParkId = 100000200;
var exitMapSuccess = 910010300;
var PQMapId = 910010000;
var exitMapFail = 910010400;

var completionExpReward = 16000;

var MessageType = Java.type("constants.MessageType");
var MapleMonster = Java.type("maplestory.life.MapleMonster");

function onSeedPlant(reactor){
	var map = reactor.getMap();
	
	var seedsPlanted = map.getMetadata("seedsPlanted", 0);
	
	seedsPlanted++;
	map.setMetadata("seedsPlanted", seedsPlanted);
	
	if(seedsPlanted == 6){
		var moonReactor = map.getReactorByName("fullmoon");
		moonReactor.changeState(6);
		var bunny = map.spawnMonster(bunnyId, -183, -433);
		var dropPeriod = bunny.getStats().getDropPeriod() / 3;
		map.broadcastMessage(MessageType.PINK_TEXT, "Protect the moon bunny!!!");
		map.setMonsterSpawnsEnabled(true);
		map.scheduleRepeatingScriptTask("dropRiceCake", dropPeriod, dropPeriod, bunny);
	}
	
}

function dropRiceCake(bunny){
	if(bunny.isAlive()){
		var dropped = bunny.getMap().getMetadata("cakesDropped", 0)+1;
		bunny.getMap().setMetadata("cakesDropped", dropped);
		bunny.getMap().dropItem(riceCake, 1, bunny.getPosition(), bunny);
		bunny.getMap().broadcastMessage(MessageType.YELLOW_POPUP, "The moon bunny has dropped "+dropped+" rice cakes so far!");
	}
}