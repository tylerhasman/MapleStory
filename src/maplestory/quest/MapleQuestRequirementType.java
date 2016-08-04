package maplestory.quest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MapleQuestRequirementType {
	
	JOB("job"),
	ITEM("item"),
	QUEST("quest"),
	MIN_LEVEL("lvmin"),
	MAX_LEVEL("lvmax"),
	END_DATE("end"),
	MOB("mob"),
	NPC("npc"),
	FIELD_ENTER("fieldEnter"),
	INTERVAL("interval"),
	START_SCRIPT("startscript"),
	END_SCRIPT("endscript"),
	PET("pet"),
	MIN_PET_TAMENESS("pettamenessmin"),
	MONSTER_BOOK("mbmin"),
	NORMAL_AUTO_START("normalAutoStart"),
	INFO_NUMBER("infoNumber"),
	INFO_EX("infoex"),
	COMPLETED_QUEST("questComplete"),
	START("start"),
	END("end"),
	DAY_BY_DAY("daybyday");
	
	@Getter
	private final String wzName;
	
	public static MapleQuestRequirementType getByWzName(String name){
		for(MapleQuestRequirementType type : values()){
			if(type.wzName.equals(name)){
				return type;
			}
		}
		return null;
	}
	
	
}
