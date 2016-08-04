package maplestory.inventory.item;


public interface CashItem extends Item {

	public long getExpirationDate();
	
	public long getUniqueId();
	
	public int getCashShopEntryId();
	
}
