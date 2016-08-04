package maplestory.inventory.item;



public interface EquipItem extends Item {

	public int getUpgradeSlotsAvailble();
	
	public int getLevel();
	
	public int getFlag();
	
	public int getItemLevel();
	
	public int getStr();
	public int getDex();
	public int getInt();
	public int getLuk();
	
	public int getHp();
	public int getMp();
	
	public int getWeaponAttack();
	
	public int getMagicAttack();
	
	public int getWeaponDefense();
	public int getMagicDefense();
	
	public int getAccuracy();
	
	public int getAvoid();
	
	public int getSpeed();
	
	public int getHands();
	
	public int getJump();
	
	public int getHammerUpgrades();
	
	public int getRequiredJob();
	public int getRequiredLevel();
	public int getRequiredStr();
	public int getRequiredDex();
	public int getRequiredLuk();
	public int getRequiredInt();
	
	public boolean isCash();
	
	public boolean isUntradeableOnEquip();
	
	public EquipItemInfo getStatInfo();
	
/*	*//**
	 * Applies a scroll to a copy of this item
	 * @param scroll the scroll
	 * @return the result of the scroll along with a copy of this item with the applied stats
	 *//*
	public ScrollResult applyScroll(ScrollItem scroll);*/
}
