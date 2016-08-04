package maplestory.cashshop;

import java.util.concurrent.TimeUnit;

import sun.misc.Perf.GetPerfAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;

@AllArgsConstructor
public class CashShopItemData {

	/**
	 * Identified in the wz files as 'SN'
	 * <p>A unique entry id for the cash shop</p>
	 */
	private final int cashEntryId;
	/**
	 * The item id for the item
	 */
	private final int itemId;
	/**
	 * The amount of the item a player recieves on purchase
	 */
	private final int amount;
	private final int price;
	/**
	 * The amount of time in days until the item expires
	 */
	private final int period;

	/**
	 * Whether or not this item is currently being sold in the cash shop
	 */
	private final boolean onSale;
	
	/**
	 * @see #cashEntryId
	 * @return A unique entry id for the cash shop
	 */
	public int getCashEntryId() {
		return cashEntryId;
	}

	/**
	 * The price of the item
	 * @return the price
	 */
	public int getPrice() {
		return price;
	}
	
	/**
	 * @return true if this item is currently being sold in the cash shop
	 */
	public boolean isOnSale() {
		return onSale;
	}
	
	public Item createItem(){
		Item item = ItemFactory.getItem(itemId, amount, null, TimeUnit.DAYS.toMillis(period));
		
		return item;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	@Override
	public String toString() {
		return itemId+" x "+amount+" @ "+price+" SN "+cashEntryId;
	}
	
}
