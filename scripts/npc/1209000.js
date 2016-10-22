var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
	if(cm.isQuestStarted(21000)){
		cm.sendOk("We can't leave without him!");
		cm.dispose();
		return;
	}
    if (mode == 1) {
        status++;
    } else {
        status--;
	}

	if (status == 0) {
		cm.sendNext("Aran, you're awake! How are you feeling? Hm? You want to know what's been going on?");
	} else if (status == 1) {
		cm.sendNext("We're almost done preparing for the escape. You don't have to worry. Everyone I could possibly find has boarded the ark, and Shinsoo has agreed to guide the way. We'll head to Victoria Island as soon as we finish the remaining preparations.");
	} else if (status == 2) {
		cm.sendNext("The other heroes? They've left to fight the Black Mage. They're buying us time to escape. What? You want to fight with them? No! You can't! You're hurt. You must leave with us!");
	}else{
		cm.dispose();
	}
} 