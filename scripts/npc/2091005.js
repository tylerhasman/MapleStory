
var DOJO_MAP = 925020001;

var BELTS = [1132000, 1132001, 1132002, 1132003, 1132004];
var BELT_LEVELS = [25, 35, 45, 60, 75];

var status = -1;

var noMessage = "";

function start(){
	action(1, 0, 0);
}

function action(mode, type, selection){
	if(mode == -1){
		cm.dispose();
		return;
	}else if(mode == 0){
	
		if(noMessage.length() > 0){
			cm.sendNext(noMessage);
		}
	
		cm.dispose();
		return;
	}else{
		status++;
	}
	
	if(cm.getCharacter().getMapId() == DOJO_MAP){
		
		if(status == 0){
			cm.sendSimple("My master is the strongest person in Mu Lung, and you want to challenge him? Fine, but you'll regret it later.\r\n\r\n#b#L0#I want to challenge him alone.#l\r\n#L1#I want to challenge him with a party.#l\r\n\r\n#L2#I want to receive a belt.#l\r\n#L3#I want to reset my training points.#l\r\n#L4#I want to receive a medal.#l\r\n#L5#What is a Mu Lung Dojo?#l");
		}else{
			if(selection == 0){
				if(hasCompletedTutorial(cm.getCharacter())){
				
		}else{
			if(status == 1){
				cm.sendYesNo("Hey there! You! This is your first time, huh? Well, my master doesn't just meet with anyone. He's a busy man. And judging by your looks, I don't think he'd bother. Ha! But, today's your lucky day... I tell you what, if you can defeat me, I'll allow you to see my Master. So what do you say?");
				noMessage = "Haha! Who are you trying to impress with a heart like that?\r\nGo back home where you belong!";
			}else if(status == 2){
				cm.sendNext("No tutorial ready yet...");
				cm.dispose();
			}
			
		}
			}
		}
		
		
	}
}

function getAvailableDojo(){
	
}

function hasCompletedTutorial(pl){
	pq = pl.getPartyQuestProgress();
	
	return pq.hasPartyQuestItem("dojo_tutorial");
}

function isRestingSpot(id) {
    return (Math.floor(id / 100) % 100) % 6 == 0 && id != 925020001;
}