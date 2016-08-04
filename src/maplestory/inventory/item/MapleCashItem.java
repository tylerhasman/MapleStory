package maplestory.inventory.item;


public class MapleCashItem extends MapleItem implements CashItem {

	private long expirationDate;
	private long uniqueId;
	
	public MapleCashItem(int itemId, int amount, long expirationDate, long uniqueId) {
		super(itemId, amount);
		this.expirationDate = expirationDate;
		this.uniqueId = uniqueId;
	}
	
	public MapleCashItem(int itemId, int amount, String owner, long expirationDate, long uniqueId) {
		super(itemId, amount, owner);
		this.expirationDate = expirationDate;
		this.uniqueId = uniqueId;
	}

	@Override
	public long getExpirationDate() {
		return expirationDate;
	}

	@Override
	public long getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public int getCashShopEntryId() {
		return ItemInfoProvider.getCashShopItemDataByItemId(getItemId()).getCashEntryId();
	}
	
	@Override
	public boolean canMerge(Item other) {
		if(other instanceof CashItem){
			CashItem ci = (CashItem) other;
			if(ci.getExpirationDate() != expirationDate){
				return false;
			}
			
		}
		return super.canMerge(other);
	}

	@Override
	public Item copyOf(int amount) {
		MapleCashItem copy = new MapleCashItem(getItemId(), amount, getOwner(), expirationDate, uniqueId);
		
		return copy;
	}
	
}
