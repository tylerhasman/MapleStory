
function onPlayerEnter(chr){
	chr.sendHint("When you're ready to leave, talk to the #bGold Compass#k", 300, 300);
}

var Random = Java.type("java.util.Random");

var myRandom = new Random();

function onMonsterKilled(monster, killer){
	
	if(killer.getLevel() >= 10){
		return;
	}
	
	mesos = myRandom.nextInt(30) + 50;
	exp = killer.getLevel() * 5 + 10;
	
	monster.addDeathListener(function (time) {
			msm.dropMesos(monster, monster.getPosition(), mesos);
			killer.giveExp(exp);
		});
	
}