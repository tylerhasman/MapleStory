package maplestory.inventory.item;

public class MapleEquipCashItem extends MapleEquipItem implements CashItem, EquipItem {

	private long expirationDate;
	
	private long uniqueId;
	
	public MapleEquipCashItem(int itemId, int amount, long expirationDate, long uniqueId, EquipItemInfo itemInfo) {
		super(itemId, amount, itemInfo);
		this.expirationDate = expirationDate;
		this.uniqueId = uniqueId;
	}
	
	public MapleEquipCashItem(int itemId, int amount, String owner, long expirationDate, long uniqueId, EquipItemInfo itemInfo) {
		super(itemId, amount, owner, itemInfo);
		this.expirationDate = expirationDate;
		this.uniqueId = uniqueId;
		addItemFlag(ItemFlag.UNKN);
	}
	
	@Override
	public String toString() {
		return "(Cash) " + super.toString();
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
	public Item copyOf(int amount) {
		MapleEquipItem copy = new MapleEquipCashItem(getItemId(), amount, getOwner(), expirationDate, uniqueId, new EquipItemInfo(itemInfo));
		
		return copy;
	}
	
}
