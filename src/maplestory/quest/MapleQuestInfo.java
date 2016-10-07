package maplestory.quest;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import maplestory.player.MapleCharacter;
import me.tyler.mdf.Node;

public class MapleQuestInfo {

	@Getter
	private int timeLimit;
	
	@Getter
	private int infoNumber;
	
	@Getter
	private boolean repeatable;
	
	@Getter
	private boolean autoStarted, autoCompleted, autoPreCompleted;
	
	@Getter
	private EnumMap<MapleQuestRequirementType, MapleQuestRequirement> startRequirements, completionRequirements;
	
	private EnumMap<MapleQuestActionType, MapleQuestAction> startActions, endActions;
	
	@Getter
	private Map<Integer, Integer> relevantItems, relevantMobs;
	
	protected MapleQuestInfo(MapleQuest quest, Node info, Node acts, Node reqs){
		loadInfo(info);
		relevantMobs = new HashMap<>();
		relevantItems = new HashMap<>();
		startActions = new EnumMap<>(MapleQuestActionType.class);
		endActions = new EnumMap<>(MapleQuestActionType.class);
	}
	
	
	protected void loadWzData(Node acts, Node reqs, MapleQuest quest){
		Node startReqs = reqs.getChild("0");
		Node endReqs = reqs.getChild("1");
		Node startActs = acts.getChild("0");
		Node endActs = acts.getChild("1");
		
		startRequirements = loadRequirements(quest, startReqs);
		completionRequirements = loadRequirements(quest, endReqs);

		startActions = loadActions(quest, startActs);
		endActions = loadActions(quest, endActs);
		
		repeatable = startRequirements.containsKey(MapleQuestRequirementType.INTERVAL);
	}
	
	private EnumMap<MapleQuestActionType, MapleQuestAction> loadActions(MapleQuest quest, Node data){
		EnumMap<MapleQuestActionType, MapleQuestAction> actions = new EnumMap<>(MapleQuestActionType.class);
		
		for(Node child : data.getChildren()){
			MapleQuestActionType type = MapleQuestActionType.getByWZName(child.getName());
			MapleQuestAction action = MapleQuestAction.getFromType(quest, type, child);
			
			if(action == null){
				continue;
			}
			
			actions.put(type, action);
		}
		
		return actions;
	}
	
	private EnumMap<MapleQuestRequirementType, MapleQuestRequirement> loadRequirements(MapleQuest quest, Node data){
		EnumMap<MapleQuestRequirementType, MapleQuestRequirement> requirements = new EnumMap<>(MapleQuestRequirementType.class);
		
		for(Node child : data.getChildren()){
			String name = child.getName();
			
			MapleQuestRequirementType qrt = MapleQuestRequirementType.getByWzName(name);
			
			if(qrt == MapleQuestRequirementType.INFO_NUMBER){
				infoNumber = child.intValue();
			}else if(qrt == MapleQuestRequirementType.MOB){
				for(Node mobData : child.getChildren()){
					int id = mobData.readInt("id");
					int count = mobData.readInt("count");
					relevantMobs.put(id, count);
				}
			}else if(qrt == MapleQuestRequirementType.ITEM){
				for(Node itemData : child.getChildren()){
					int id = itemData.readInt("id");
					int count = itemData.readInt("count");
					relevantItems.put(id, count);
				}
			}
			MapleQuestRequirement req = MapleQuestRequirement.getFromType(qrt, quest, child);
			if(req != null)
				requirements.put(qrt, req);
		}
		
		return requirements;
	}
	
	private void loadInfo(Node info){
		timeLimit = info.readInt("timeLimit", -1);
		autoStarted = info.readInt("autoStart", 0) == 1;
		autoCompleted = info.readInt("autoComplete", 0) == 1;
		autoPreCompleted = info.readInt("autoPreComplete", 0) == 1;
	}
	
	public Collection<MapleQuestAction> getStartActions(){
		return Collections.unmodifiableCollection(startActions.values());
	}
	
	public Collection<MapleQuestAction> getEndActions(){
		return Collections.unmodifiableCollection(endActions.values());
	}
	
	public boolean canComplete(MapleCharacter chr, int npc){
		for(MapleQuestRequirementType reqType : completionRequirements.keySet()){
			MapleQuestRequirement req = completionRequirements.get(reqType);
			
			if(!req.isConditionMet(chr, npc)){
				return false;
			}
		}

		return true;
	}

	public boolean canStart(MapleCharacter chr, int npc) {
		
		for(MapleQuestRequirementType reqType : startRequirements.keySet()){
			MapleQuestRequirement req = startRequirements.get(reqType);
			if(!req.isConditionMet(chr, npc)){
				return false;
			}
		}
		
		return true;
	}
	
}
