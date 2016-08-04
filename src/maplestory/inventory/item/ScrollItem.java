package maplestory.inventory.item;


public interface ScrollItem extends Item {

	public int getDestroyRate();
	
	public int getSuccessRate();
	
	public ItemStatInfo getStatBonuses();
	
	public boolean isCursed();
	
	public ScrollResult useScroll(EquipItem item);

	public boolean isUseableOn(int itemId);
	
}
