package maplestory.shop;

import maplestory.inventory.item.ItemInfoProvider;

public class MapleShopItem {

	private int itemId;
	private int price;
	private byte currency;
	
	public MapleShopItem(int itemId, int price, CurrencyType type){
		this.itemId = itemId;
		this.price = price;
		currency = type.getId();
	}
	
	public int getQuantity() {
		return ItemInfoProvider.getSlotMax(itemId);
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getMesoPrice(){
		if(currency != CurrencyType.MESOS.getId()){
			return 0;
		}
		
		return price;
	}
	
	public int getPitchPrice(){
		if(currency != CurrencyType.PITCH.getId()){
			return 0;
		}
		
		return price;
	}
	
}
