package maplestory.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.inventory.item.ItemType;

@AllArgsConstructor
public enum InventoryType {

	EQUIPPED(-1),
	EQUIP(1),
	USE(2),
	SETUP(3),
	ETC(4),
	CASH(5);
	
	@Getter
	private final int id;
	
	public static InventoryType getByWZName(String name) {
        if (name.equals("Install")) {
            return SETUP;
        } else if (name.equals("Consume")) {
            return USE;
        } else if (name.equals("Etc")) {
            return ETC;
        } else if (name.equals("Cash")) {
            return CASH;
        } else if (name.equals("Pet")) {
            return CASH;
        }
        throw new IllegalArgumentException("Unknown WzName "+name);
    }
	
	public static InventoryType getByItemId(int itemId){
		
		if(itemId >= 2000000 && itemId <= 2450000){
			return USE;
		}else if(itemId >= 5000000 && itemId <= 5990000){
			return CASH;
		}else if(itemId >= 4000000 && itemId <= 4310000){
			return ETC;
		}else if(itemId >= 3010000 && itemId <= 3994178){
			return SETUP;
		}else if(itemId >= 1000000 && itemId < 2000000){
			return EQUIP;
		}
		
		throw new IllegalArgumentException("Unknown InventoryType for item "+itemId);
	}

	public static InventoryType getById(int type) {
		for(InventoryType t : values()){
			if(t.id == type){
				return t;
			}
		}
		
		return null;
	}
	
}
