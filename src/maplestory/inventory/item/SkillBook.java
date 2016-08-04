package maplestory.inventory.item;

import maplestory.player.MapleJob;

public interface SkillBook extends Item {

	public int getMasterLevel();
	
	public int getRequiredSkillLevel();
	
	public int getSkill(MapleJob job);
	
	public int getSuccessRate();
	
}
