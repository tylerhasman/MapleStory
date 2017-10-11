package maplestory.player.ui;

import java.util.List;

import maplestory.inventory.item.Item;
import maplestory.life.MapleHiredMerchant;
import maplestory.player.MapleCharacter;

public interface HiredMerchantInterface extends UserInterface {

	public int getVisitorSlot(MapleCharacter chr);
	
	public int getMerchantItemId();

	public boolean isOwner(MapleCharacter chr);

	public String getOwnerName();

	public int getMesos();
	
	public String getDescription();

	public int getCapacity();
	
	public List<HiredMerchantItem> getItems();

	public List<MapleCharacter> getVisitors();

	public void addItem(Item clone, short amount, int price);
	
	public void buyItem(MapleCharacter visitor, int item, int amount);
	
	public boolean isOpen();
	
	public void open();
	
	public void close();

	public Item removeItem(int slot);
	
	public static interface HiredMerchantReciept {
		
		public Item getItem();
		
	}
	
	public static interface HiredMerchantItem {
		
		public Item getItem();
		
		public int getPrice();
		
		public int getAmountLeft();
		
		public void setAmountLeft(int amount);
		
	}

	public void removeStore();


	
}
