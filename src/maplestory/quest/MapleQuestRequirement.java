package maplestory.quest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import maplestory.inventory.InventoryType;
import maplestory.player.MapleCharacter;
import maplestory.player.MaplePetInstance;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.server.MapleStory;
import me.tyler.mdf.Node;
import static maplestory.quest.MapleQuestRequirementType.*;

public interface MapleQuestRequirement {

	public boolean isConditionMet(MapleCharacter chr, int npc);
	
	public void processData(Node data);
	
	public static MapleQuestRequirement getFromType(MapleQuestRequirementType type, MapleQuest quest, Node data){
		
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
		public void processData(Node data) {
			quests = new HashMap<>();
			for(Node questEntry : data){
				int quest = questEntry.readInt("id");
				int state = questEntry.readInt("state");
				
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
		public void processData(Node data) {
			npcReq = data.intValue();
		}
		
	}
	
	public static class MonsterBookCountRequirement implements MapleQuestRequirement {

		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			MapleStory.getLogger().warn("Monster Book not implemented!");
			return false;
		}

		@Override
		public void processData(Node data) {
			
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
		public void processData(Node data) {
			mobs = new HashMap<>();
			for(Node mobEntry : data.getChildren()){
				int mobId = mobEntry.readInt("id");
				int count = mobEntry.readInt("count");
				mobs.put(mobId, count);
			}
		}
		
	}
	
	public static class MinTamenessRequirement implements MapleQuestRequirement {

		private int min;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			
			int bestCandidate = 0;
			
			for(MaplePetInstance pet : chr.getPets()){
				if(pet != null && pet.getSource().getCloseness() > bestCandidate){
					bestCandidate = pet.getSource().getCloseness();
				}
			}
			
			return bestCandidate >= min;
		}

		@Override
		public void processData(Node data) {
			min = data.intValue();
		}
		
	}
	
	public static class MinLevelRequirement implements MapleQuestRequirement {

		private int minLevel;

		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			return chr.getLevel() >= minLevel;
		}

		@Override
		public void processData(Node data) {
			minLevel = data.intValue();
		}
		
	}
	
	public static class MaxLevelRequirement implements MapleQuestRequirement {
		private int maxLevel;

		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			return maxLevel >= chr.getLevel();
		}

		@Override
		public void processData(Node data) {
			maxLevel = data.intValue();
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
		public void processData(Node data) {
			interval = data.intValue() * 60 * 1000;
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
		public void processData(Node data) {
			expected = new ArrayList<>();
			for(Node infoEx : data.getChildren()){
				expected.add(infoEx.readString("value", ""));
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
		public void processData(Node data) {
			mapId = data.readInt("0");
		}
		
		
	}
	
	public static class EndDateRequirement implements MapleQuestRequirement {

		private long timeInMillis;
		
		@Override
		public boolean isConditionMet(MapleCharacter chr, int npc) {
			return timeInMillis >= System.currentTimeMillis();
		}

		@Override
		public void processData(Node data) {
			String timeStr = data.stringValue();
			
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
		public void processData(Node data) {
			questAmount = data.intValue();
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
		public void processData(Node data) {
			Collection<Node> children = data.getChildren();
			
			items = new HashMap<>();
			
			for(Node child : children){
				int id = child.readInt("id");
				int count = child.readInt("count");
				
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
		public void processData(Node data) {
			Collection<Node> children = data.getChildren();
			
			jobs = new int[children.size()];
			
			Iterator<Node> iterator = children.iterator();
			
			for(int i = 0; iterator.hasNext();i++){
				jobs[i] = iterator.next().intValue();
			}
		}
		
		
		
	}
	
}
