/*	
	Author : kevintjuh93
*/
var status = -1;

function clearAranTutorialSkills(){
	startSkill = 20000014;
	for(i = 0; i < 5;i++){
		qm.teachSkill(startSkill + i, -1, -1);
	}
}

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 15 && mode == 0) {
            qm.sendNext("*Sob* Aran has declined my request!");
            qm.dispose();
            return;
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("*Sniff sniff* I was so scared... Please take me to Athena Pierce.");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.warp(914000300);
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            qm.sendNext("What about the child? Please give me the child!");
        }
        qm.dispose();
        return;
    }
    if (status == 0)
        qm.sendYesNo("You made it back safely! What about the child?! Did you bring the child with you?!");
    else if (status == 1) {
        qm.giveItem(4001271, -1);
        //qm.removeEquipFromSlot(-11);
        qm.forceCompleteQuest();
        qm.sendNext("Oh, what a relief. I'm so glad...", 9);
    } else if (status == 2)
        qm.sendNextPrev("Hurry and board the ship! We don't have much time!", 3);
    else if (status == 3)
        qm.sendNextPrev("We don't have any time to waste. The Black Mage's forces are getting closer and closer! We're doomed if we don't leave right right this moment!", 9);
    else if (status == 4)
        qm.sendNextPrev("Leave, now!", 3);
    else if (status == 5)
        qm.sendNextPrev("Aran, please! I know you want to stay and fight the Black Mage, but it's too late! Leave it to the others and come to Victoria Island with us!", 9);
    else if (status == 6)
        qm.sendNextPrev("No, I can't!", 3);
    else if (status == 7) {
        qm.sendNextPrev("Athena Pierce, why don't you leave for Victoria Island first? I promise I'll come for you later. I'll be alright. I must fight the Black Mage with the other heroes!", 3);
    } else if (status == 8) {
		clearAranTutorialSkills();
		qm.getCharacter().setMaxHp(50);
		qm.getCharacter().setMaxMp(50);
        qm.forceCompleteQuest();
        qm.warp(914090010); // Initialize Aran Tutorial Scenes
		
        qm.dispose();
    }
}