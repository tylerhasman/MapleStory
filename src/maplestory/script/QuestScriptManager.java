package maplestory.script;

import maplestory.life.MapleLifeFactory;
import maplestory.player.MapleCharacter;
import maplestory.quest.MapleQuest;

public class QuestScriptManager extends NpcConversationManager {
	
	private int quest;
	
	public QuestScriptManager(MapleCharacter chr,  int quest, int npc) {
		super(chr, MapleLifeFactory.getNPC(npc));
		this.quest = quest;
	}
	
	public MapleQuest getQuest(){
		return MapleQuest.getQuest(quest);
	}
	
	public void forceStartQuest(){
		getQuest().start(getClient().getCharacter(), getNpc().getId());
	}
	
	public void forceCompleteQuest(){
		getQuest().complete(getClient().getCharacter(), getNpc().getId());
	}
	
	public void forceCompleteQuest(int questId){
		MapleQuest.getQuest(questId).complete(getClient().getCharacter(), getNpc().getId());
	}
	
	@Override
	public String toString() {
		return "QuestScriptManager {"+quest+"}";
	}

}
