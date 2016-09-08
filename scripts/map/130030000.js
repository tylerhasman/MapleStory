
var MapleQuest = Java.type("maplestory.quest.MapleQuest");
var MapleQuestInstance = Java.type("maplestory.quest.MapleQuestInstance");

function onPlayerEnter(chr){
	
	instance = chr.getQuest(21000);
	instance.setStatus(0);
	instance.setProgress(0, 1);
	chr.updateQuest(instance);
	
}