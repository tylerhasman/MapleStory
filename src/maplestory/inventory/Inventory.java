package maplestory.inventory;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import maplestory.inventory.item.Item;
import maplestory.player.MapleCharacter;
import maplestory.player.MaplePetInstance;

public interface Inventory {

	/**
	 * The result of an item being added to an inventory
	 *
	 */
	@AllArgsConstructor
	@Data
	public static class AddItemResult {
		
		private final boolean success;
		private final boolean inventoryFull;
		
	}
	
	@Data
	@AllArgsConstructor
	public static class RemoveItemResult {
		private final int remainingAmount;
		private final boolean allRemoved;
	}
	
	/**
	 * Add an item to this inventory
	 * @param item the item to add
	 * @return see {@link AddItemResult}
	 */
	public AddItemResult addItem(Item item);
	
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
	
	public InventoryType getType();
	
	public MapleCharacter getOwner();
	
	public List<Item> listById(int itemId);

	public int countById(int itemId);

	public Item getItem(int index);

	public RemoveItemResult removeItem(Item item);
	
	public RemoveItemResult removeItem(int id, int amount);
	
	public int getProjectileId(int bulletCount, Item weapon);

	public void removeItemFromSlot(int slot, int amount);

	public boolean isFull();
	
	public boolean hasSpace(int amountOfItems);

	public void dropItem(int slot, int amount);

	public void moveItem(int slot, int destination);

	public boolean hasSpace(int itemId, int quantity);

	public void refreshItem(int slot);

	
}
