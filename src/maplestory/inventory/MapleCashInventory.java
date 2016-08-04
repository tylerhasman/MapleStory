package maplestory.inventory;

import maplestory.inventory.item.Item;
import maplestory.player.MapleCharacter;

public class MapleCashInventory extends MapleInventory implements CashInventory{

	public MapleCashInventory(MapleCharacter chr, int maxSize) {
		super(chr, maxSize, InventoryType.CASH);
	}
	
	@Override
	public int getVisibleProjectile(){
		for (int i = 1; i <= getSize(); i++) { // impose order...
            Item item = getItem((short) i);
            if (item != null) {
                if (item.getItemId() / 1000 == 5021) {
                    return item.getItemId();
                }
            }
        }
		return -1;
	}

}
