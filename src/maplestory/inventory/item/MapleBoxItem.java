package maplestory.inventory.item;

import java.util.List;

public class MapleBoxItem extends MapleItem implements BoxItem {

	public MapleBoxItem(int itemId, int amount) {
		super(itemId, amount);
	}
	
	public MapleBoxItem(int itemId, int amount, String owner) {
		super(itemId, amount, owner);
	}

	@Override
	public List<Reward> getRewards() {
		return ItemInfoProvider.getItemRewards(getItemId());
	}

}
