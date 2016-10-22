package maplestory.inventory.item;



public class MapleItem implements Item {

	private int itemId;
	private int amount;
	private int flag;
	private String owner;
	private ItemMeta itemMeta = null;

	
	public MapleItem(int itemId, int amount) {
		this.itemId = itemId;
		this.amount = amount;
		flag = 0;
	}
	
	public MapleItem(int itemId, int amount, String owner) {
		this(itemId, amount);
		this.owner = owner;
	}
	
	
	@Override
	public ItemMeta getItemMeta() {
		if (itemMeta == null) {
			itemMeta = new ItemMeta();
		}

		return itemMeta;
	}
	
    public int compareTo(Item other) {
        if (getItemId() < other.getItemId()) {
            return -1;
        } else if (getItemId() > other.getItemId()) {
            return 1;
        }
         return 0;
    }
    
    @Override
    public Item copy() {
    	return copyOf(amount);
    }
	
    @Override
    public String toString() {
    	return itemId+" "+ItemInfoProvider.getItemName(itemId)+" x "+amount;
    }
    
	@Override
	public boolean isA(ItemType type) {
		return type.isThis(itemId);
	}
	
	@Override
	public int getFlag() {
		return flag;
	}
	
	@Override
	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	@Override
	public void addItemFlag(ItemFlag... flags){
		for(ItemFlag f : flags){
			flag |= f.getBit();
		}
	}
	
	@Override
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	@Override
	public void removeItemFlags(ItemFlag... flags){
		for(ItemFlag f : flags){
			flag &= ~f.getBit();
		}
	}
	
	@Override
	public int getItemId() {
		return itemId;
	}

	@Override
	public int getAmount() {
		return amount;
	}

	@Override
	public String getOwner() {
		if(owner == null || owner.isEmpty()){
			return "";
		}
		return owner;
	}

	@Override
	public boolean canMerge(Item other) {
		if(other.isA(ItemType.RECHARGABLE) || isA(ItemType.RECHARGABLE)){
			return false;
		}
		return other.getItemId() == itemId && other.getFlag() == flag;
	}

	@Override
	public Item copyOf(int amount) {
		
		MapleItem item = new MapleItem(itemId, amount);
		item.flag = flag;
		item.owner = owner;
		item.itemMeta = itemMeta == null ? null : itemMeta.clone();
		
		return item;
	}
	
}
