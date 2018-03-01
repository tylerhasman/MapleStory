package maplestory.inventory.item;

import maplestory.player.MapleJob;

public class MapleSkillBook extends MapleItem implements SkillBook {

	
	

	private int masterLevel;
	private int requiredSkillLevel;
	private int[] skills;
	private int successRate;

	public MapleSkillBook(int itemId, int amount, int level, int requiredLevel, int[] skills, int successRate) {
		super(itemId, amount);
		this.masterLevel = level;
		this.requiredSkillLevel = requiredLevel;
		this.skills = skills;
		this.successRate = successRate;
	}
	

	public MapleSkillBook(int itemId, int amount, String owner, int level, int requiredLevel, int[] skills, int successRate) {
		super(itemId, amount, owner);
		this.masterLevel = level;
		this.requiredSkillLevel = requiredLevel;
		this.skills = skills;
		this.successRate = successRate;
	}
	
	@Override
	public int getMasterLevel() {
		return masterLevel;
	}

	@Override
	public int getRequiredSkillLevel() {
		return requiredSkillLevel;
	}

	@Override
	public int getSkill(MapleJob job) {
		
		for(int i = 0; i < skills.length;i++){
			int skill = skills[i];
			
			if(skill / 10000 == job.getId()){
				return skill;
			}
		}
		
		return 0;
	}

	@Override
	public int getSuccessRate() {
		return successRate;
	}

	@Override
	public Item copy() {
		return new MapleSkillBook(getItemId(), getAmount(), masterLevel, getRequiredSkillLevel(), skills, getSuccessRate());
	}
	
	@Override
	public Item copyOf(int amount) {
		
		Item item = copy();
		item.setAmount(amount);
		
		return item;
	}
	
}
