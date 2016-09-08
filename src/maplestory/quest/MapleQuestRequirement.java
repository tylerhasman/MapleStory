package maplestory.quest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataTool;
import lombok.Getter;
import maplestory.inventory.InventoryType;
import maplestory.player.MapleCharacter;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.server.MapleStory;
import static maplestory.quest.MapleQuestRequirementType.*;

public interface MapleQuestRequirement {

	public boolean isConditionMet(MapleCharacter chr, int npc);
	
	public void processData(MapleData data);
	
	public static MapleQuestRequirement getFromType(MapleQuestRequirementType type, MapleQuest quest, MapleData data){
		
		MapleQuestRequirement req = null;
		
		if(type == JOB){
			req = new JobRequirement();
		}else if(type == ITEM){
			req = new ItemRequirement();
		}else if(type == COMPLETED_QUEST){
			req = new QuestMinRequirement();
		}else if(type == END_DATE){
			req = new EndDateRequirement();
		}else if(type == INFO_EX){
			req = new InfoExRequirement(quest);
		}else if(type == INTERVAL){
			req = new IntervalRequirement(quest);
		}else if(type == MAX_LEVEL){
			req = new MaxLevelRequirement();
		}else if(type == MIN_LEVEL){
			req = new MinLevelRequirement();
		}else if(type == MIN_PET_TAMENESS){
			req = new MinTamenessRequirement();
		}else if(type == MOB){
			req = new MobRequirement(quest.getId());
		}else if(type == MONSTER_BOOK){
			req = new MonsterBookCountRequirement();
		}else if(type == NPC){
			req = new NpcRequirement(quest);
		}else if(type == QUEST){
			req = new QuestCompletedRequirement();
		}
		
		if(req != null){
			req.processData(data);
		}
		
		return req;
	}
	
	public static class QuestCompletedRequirement implements MapleQuestRequirement {

		private Map<Integer, Integer> quests;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			for(int quest : quests.keySet()){
				int state = quests.get(quest);
				MapleQuestInstance inst = chr.getQuest(quest);
				
				if(MapleQuestStatus.getById(state) == MapleQuestStatus.NOT_STARTED){
					continue;
				}
				
				if(inst.getStatus() != MapleQuestStatus.getById(state)){
					return false;
				}
				
			}
			return true;
		}

		@Override
		public void processData(MapleData data) {
			quests = new HashMap<>();
			for(MapleData questEntry : data.getChildren()){
				int quest = MapleDataTool.getInt("id", questEntry);
				int state = MapleDataTool.getInt("state", questEntry);
				
				quests.put(quest, state);
			}
		}
		
	}
	
	public static class NpcRequirement implements MapleQuestRequirement {

		@Getter
		private int npcReq;
		private boolean autoComplete, autoStart;
		
		public NpcRequirement(MapleQuest quest) {
			MapleQuestInfo info = quest.getQuestInfo();
			autoComplete = info.isAutoCompleted();
			autoStart = info.isAutoStarted();
		}
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			
			if(npcReq == 0){
				return true;
			}
			
			if(npc == npcReq){
				if(autoComplete || autoStart || chr.getMap().containsNPC(npc)){
					return true;
				}
			}
			
			return false;
		}

		@Override
		public void processData(MapleData data) {
			npcReq = MapleDataTool.getInt(data);
		}
		
	}
	
	public static class MonsterBookCountRequirement implements MapleQuestRequirement {

		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			MapleStory.getLogger().warn("Monster Book not implemented!");
			return false;
		}

		@Override
		public void processData(MapleData data) {
			
		}
		
	}
	
	public static class MobRequirement implements MapleQuestRequirement {

		private Map<Integer, Integer> mobs;
		private int quest;
		
		public MobRequirement(int quest) {
			this.quest = quest;
		}
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			MapleQuestInstance inst = chr.getQuest(quest);
			for(int mob : mobs.keySet()){
				int count = mobs.get(mob);
				int progress = inst.getProgress(mob);
				
				if(progress < count){
					return false;
				}
			}
			return true;
		}

		@Override
		public void processData(MapleData data) {
			mobs = new HashMap<>();
			for(MapleData mobEntry : data.getChildren()){
				int mobId = MapleDataTool.getInt("id", mobEntry);
				int count = MapleDataTool.getInt("count", mobEntry);
				mobs.put(mobId, count);
			}
		}
		
	}
	
	public static class MinTamenessRequirement implements MapleQuestRequirement {

		private int min;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			//Pets not implemented...
			MapleStory.getLogger().warn("Pets not implemented!");
			return false;
		}

		@Override
		public void processData(MapleData data) {
			min = MapleDataTool.getInt(data);
		}
		
	}
	
	public static class MinLevelRequirement implements MapleQuestRequirement {

		private int minLevel;

		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			return chr.getLevel() >= minLevel;
		}

		@Override
		public void processData(MapleData data) {
			minLevel = MapleDataTool.getInt(data);
		}
		
	}
	
	public static class MaxLevelRequirement implements MapleQuestRequirement {
		private int maxLevel;

		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			return maxLevel >= chr.getLevel();
		}

		@Override
		public void processData(MapleData data) {
			maxLevel = MapleDataTool.getInt(data);
		}
		
	}
	
	public static class IntervalRequirement implements MapleQuestRequirement {

		private int interval;
		private int quest;
		
		public IntervalRequirement(MapleQuest quest) {
			this.quest = quest.getId();
		}
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			MapleQuestInstance inst = chr.getQuest(quest);
			if(inst.getStatus() != MapleQuestStatus.COMPLETED){
				return true;
			}
			if(inst.getCompletionTime() <= System.currentTimeMillis() - interval){
				return true;
			}
			return false;
		}

		@Override
		public void processData(MapleData data) {
			interval = MapleDataTool.getInt(data) * 60 * 1000;
		}
		
	}
	
	public static class InfoExRequirement implements MapleQuestRequirement {

		private List<String> expected;
		private int quest;

		public InfoExRequirement(MapleQuest quest) {
			this.quest = quest.getId();
		}
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			MapleQuestInstance inst = chr.getQuest(quest);
			
			return expected.contains(inst.getInfo());
		}

		@Override
		public void processData(MapleData data) {
			expected = new ArrayList<>();
			for(MapleData infoEx : data.getChildren()){
				MapleData value = infoEx.getChildByPath("value");
				expected.add(MapleDataTool.getString(value, ""));
			}
		}
		
	}
	
	public static class FieldEnterRequirement implements MapleQuestRequirement{

		private int mapId;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			return chr.getMapId() == mapId;
		}

		@Override
		public void processData(MapleData data) {
			mapId = MapleDataTool.getInt("0", data);
		}
		
		
	}
	
	public static class EndDateRequirement implements MapleQuestRequirement {

		private long timeInMillis;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			return timeInMillis >= System.currentTimeMillis();
		}

		@Override
		public void processData(MapleData data) {
			String timeStr = MapleDataTool.getString(data);
			
			int year, month, date, hour;
			year = Integer.parseInt(timeStr.substring(0, 4));
			month = Integer.parseInt(timeStr.substring(4, 6));
			date = Integer.parseInt(timeStr.substring(6, 8));
			hour = Integer.parseInt(timeStr.substring(8, 10));
			
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, date, hour, 0);
			
			timeInMillis = cal.getTimeInMillis();
		}
		
	}
	
	public static class QuestMinRequirement implements MapleQuestRequirement{

		private int questAmount;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			return chr.getQuests(MapleQuestStatus.COMPLETED).size() >= questAmount;
		}

		@Override
		public void processData(MapleData data) {
			questAmount = MapleDataTool.getInt(data);
		}
		
	}
	
	public static class ItemRequirement implements MapleQuestRequirement {

		private Map<Integer, Integer> items;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {

			for(int id : items.keySet()){
				if(!hasItem(chr, id, items.get(id))){
					return false;
				}
			}
			
			return true;
		}
		
		private boolean hasItem(MapleCharacter chr, int id, int amount){
			InventoryType invType = InventoryType.getByItemId(id);
			
			if(invType == InventoryType.EQUIP){
				if(chr.getInventory(InventoryType.EQUIPPED).countById(id) >= amount){
					return true;
				}
			}
			
			return chr.getInventory(invType).countById(id) >= amount;
		}

		@Override
		public void processData(MapleData data) {
			List<MapleData> children = data.getChildren();
			
			items = new HashMap<>();
			
			for(int i = 0; i < children.size();i++){
				MapleData child = children.get(i);
				
				int id = MapleDataTool.getInt("id", child);
				int count = MapleDataTool.getInt("count", child);
				
				items.put(id, count);
			}
		}
		
	}
	
	public static class JobRequirement implements MapleQuestRequirement {

		private int[] jobs;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			
			for(int i = 0; i < jobs.length;i++){
				if(chr.getJob().getId() == jobs[i]){
					return true;
				}
			}
			
			return false;
		}

		@Override
		public void processData(MapleData data) {
			List<MapleData> children = data.getChildren();
			
			jobs = new int[children.size()];
			
			for(int i = 0; i < children.size();i++){
				jobs[i] = MapleDataTool.getInt(children.get(i));
			}
		}
		
		
		
	}
	
}
