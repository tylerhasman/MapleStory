package maplestory.inventory;

import java.lang.ref.WeakReference;
import java.util.Map;

import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;

public class InventoryGroup {

	private Map<InventoryType, Inventory> inventories;
	
	private MapleCharacterSnapshot snapshot;
	
	public InventoryGroup(MapleCharacter chr) {
		this.snapshot = chr.createSnapshot();
	}
	
	public void createInventory(InventoryType type){
		if(!snapshot.isOnline()){
			throw new IllegalStateException("Cannot create inventory if character is not online.");
		}
		Inventory inv = null;
		
		/*if(type == InventoryType.EQUIPPED){
			inv = new MapleEquippedInventory(snapshot.getLiveCharacter().get());
		}else{
			inv = new MapleInventory(snapshot.getLiveCharacter(), 0, type);
		}
		*/
		
	}
	
}
