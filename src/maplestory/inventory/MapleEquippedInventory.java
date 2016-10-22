package maplestory.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import maplestory.inventory.item.EquipItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFlag;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MapleEquippedInventory extends MapleInventory {
	
	public MapleEquippedInventory(MapleCharacter chr, int maxSize) {
		super(chr, 20, InventoryType.EQUIPPED);
	}
	
	@Override
	public boolean addItem(Item item) {
		throw new RuntimeException("You cannot add items to an equip inventory. Use setItem(int, Item) instead.");
	}
	
	public boolean hasSpaceForTwoSlotItem(int extraSlot){
		
		if(getItem(extraSlot) == null){
			return true;
		}
		
		MapleCharacter owner = getOwner();
		MapleInventory equipInventory = (MapleInventory) owner.getInventory(InventoryType.EQUIP);
		
		if(equipInventory.getFreeSlot() == -1){
			return false;
		}
		
		return true;
	}
	
	public void equip(int source, int destination){
		
		MapleCharacter owner = getOwner();
		
		EquipItem equip = (EquipItem) getOwner().getInventory(InventoryType.EQUIP).getItem(source);
		EquipItem target = (EquipItem) getOwner().getInventory(InventoryType.EQUIPPED).getItem(destination);
		
		MapleInventory equipInventory = (MapleInventory) owner.getInventory(InventoryType.EQUIP);
		
		if(equip == null || !ItemInfoProvider.isEquippable(owner, equip, destination)){
			sendPacket(PacketFactory.getAllowActionsPacket());
			return;
		}else if(owner.isCygnus() && equip.isA(ItemType.ADVENTURER_MOUNT)){
			sendPacket(PacketFactory.getAllowActionsPacket());
			return;
		}else if(!owner.isCygnus() && equip.isA(ItemType.CYGNUS_MOUNT)){
			sendPacket(PacketFactory.getAllowActionsPacket());
			return;
		}
		
		//The client handles this so I think we can omit it
/*		boolean mayNotEquip = false;
		
		mayNotEquip = (equip.isA(ItemType.TWO_HANDED_WEAPON) && !hasSpace(-10));
		mayNotEquip = (equip.isA(ItemType.OVERALL) && !hasSpace(-5));
		
		if(mayNotEquip){
			sendPacket(PacketFactory.getAllowActionsPacket());
			owner.sendMessage(MessageType.POPUP, "You will need another free slot to equip that item");
			return;
		}*/
		
		List<InventoryOperation> ops = new ArrayList<>();
		

		equipInventory.setItemInternal(source, null);
		equipInventory.setItemInternal(destination, null);//Clear them both out to remove any confusion
		
		if(equip.isUntradeableOnEquip()){
			equip.addItemFlag(ItemFlag.UNTRADEABLE);
			ops.add(InventoryOperation.addItem(equip, source));
		}
		
		ops.add(InventoryOperation.moveItem(equip, source, destination));
		
		equipInventory.setItemInternal(source, target);
		setItemInternal(destination, equip);
		
		sendPacket(PacketFactory.getInventoryOperationPacket(true, ops));
		
		if(equip.isA(ItemType.OVERALL)){
			unequip(-6, equipInventory.getFreeSlot());
		}else if(destination == -6 && (getItem(-5) != null && getItem(-5).isA(ItemType.OVERALL))){
			unequip(-5, equipInventory.getFreeSlot());
		}else if(equip.isA(ItemType.TWO_HANDED_WEAPON)){
			unequip(-10, equipInventory.getFreeSlot());
		}else if(destination == -10 && (getItem(-11) != null && getItem(-11).isA(ItemType.TWO_HANDED_WEAPON))){
			unequip(-11, equipInventory.getFreeSlot());
		}
		
		owner.updateCharacterLook();
		
	}
	
	
	public void unequip(int slot, int destination){
		MapleInventory equipInventory = (MapleInventory) getOwner().getInventory(InventoryType.EQUIP);
		
		EquipItem equip = (EquipItem) getItem(slot);
		EquipItem target = (EquipItem) equipInventory.getItem(destination);
		
		if(equip == null){
			return;
		}
		
		setItemInternal(slot, target);
		equipInventory.setItemInternal(destination, equip);
		List<InventoryOperation> ops = Collections.singletonList(InventoryOperation.moveItem(equip, slot, destination));
		
		sendPacket(PacketFactory.getInventoryOperationPacket(true, ops));
		getOwner().updateCharacterLook();
	}
	
}
