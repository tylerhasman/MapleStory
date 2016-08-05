package maplestory.quest;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import lombok.Getter;
import maplestory.player.MapleCharacter;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.script.MapleScript;
import maplestory.script.MapleScriptInstance;
import maplestory.script.QuestScriptManager;
import maplestory.server.MapleStory;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

public class MapleQuest {
	@Getter
	private final static MapleDataProvider questData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Quest.wz"));
	private final static Map<Integer, MapleQuest> cache = new HashMap<>();
	
	@Getter
	private MapleQuestInfo questInfo;
	
	@Getter
	private int id;
	
	private MapleQuest(int id, MapleQuestInfo questInfo){
		this.id = id;
		this.questInfo = questInfo;
	}
	
	public static int clearCache(){
		int size = cache.size();
		cache.clear();
		return size;
	}
	
	public boolean canStart(MapleCharacter chr, int npc){
		
		if(questInfo.isAutoStarted()){
			return true;
		}
		
		MapleQuestInstance inst = chr.getQuest(id);
		
		if(inst.getStatus() == MapleQuestStatus.COMPLETED){
			if(!questInfo.isRepeatable()){
				return false;
			}
		}
		
		if(inst.getStatus() != MapleQuestStatus.NOT_STARTED){
			return false;
		}
		
		return questInfo.canStart(chr, npc);
	}

	public boolean canComplete(MapleCharacter chr, int npc) {
		if(questInfo.isAutoPreCompleted()){
			return true;
		}
		MapleQuestInstance inst = chr.getQuest(id);
		
		if(inst.getStatus() != MapleQuestStatus.STARTED){
			return false;
		}
		
		return questInfo.canComplete(chr, npc);
	}
	
	public void start(MapleCharacter chr, int npc){
		for(MapleQuestAction action : questInfo.getStartActions()){
			if(action.isRunnableOn(chr, -1)){
				action.run(chr, -1);
			}
		}
		MapleQuestInstance old = chr.getQuest(id);
		MapleQuestInstance inst = new MapleQuestInstance(this, old.getForfeits(), npc, MapleQuestStatus.STARTED);
		
		if(questInfo.getTimeLimit() > 0){
			//time limit here
		}
		
		chr.updateQuest(inst);
		
	}
	
	public void complete(MapleCharacter chr, int npc){
		complete(chr, npc, -1);
	}
	
	public void complete(MapleCharacter chr, int npc, int selection){
		for(MapleQuestAction action : questInfo.getEndActions()){
			if(!action.isRunnableOn(chr, selection)){
				return;
			}
		}
		MapleQuestInstance inst = chr.getQuest(id);
		inst.complete();
		chr.updateQuest(inst);
		for(MapleQuestAction action : questInfo.getEndActions()){
			action.run(chr, selection);
		}
	}
	
	public void forfeit(MapleCharacter chr){
		MapleQuestInstance inst = chr.getQuest(id);
		if(inst.getStatus() != MapleQuestStatus.STARTED){
			return;
		}
		if(questInfo.getTimeLimit() > 0){
			//Timelimit
		}
		inst.forfeit();
		chr.updateQuest(inst);
	}
	
	public static MapleQuest getQuest(int id){
		MapleQuest quest = cache.get(id);
		if(quest == null){
			
			MapleData info = questData.getData("QuestInfo.img").getChildByPath(String.valueOf(id));
			MapleData acts = questData.getData("Act.img").getChildByPath(String.valueOf(id));
			MapleData reqs = questData.getData("Check.img").getChildByPath(String.valueOf(id));
			
			MapleQuestInfo questInfo = new MapleQuestInfo(quest, info, acts, reqs);
			quest = new MapleQuest(id, questInfo);
			
			if(info == null || acts == null || reqs == null){
				MapleStory.getLogger().warn("No data for quest "+id+" "+info+" "+acts+" "+reqs);
			}else{
				questInfo.loadWzData(acts, reqs, quest);
			}
			

			
			
			cache.put(id, quest);
		}
		
		return quest;
	}
	
}
