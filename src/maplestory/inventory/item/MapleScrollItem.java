package maplestory.inventory.item;

import maplestory.util.Randomizer;


public class MapleScrollItem extends MapleItem implements ScrollItem {

	private int destroyRate, successRate;
	private ItemStatInfo stats;
	
	public MapleScrollItem(int itemId, int amount, int destroyRate, int successRate, ItemStatInfo info) {
		super(itemId, amount);
		this.successRate = successRate;
		this.destroyRate = destroyRate;
		this.stats = info;
	}
	
	public MapleScrollItem(int itemId, int amount, String owner, int destroyRate, int successRate, ItemStatInfo info) {
		super(itemId, amount, owner);
		this.successRate = successRate;
		this.destroyRate = destroyRate;
		this.stats = info;
	}

	@Override
	public Item copyOf(int amount) {
		return new MapleScrollItem(getItemId(), amount, getOwner(), destroyRate, successRate, new ItemStatInfo(stats));
	}
	
	@Override
	public int getDestroyRate() {
		return destroyRate;
	}

	@Override
	public int getSuccessRate() {
		return successRate;
	}

	@Override
	public ItemStatInfo getStatBonuses() {
		return stats;
	}

	@Override
	public boolean isCursed() {
		return destroyRate > 0;
	}

	@Override
	public ScrollResult useScroll(EquipItem target) {
		
		MapleEquipItem item = (MapleEquipItem) target.copy();
		
		if(Randomizer.nextInt(100) < getSuccessRate()){
			
			
			if(!isA(ItemType.CLEAN_SLATE_SCROLL)){
				item.itemInfo.setUpgradeSlots(item.itemInfo.getUpgradeSlots()-1);
				item.itemInfo.setLevel(item.itemInfo.getLevel() + 1);
				item.itemInfo.addStats(getStatBonuses());
				item.itemInfo.addStats(getStatBonuses());
			}else{
				item.itemInfo.setUpgradeSlots(item.itemInfo.getUpgradeSlots()+1);
			}
			
			return new ScrollResult(true, false, item);
		}else{
			
			if(isCursed()){
				if(Randomizer.nextInt(100) < getDestroyRate()){
					return new ScrollResult(false, true, null);
				}
			}
			
			if(!isA(ItemType.CLEAN_SLATE_SCROLL)){
				item.itemInfo.setUpgradeSlots(item.itemInfo.getUpgradeSlots()-1);
			}
			
		}
		
		return new ScrollResult(false, false, item);
	}

	@Override
	public boolean isUseableOn(int itemId) {
		return (getItemId() / 100) % 100 == (itemId / 10000) % 100;
	}

}
