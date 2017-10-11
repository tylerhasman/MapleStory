package maplestory.inventory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import constants.LoginStatus;
import maplestory.cashshop.CashShopInventory;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.EquipItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;
import maplestory.util.Pair;

public class MapleInventory implements Inventory {

	private Map<Integer, Item> items;
	private int maxSize;
	private InventoryType type;
	private WeakReference<MapleCharacter> owner;
	
	public MapleInventory(MapleCharacter owner, int maxSize, InventoryType type) {
		items = new HashMap<>(maxSize);
		this.maxSize = maxSize;
		this.type = type;
		this.owner = new WeakReference<MapleCharacter>(owner);
	}
	
	@Override
	public final Map<Integer, Item> getItems() {
		return Collections.unmodifiableMap(items);
	}
	
	@Override
	public boolean hasSpace(int amountOfItems) {
		return items.size() + amountOfItems <= maxSize;
	}
	
	@Override
	public void transferToCashInventory(int slot, CashShopInventory cashInventory) {
		
		Item item = getItem(slot);

		System.out.println(slot);
		if(item == null){
			return;
		}
		
		
		if(item instanceof CashItem){
			setItemInternal(slot, null);
			cashInventory.addItem(item);

			if(getOwner() != null){
				getOwner().getClient().sendPacket(PacketFactory.cashShopAddItem((CashItem) item, getOwner().getClient().getId()));
			}
			
		}else{
			throw new IllegalArgumentException("Item at slot "+slot+" is not a cash item. It is a "+item.getClass().getName()+" toString: "+item.toString());
		}
		
	}
	
	@Override
	public Item firstOf(int itemId) {
		for(Item item : items.values()){
			if(item.getItemId() == itemId){
				return item;
			}
		}
		return null;
	}
	
	@Override
	public int findByCashId(int cashUniqueId) {
		for(Entry<Integer, Item> item : items.entrySet()){
			if(item.getValue() instanceof CashItem){
				if(((CashItem) item.getValue()).getUniqueId() == cashUniqueId){
					return item.getKey();
				}
			}
		}
		return -1;
	}
	
	@Override
	public void refreshItem(int slot) {
		sendPacket(PacketFactory.getInventoryOperationPacket(false, Collections.singletonList(InventoryOperation.addItem(getItem(slot), slot))));
	}
	
	@Override
	public boolean hasSpace(int itemId, int quantity) {
		
		if(hasSpace(Math.max(quantity, ItemInfoProvider.getSlotMax(itemId)) / ItemInfoProvider.getSlotMax(itemId))){
			return true;
		}
		
		for(Item item : items.values()){
			if(item.getItemId() == itemId){
				int amountCanAdd = ItemInfoProvider.getSlotMax(itemId) - item.getAmount();
				quantity -= amountCanAdd;
			}
		}
		
		return quantity <= 0;
	}
	
	@Override
	public int countById(int itemId) {
		int count = 0;
		for(Item item : listById(itemId)){
			count += item.getAmount();
		}
		return count;
	}
	
	@Override
	public void moveItem(int slot, int destination) {
		if(slot < 0 || destination < 0){
			return;
		}
		if(destination > getSize()){
			return;
		}
		
		Item moving = getItem(slot);
		Item target = getItem(destination);
		
		if(moving == null){
			return;
		}
		
		int slotMax = getOwner().getMaxSlotForItem(moving);
		
		List<InventoryOperation> ops = new ArrayList<>();
		
		if(target == null){
			setItemInternal(slot, null);
			setItemInternal(destination, moving);
			ops.add(InventoryOperation.moveItem(moving, slot, destination));
		}else if(target.canMerge(moving)){
			if(moving.getAmount() + target.getAmount() > slotMax){
				int total = target.getAmount() + moving.getAmount();
				target.setAmount(slotMax);
				moving.setAmount(total - slotMax);
				ops.add(InventoryOperation.updateAmount(moving, slot));
				ops.add(InventoryOperation.updateAmount(target, destination));
			}else{
				target.setAmount(moving.getAmount() + target.getAmount());
				setItemInternal(slot, null);
				ops.add(InventoryOperation.updateAmount(target, destination));
				ops.add(InventoryOperation.removeItem(moving, slot));
			}
		}else{
			swap(slot, destination);
			ops.add(InventoryOperation.moveItem(moving, slot, destination));
		}
		
		sendPacket(PacketFactory.getInventoryOperationPacket(true, ops));
	}
	
	private void swap(int one, int two){
		Item itemOne = getItem(one);
		Item itemTwo = getItem(two);
		
		setItemInternal(two, itemOne);
		setItemInternal(one, itemTwo);
	}
	
	@Override
	public void dropItem(int slot, int amount) {
		Item dropped = getItem(slot);
		
		if(dropped == null || amount <= 0){
			return;
		}
		
		if(!getOwner().canDropItems()){
			return;
		}
		
		if(dropped.isA(ItemType.PET)){
			return;
		}
		
		if((!dropped.isA(ItemType.RECHARGABLE) && getOwner().getItemQuantity(dropped.getItemId(), true) < amount)){
			return;
		}
		
		if(amount < dropped.getAmount() && !dropped.isA(ItemType.RECHARGABLE)){
			Item copy = dropped.copyOf(amount);
			dropped.setAmount(dropped.getAmount() - copy.getAmount());
			
			sendPacket(PacketFactory.getInventoryOperationPacket(true, Collections.singletonList(InventoryOperation.updateAmount(dropped, slot))));
		
			if(ItemInfoProvider.isDropRestricted(dropped.getItemId()) || copy instanceof CashItem){
				getOwner().getMap().spawnDisappearingItemDrop(getOwner(), dropped, getOwner().getPosition());
			}else{
				getOwner().getMap().dropItem(copy, getOwner().getPosition(), getOwner());
			}
		}else{
			setItemInternal(slot, null);
			sendPacket(PacketFactory.getInventoryOperationPacket(true, Collections.singletonList(InventoryOperation.removeItem(dropped, slot))));
			if(slot < 0){
				getOwner().updateCharacterLook();
			}
			if(ItemInfoProvider.isDropRestricted(dropped.getItemId()) || dropped instanceof CashItem){
				getOwner().getMap().spawnDisappearingItemDrop(getOwner(), dropped, getOwner().getPosition());
			}else{
				getOwner().getMap().dropItem(dropped, getOwner().getPosition(), getOwner());
			}
		}
		
	}
	
	@Override
	public void removeItemFromSlot(int slot, int amount) {
		Item item = getItem(slot);
		
		item.setAmount(item.getAmount() - amount);
		
		if(item.getAmount() <= 0){
			setItemInternal(slot, null);
			sendPacket(PacketFactory.getInventoryOperationPacket(true, Collections.singletonList(InventoryOperation.removeItem(item, slot))));
		}else{
			sendPacket(PacketFactory.getInventoryOperationPacket(true, Collections.singletonList(InventoryOperation.updateAmount(item, slot))));
		}
	}
	
	@Override
	public int getProjectileId(int bulletCount, Item weapon) {
		
		MapleWeaponType weaponType = ItemInfoProvider.getWeaponType(weapon.getItemId());
		
		MapleCharacter chr = (MapleCharacter) getOwner();
		
		Set<Integer> slotSet = items.keySet();
		List<Integer> slots = slotSet.stream().sorted().collect(Collectors.toList());
		
		for(int slot : slots){
			Item item = getItem(slot);
			
			int id = item.getItemId();
			
			boolean bow = item.isA(ItemType.ARROW);
			boolean cbow = item.isA(ItemType.BOLT);
			boolean star = item.isA(ItemType.THROWING_STAR);
			boolean bullet = item.isA(ItemType.BULLET);
			boolean isMagicalMitten = weapon.getItemId() == 1472063;
			
			// 1472063 is Magical Mitten. It allows players to throw arrows :O
			
			if(item.getAmount() >= bulletCount){
				if(weaponType == MapleWeaponType.CLAW && star && !isMagicalMitten){
					if (!((id == 2070007 || id == 2070018) && chr
							.getLevel() < 70)
							|| (id == 2070016 && chr.getLevel() < 50)) {
						return id;
					}
				}else if(weaponType == MapleWeaponType.GUN && bullet){
					if (id == 2331000 && id == 2332000) {
						if (chr.getLevel() > 69) {
							return id;
						}
					} else if (chr.getLevel() > (id % 10) * 20 + 9) {
						return id;
					}
				}else if((weaponType == MapleWeaponType.BOW && bow) 
						|| (weaponType == MapleWeaponType.CROSSBOW && cbow)
						|| (weapon.getItemId() == 1472063 && (bow || cbow))){
					return id;
				}
			}
		}
		
		return 0;
	}
	
	@Override
	public boolean removeItem(int id, int amount) {
		if(amount == 0){
			return true;
		}
		List<Pair<Integer, Item>> existing = listByIdWithSlot(id);
		List<InventoryOperation> actions = new ArrayList<>();
		
		for(Pair<Integer, Item> pair : existing){
			if(amount <= 0){
				break;
			}
			int slot = pair.getLeft();
			Item item = pair.getRight();
			
			if(item.getAmount() > amount){
				int oldAmount = item.getAmount();
				item.setAmount(item.getAmount() - amount);
				amount -= oldAmount;
				actions.add(InventoryOperation.updateAmount(item, slot));
			}else{// only possibility left is (item.getAmount() < amount)
				amount -= item.getAmount();
				if(!ItemType.RECHARGABLE.isThis(id)){
					setItemInternal(slot, null);
					actions.add(InventoryOperation.removeItem(item, slot));
				}else{
					item.setAmount(0);
					actions.add(InventoryOperation.updateAmount(item, slot));
				}
				
			}
		}

		sendPacket(PacketFactory.getInventoryOperationPacket(true, actions));
		
		return amount <= 0;
	}
	
	@Override
	public boolean removeItem(Item item) {
		return removeItem(item.getItemId(), item.getAmount());
	}
	
	@Override
	public MapleCharacter getOwner() {
		return owner.get();
	}
	
	public final Item getItem(int slot){
		return items.get(slot);
	}
	
	@Override
	public boolean isFull() {
		return items.size() >= getSize();
	}
	
	@Override
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	private List<Pair<Integer, Item>> listByIdWithSlot(int itemId){
		List<Pair<Integer, Item>> filtered = new ArrayList<>();
		
		for(int slot : items.keySet()){
			Item item = items.get(slot);
			
			if(item.getItemId() == itemId){
				filtered.add(new Pair<Integer, Item>(slot, item));
			}
		}
		
		return filtered;
	}
	
	@Override
	public boolean addItem(Item item) {
		
		int slotMax = getOwner().getMaxSlotForItem(item);
		
		List<Pair<Integer, Item>> existing = listByIdWithSlot(item.getItemId());
		
		List<InventoryOperation> changes = new ArrayList<InventoryOperation>();
		
		if(item instanceof EquipItem){
			int slot = getFreeSlot();
			if(slot > 0){
				setItemInternal(slot, item);
				changes.add(InventoryOperation.addItem(item, slot));
			}
			sendPacket(PacketFactory.getInventoryOperationPacket(true, changes));
			return slot > 0;
		}
		if(!item.isA(ItemType.RECHARGABLE)){
			for(Pair<Integer, Item> pair : existing){
				Item exists = pair.getRight();
				if(exists.canMerge(item)){
					int oldAmount = exists.getAmount();
					if(exists.getAmount() < slotMax){
						int newAmount = Math.min(exists.getAmount() + item.getAmount(), slotMax);
						
						item.setAmount(item.getAmount() - (newAmount - oldAmount));
						exists.setAmount(newAmount);
						
						changes.add(InventoryOperation.updateAmount(exists, pair.getLeft()));
					}	
				}
			}
			int total = item.getAmount();
			while(total > 0){
				int newQ = Math.min(item.getAmount(), slotMax);
				if(newQ != 0){
					total -= item.getAmount();
					Item nItem = item.copyOf(newQ);
					int slot = getFreeSlot();
					
					if(slot == -1){
						sendPacket(PacketFactory.getInventoryOperationPacket(true, changes));
						return true;
					}
					changes.add(InventoryOperation.addItem(nItem, slot));
					setItemInternal(slot, nItem);
					
				}
			}
			sendPacket(PacketFactory.getInventoryOperationPacket(true, changes));
			return true;
		}else{
			int slot = getFreeSlot();
			
			if(slot == -1){
				sendPacket(PacketFactory.getInventoryOperationPacket(true, changes));
				return false;
			}
			
			changes.add(InventoryOperation.addItem(item, slot));
			
			setItemInternal(slot, item);
			sendPacket(PacketFactory.getInventoryOperationPacket(true, changes));
			return false;
		}
		
	}
	
	protected void sendPacket(byte[] packet){
		if(getOwner().getClient().getLoginStatus() != LoginStatus.IN_GAME){
			return;
		}
		getOwner().getClient().sendPacket(packet);
	}
	
	public int getFreeSlot(){
		return getFreeSlot(0);
	}
	
	@Override
	public int getFreeSlot(int margin) {
		for(int i = margin + 1;i <= getSize();i++){
			if(getItem(i) == null){
				return i;
			}
		}
		
		return -1;
	}
	
	@Override
	public List<Item> listById(int itemId) {
		List<Item> items = new ArrayList<>();
		
		for(Item item : this.items.values()){
			if(item.getItemId() == itemId){
				items.add(item);
			}
		}
		return items;
	}
	
	protected void setItemInternal(int slot, Item item){
		if(item == null){
			items.remove(slot);
		}
		else{
			items.put(slot, item);
		}
	}
	
	@Override
	public final void setItem(int slot, Item item) {
		if(getOwner() != null && getOwner().getClient().getLoginStatus() == LoginStatus.IN_GAME){
			if(item != null)
				sendPacket(PacketFactory.getInventoryOperationPacket(false, Collections.singletonList(InventoryOperation.addItem(item, slot))));
			else
				sendPacket(PacketFactory.getInventoryOperationPacket(false, Collections.singletonList(InventoryOperation.removeItem(getItem(slot), slot))));
		}
		setItemInternal(slot, item);
	}

	@Override
	public final InventoryType getType() {
		return type;
	}

	@Override
	public int getSize() {
		return maxSize;
	}

	
	
}
