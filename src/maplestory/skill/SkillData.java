package maplestory.skill;

import lombok.Data;

@Data
public class SkillData {

	public static final SkillData EMPTY = new SkillData(0, 0);

	private final int level;
	private final int masterLevel;
	

}
