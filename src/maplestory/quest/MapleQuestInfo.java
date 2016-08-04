package maplestory.quest;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataTool;
import lombok.Getter;
import maplestory.player.MapleCharacter;

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
	
	protected MapleQuestInfo(MapleQuest quest, MapleData info, MapleData acts, MapleData reqs){
		loadInfo(info);
		relevantMobs = new HashMap<>();
		relevantItems = new HashMap<>();
		startActions = new EnumMap<>(MapleQuestActionType.class);
		endActions = new EnumMap<>(MapleQuestActionType.class);
	}
	
	
	protected void loadWzData(MapleData acts, MapleData reqs, MapleQuest quest){
		MapleData startReqs = reqs.getChildByPath("0");
		MapleData endReqs = reqs.getChildByPath("1");
		MapleData startActs = acts.getChildByPath("0");
		MapleData endActs = acts.getChildByPath("1");
		
		startRequirements = loadRequirements(quest, startReqs);
		completionRequirements = loadRequirements(quest, endReqs);

		startActions = loadActions(quest, startActs);
		endActions = loadActions(quest, endActs);
		
		repeatable = startRequirements.containsKey(MapleQuestRequirementType.INTERVAL);
	}
	
	private EnumMap<MapleQuestActionType, MapleQuestAction> loadActions(MapleQuest quest, MapleData data){
		EnumMap<MapleQuestActionType, MapleQuestAction> actions = new EnumMap<>(MapleQuestActionType.class);
		
		for(MapleData child : data.getChildren()){
			MapleQuestActionType type = MapleQuestActionType.getByWZName(child.getName());
			MapleQuestAction action = MapleQuestAction.getFromType(quest, type, child);
			
			if(action == null){
				continue;
			}
			
			actions.put(type, action);
		}
		
		return actions;
	}
	
	private EnumMap<MapleQuestRequirementType, MapleQuestRequirement> loadRequirements(MapleQuest quest, MapleData data){
		EnumMap<MapleQuestRequirementType, MapleQuestRequirement> requirements = new EnumMap<>(MapleQuestRequirementType.class);
		
		for(MapleData child : data.getChildren()){
			String name = child.getName();
			
			MapleQuestRequirementType qrt = MapleQuestRequirementType.getByWzName(name);
			
			if(qrt == MapleQuestRequirementType.INFO_NUMBER){
				infoNumber = MapleDataTool.getInt(child, 0);
			}else if(qrt == MapleQuestRequirementType.MOB){
				for(MapleData mobData : child.getChildren()){
					int id = MapleDataTool.getInt("id", mobData);
					int count = MapleDataTool.getInt("count", mobData);
					relevantMobs.put(id, count);
				}
			}else if(qrt == MapleQuestRequirementType.ITEM){
				for(MapleData itemData : child.getChildren()){
					int id = MapleDataTool.getInt("id", itemData);
					int count = MapleDataTool.getInt("count", itemData);
					relevantItems.put(id, count);
				}
			}
			MapleQuestRequirement req = MapleQuestRequirement.getFromType(qrt, quest, child);
			if(req != null)
				requirements.put(qrt, req);
		}
		
		return requirements;
	}
	
	private void loadInfo(MapleData info){
		timeLimit = MapleDataTool.getInt("timeLimit", info, -1);
		autoStarted = MapleDataTool.getInt("autoStart", info, 0) == 1;
		autoCompleted = MapleDataTool.getInt("autoComplete", info, 0) == 1;
		autoPreCompleted = MapleDataTool.getInt("autoPreComplete", info, 0) == 1;
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
