package maplestory.player.ui;

import maplestory.player.MapleCharacter;

import maplestory.inventory.item.Item;

public interface TradeInterface extends UserInterface {

	public void putItem(Item item, int slot, MapleCharacter chr);
	
	public void confirmTrade(MapleCharacter chr);
	
	public void offerMesos(int amount, MapleCharacter chr);
	
	public int countTradedItem(int itemId, MapleCharacter chr);
	
}
