
var Random = Java.type("java.util.Random");

var myRandom = new Random();

var luck = 3;

function onMonsterKilled(monster, killer){
	
	if(myRandom.nextInt(luck--) == 0){//this will guarantee a pearl every 3 monsters, I like that :)
		luck = 3;
		monster.addDeathListener(function (time) {
			monster.getMap().dropItem(4031013, 1, monster.getPosition(), monster);
		});
	}
	
}