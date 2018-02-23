package maplestory.inventory.item;

public interface Item extends Comparable<Item> {

	public int getItemId();
	
	public int getAmount();
	
	public String getOwner();
	
	public int getFlag();
	
	public void setFlag(int flag);
	
	public void addItemFlag(ItemFlag...flags);
	
	public void removeItemFlags(ItemFlag...flags);
	
	public boolean isA(ItemType type);

	public void setAmount(int amount);
	
	public boolean canMerge(Item other);
	
	/**
	 * Create a copy of this item stack with a new amount
	 */
	public Item copyOf(int amount);
	
	public Item copy();
	
	public ItemMeta getItemMeta();

	public static long getExpiration(Item item) {
		long expiration = -1;
		if(item instanceof CashItem) {
			expiration = ((CashItem) item).getExpirationDate();
		}
		return expiration;
	}
	
	public static long getUniqueId(Item item) {
		long uniqueId = -1;
		if(item instanceof CashItem) {
			uniqueId = ((CashItem) item).getUniqueId();
		}
		return uniqueId;
	}
	
}
