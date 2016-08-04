package maplestory.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.MessageType;
import constants.ServerConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import maplestory.inventory.InventoryType;
import maplestory.inventory.Inventory.RemoveItemResult;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;
import maplestory.util.Randomizer;
import provider.MapleData;
import provider.MapleDataTool;

public interface MapleQuestAction {

	public void processData(MapleData data);
	
	public boolean isRunnableOn(MapleCharacter chr, int selection);
	
	public void run(MapleCharacter chr, int selection);

	public static MapleQuestAction getFromType(MapleQuest quest, MapleQuestActionType type, MapleData child){
		MapleQuestAction action = null;
		
		if(type == MapleQuestActionType.BUFF){
			action = new BuffAction();
		}else if(type == MapleQuestActionType.EXP){
			action = new ExpAction();
		}else if(type == MapleQuestActionType.FAME){
			action = new FameAction();
		}else if(type == MapleQuestActionType.ITEM){
			action = new ItemAction();
		}else if(type == MapleQuestActionType.MESO){
			action = new MesoAction();
		}else if(type == MapleQuestActionType.NEXTQUEST){
			action = new NextQuestAction(quest.getId());
		}else if(type == MapleQuestActionType.PETSKILL){
			//TODO: pets
		}else if(type == MapleQuestActionType.QUEST){
			action = new ChangeQuestStateAction();
		}else if(type == MapleQuestActionType.SKILL){
			action = new SkillAction();
		}
		
		if(action != null){
			action.processData(child);	
		}
		
		return action;
	}
	
	//TODO: Pet actions
	
	public static class SkillAction implements MapleQuestAction {

		private Map<Integer, SkillActionData> skills;
		
		@Override
		public void processData(MapleData data) {
			skills = new HashMap<>();
			for(MapleData entry : data.getChildren()){
				int id = MapleDataTool.getInt("id", entry);
				int level = MapleDataTool.getInt("skillLevel", entry);
				int masterLevel = MapleDataTool.getInt("masterLevel", entry);
				
				List<Integer> jobs = new ArrayList<>();
				MapleData applicable = entry.getChildByPath("job");
				if(applicable != null){
					for(MapleData applicableEntry : applicable.getChildren()){
						jobs.add(MapleDataTool.getInt(applicableEntry));
					}
				}
				
				skills.put(id, new SkillActionData(id, level, masterLevel, jobs));
			}
		}

		@Override
		public boolean isRunnableOn(MapleCharacter chr, int selection) {
			return true;
		}

		@Override
		public void run(MapleCharacter chr, int selection) {
			for(SkillActionData data : skills.values()){
				Skill skill = SkillFactory.getSkill(data.getId());
				
				boolean shouldLearn = false;
				
				if(data.getJobs().contains(chr.getJob().getId()) || skill.isBeginnerSkill()){
					shouldLearn = true;
				}
				
				if(shouldLearn){
					int level = Math.max(data.getLevel(), chr.getSkillLevel(skill));
					int masterLevel = Math.max(data.getMasterLevel(), chr.getMasterLevel(skill));
					chr.changeSkillLevel(skill, level, masterLevel);
				}
				
			}
		}
		
		@AllArgsConstructor
		@Data
		private static class SkillActionData {
			private int id, level, masterLevel;
			private List<Integer> jobs;
		}
		
	}
	
	public static class ChangeQuestStateAction implements MapleQuestAction {

		private Map<Integer, Integer> quests;
		
		@Override
		public void processData(MapleData data) {
			quests = new HashMap<>();
			for(MapleData entry : data.getChildren()){
				int id = MapleDataTool.getInt("id", entry);
				int state = MapleDataTool.getInt("state", entry);
				quests.put(id, state);
			}
		}

		@Override
		public boolean isRunnableOn(MapleCharacter chr, int selection) {
			return true;
		}

		@Override
		public void run(MapleCharacter chr, int selection) {
			for(int qid : quests.keySet()){
				int state = quests.get(qid);
				MapleQuestInstance inst = chr.getQuest(qid);
				inst.setStatus(state);
				chr.updateQuest(inst);
			}
		}
		
	}
	
	public static class NextQuestAction implements MapleQuestAction {

		private int quest;
		private int current;
		
		public NextQuestAction(int current) {
			this.current = current;
		}
		
		
		@Override
		public void processData(MapleData data) {
			quest = MapleDataTool.getInt(data);
		}

		@Override
		public boolean isRunnableOn(MapleCharacter chr, int selection) {
			return true;
		}

		@Override
		public void run(MapleCharacter chr, int selection) {
			MapleQuestInstance inst = chr.getQuest(current);
			
			if(inst.getStatus() == MapleQuestStatus.NOT_STARTED && inst.getForfeits() > 0){
				return;
			}
			
			chr.getClient().sendPacket(PacketFactory.updateQuestFinish((short)current, inst.getNpc(), (short)quest));
		}
		
	}
	
	public static class MesoAction implements MapleQuestAction {

		private int amount;
		
		@Override
		public void processData(MapleData data) {
			amount = MapleDataTool.getInt(data);
		}

		@Override
		public boolean isRunnableOn(MapleCharacter chr, int selection) {
			return true;
		}

		@Override
		public void run(MapleCharacter chr, int selection) {
			chr.giveMesos(amount);
		}
		
	}
	
	public static class ItemAction implements MapleQuestAction {

		private List<ItemData> possibleItems;
		
		@Override
		public void processData(MapleData data) {
			possibleItems = new ArrayList<>();
			for(MapleData child : data.getChildren()){
				int id = MapleDataTool.getInt("id", child);
				int count = MapleDataTool.getInt("count", child, 1);
				int prop = MapleDataTool.getInt("prop", child, -1);
				int gender = MapleDataTool.getInt("gender", child, -1);
				int job = MapleDataTool.getInt("job", child, -1);
				
				possibleItems.add(new ItemData(id, count, prop, gender, job));
				
			}
		}

		@Override
		public boolean isRunnableOn(MapleCharacter chr, int selection) {
			
			Map<InventoryType, Integer> props = new HashMap<>();
			List<Item> items = new ArrayList<>();
			
			for(ItemData data : possibleItems){
				if(!isItemApplicable(data, chr)){
					continue;
				}
				InventoryType type = InventoryType.getByItemId(data.getId());
				if(data.getProp() >= 0){
					if(!props.containsKey(type)){
						props.put(type, data.getId());
					}
					continue;
				}
				
				if(data.getCount() > 0){
					Item item = ItemFactory.getItem(data.getId(), data.getCount());
					items.add(item);
				}else{//Remove items
					int quantity = data.getCount() * -1;
					if(!hasEnough(data.getId(), quantity, chr)){
						return false;
					}else{
						continue;
					}
				}
				
			}
			
			for(int itemId : props.values()){
				Item item = ItemFactory.getItem(itemId, 1);
				items.add(item);
			}
			
			if(!chr.hasInventorySpace(items)){
				chr.sendMessage(MessageType.POPUP, "Please check if you have enough space in your inventory.");
				return false;
			}
			
			return true;
		}
		
		private boolean isItemApplicable(ItemData data, MapleCharacter chr){
			if(data.getGender() != chr.getGender() && data.getGender() >= 0){
				return false;
			}
			
			if(data.getJob() >= 0){
				if(data.getJob() != chr.getJob().getId()){
					return false;
				}else if(MapleJob.getBy5ByteEncoding(data.getJob()).getId() / 100 != chr.getJob().getId() / 100){
					return false;
				}
				
			}
			
			return true;
		}

		private boolean hasEnough(int itemId, int needed, MapleCharacter chr){
			
			InventoryType type = InventoryType.getByItemId(itemId);
			
			if(type == InventoryType.EQUIP){
				if(chr.getInventory(type).countById(itemId) >= needed){
					return true;
				}else if(chr.getInventory(InventoryType.EQUIPPED).countById(itemId) >= needed){
					return true;
				}
			}else{
				return chr.getInventory(type).countById(itemId) >= needed;
			}
			
			return false;
		}
		
		@Override
		public void run(MapleCharacter chr, int extSelection) {
			List<Integer> props = new ArrayList<>();
			for(ItemData data : possibleItems){
				if(data.getProp() != -1 && isItemApplicable(data, chr)){
					for(int i = 0; i < data.getProp();i++){
						props.add(data.getId());
					}
				}
			}
			
			int selection = 0;
			int extNum = 0;
			if(props.size() > 0){
				selection = props.get(Randomizer.nextInt(props.size()));
			}
			for(ItemData data : possibleItems){
				if(!isItemApplicable(data, chr)){
					continue;
				}
				if(data.getProp() != -1){
					if(extSelection != extNum++){
						continue;
					}else if(data.getId() != selection){
						continue;
					}
				}
				InventoryType type = InventoryType.getByItemId(data.getId());
				if(data.getCount() < 0){
					
					int amount = -data.getCount();
					if(type == InventoryType.EQUIP){
						if(chr.getInventory(type).countById(data.getId()) < amount){
							if(chr.getInventory(InventoryType.EQUIPPED).countById(data.getId()) >= amount){
								type = InventoryType.EQUIPPED;
							}
						}
					}
					chr.getInventory(type).removeItem(data.getId(), amount);
					chr.getClient().sendPacket(PacketFactory.getShowItemGain(data.getId(), -amount, true));
				}else{
					if(chr.getInventory(type).getFreeSlot() > -1){
						chr.getInventory(data.getId()).addItem(ItemFactory.getItem(data.getId(), data.getCount()));
						chr.getClient().sendPacket(PacketFactory.getShowItemGain(data.getId(), data.getCount(), true));
					}else{
						chr.sendMessage(MessageType.POPUP, type+" Inventory Full");
					}
				}
			}
		}
		
		@AllArgsConstructor
		@Data
		private static class ItemData {
			private int id, count, prop, gender, job;
		}
		
	}
	
	public static class FameAction implements MapleQuestAction {
		
		private int amount;
		
		@Override
		public void processData(MapleData data) {
			amount = MapleDataTool.getInt(data);
		}

		@Override
		public boolean isRunnableOn(MapleCharacter chr, int selection) {
			return true;
		}

		@Override
		public void run(MapleCharacter chr, int selection) {
			chr.gainFame(amount);
		}
		
	}
	
	public static class ExpAction implements MapleQuestAction {

		private int amount;
		
		@Override
		public void processData(MapleData data) {
			amount = MapleDataTool.getInt(data);
		}

		@Override
		public boolean isRunnableOn(MapleCharacter chr, int selection) {
			return true;
		}

		@Override
		public void run(MapleCharacter chr, int selection) {
			chr.giveExp(amount * ServerConstants.QUEST_EXP_RATE);
		}
		
	}
	
	public static class BuffAction implements MapleQuestAction {

		private int item;
		
		@Override
		public void processData(MapleData data) {
			item = MapleDataTool.getInt(data);
		}

		@Override
		public boolean isRunnableOn(MapleCharacter chr, int selection) {
			return true;
		}

		@Override
		public void run(MapleCharacter chr, int selection) {
			MapleStatEffect effect = ItemInfoProvider.getItemEffect(item);
			
			effect.applyTo(chr);
		}
		
	}
	
}
