package maplestory.skill;

import java.util.ArrayList;
import java.util.List;

public class SkillChanges {

	private List<Skill> skills;
	private List<Integer> levels;
	private List<Integer> masteryLevel;
	
	private int index;
	
	public SkillChanges() {
		skills = new ArrayList<>();
		levels = new ArrayList<>();
		masteryLevel = new ArrayList<>();
		index = -1;
	}
	
	public void addChange(Skill skill, int level, int mastery) {
		skills.add(skill);
		levels.add(level);
		masteryLevel.add(mastery);
	}
	
	public Skill getSkill() {
		return skills.get(index);
	}
	
	public int getLevel() {
		return levels.get(index);
	}
	
	public int getMasteryLevel() {
		return masteryLevel.get(index);
	}
	
	public boolean next() {
		return ++index < skills.size();
	}
	
}
