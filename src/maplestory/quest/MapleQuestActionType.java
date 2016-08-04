package maplestory.quest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MapleQuestActionType {

	EXP("exp"),
	ITEM("item"),
	NEXTQUEST("nextQuest"),
	MESO("money"),
	QUEST("quest"),
	SKILL("skill"),
	FAME("pop"),
	BUFF("buffItemID"),
	PETSKILL("petskill"),
	YES("yes"),
	NO("no"),
	NPC("npc"),
	MIN_LEVEL("lvmin");
	
	@Getter
	private final String wzName;
	
	public static MapleQuestActionType getByWZName(String name){
		for(MapleQuestActionType type : values()){
			if(type.wzName.equals(name)){
				return type;
			}
		}
		return null;
	}
	
}
