package maplestory.inventory;

import java.util.List;
import java.util.Map;

import maplestory.cashshop.CashShopInventory;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.Item;
import maplestory.player.MapleCharacter;

public interface Inventory {
	
	/**
	 * Add an item to this inventory
	 * @param item the item to add
	 * @return true if all items were added
	 */
	public boolean addItem(Item item);
	
	public Map<Integer, Item> getItems();
	
	public int getSize();
	
	public void setMaxSize(int size);
	
	public void setItem(int slot, Item item);
	
	public int getFreeSlot();
	
	/**
	 * Get a free slot after X number of slots
	 * @param margin the number of slots
	 * @return the slot or -1 if the item won't fit
	 */
	public int getFreeSlot(int margin);
	
	public int countFreeSlots();
	
	public InventoryType getType();
	
	public MapleCharacter getOwner();
	
	public List<Item> listById(int itemId);

	public int countById(int itemId);

	public Item getItem(int index);

	public boolean removeItem(Item item);
	
	public boolean removeItem(int id, int amount);
	
	public int getProjectileId(int bulletCount, Item weapon);

	public void removeItemFromSlot(int slot, int amount);

	public boolean isFull();
	
	public boolean isFull(int amount);
	
	public boolean hasSpace(int amountOfItems);

	public void dropItem(int slot, int amount);

	public void moveItem(int slot, int destination);

	public boolean hasSpace(int itemId, int quantity);

	public void refreshItem(int slot);

	public Item firstOf(int itemId);

	public int findByCashId(long cashUniqueId);

	public void transferToCashInventory(int targetItem, CashShopInventory cashInventory);

	
}
