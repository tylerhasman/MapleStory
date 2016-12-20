package maplestory.shop;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.MapleDatabase;
import database.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MapleShop {

	private static Map<Integer, MapleShop> cache = new HashMap<>();
	private static Map<Integer, Integer> shops = new HashMap<>();
	
	@Getter
	private List<MapleShopItem> items;
	
	public MapleShop() {
		items = new ArrayList<>();
	}
	
	public void addItem(MapleShopItem item){
		items.add(item);
	}
	
	public static MapleShop getShop(int id){
		MapleShop shop = cache.get(id);
		if(shop == null){
			shop = new MapleShop();
			for(MapleShopItem item : loadItems(id)){
				shop.addItem(item);
			}
			
			cache.put(id, shop);
		}
		return shop;
	}
	
	public static int getShopId(int npcId){
		if(shops.containsKey(npcId)){
			return shops.get(npcId);
		}
		int id = -1;
		
		try{
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `shopid` FROM `shops` WHERE `npcid`=?", npcId);
			
			if(!results.isEmpty()){
				id = results.get(0).get("shopid");
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		shops.put(npcId, id);
		return id;
	}
	
	private static List<MapleShopItem> loadItems(int shopId){
		List<MapleShopItem> items = new ArrayList<>();
		
		try{
			
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `itemid`,`price`,`pitch` FROM `shopitems` WHERE `shopid`=? ORDER BY `position` ASC", shopId);
			
			for(QueryResult result : results){
				
				int itemId = result.get("itemid");
				int mesos = result.get("price");
				int pitch = result.get("pitch");
				CurrencyType curType = (mesos == 0 ? CurrencyType.PITCH : CurrencyType.MESOS);
				int price = (curType == CurrencyType.PITCH ? pitch : mesos);
				
				MapleShopItem item = new MapleShopItem(itemId, price, curType);
				
				items.add(item);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return items;
	}

	public void buy(MapleCharacter character, short slot, int itemId, short quantity) {
		MapleShopItem item = getItems().get(slot);
		if(item != null){
			if(item.getItemId() != itemId){
				return;
			}
		}else{
			return;
		}
		
		if(item.getMesoPrice() > 0){
			if(character.getMeso() >= item.getMesoPrice() * quantity){
				Inventory inv = character.getInventory(item.getItemId());
				if(inv.hasSpace(itemId, quantity)){
					
					Item purchased = ItemFactory.getItem(item.getItemId(), quantity);
					
					if(purchased.isA(ItemType.RECHARGABLE)){
						purchased.setAmount(ItemInfoProvider.getSlotMax(purchased.getItemId()));
					}
					
					inv.addItem(purchased);

					character.getClient().sendPacket(PacketFactory.shopTransactionResult(0));
				}else{
					character.getClient().sendPacket(PacketFactory.shopTransactionResult(3));
				}
			}else{
				character.getClient().sendPacket(PacketFactory.shopTransactionResult(2));
			}
		}
	}

	// c.announce(MaplePacketCreator.shopTransaction((byte) 0x8)); Sell Success
	public void sell(MapleCharacter character, short slot, int itemId, short quantity) {
		
		if(quantity <= 0){
			throw new IllegalArgumentException("quantity cannot be "+quantity);
		}
		
		Inventory inv = character.getInventory(itemId);
		
		Item item = inv.getItem(slot);
		
		if(item.getItemId() != itemId){
			character.getClient().sendPacket(PacketFactory.shopTransactionResult(6));
			return;
		}
		
		if(quantity > item.getAmount() && !item.isA(ItemType.RECHARGABLE)){
			character.getClient().sendPacket(PacketFactory.shopTransactionResult(1));
			return;
		}
		
		if(item.isA(ItemType.RECHARGABLE)){
			inv.setItem(slot, null);
		}else{
			inv.removeItemFromSlot(slot, quantity);
		}
		
		int price = ItemInfoProvider.getPrice(itemId);
		
		int recvMeso = price * quantity;
		
		if(recvMeso > 0){
			character.giveMesos(recvMeso, false, false);
		}
		
		character.getClient().sendPacket(PacketFactory.shopTransactionResult(8));
	}

	public void recharge(MapleCharacter mapleCharacter, short slot) {
		
		Inventory inv = mapleCharacter.getInventory(InventoryType.USE);
	
		Item item = inv.getItem(slot);
		
		if(item == null || !item.isA(ItemType.RECHARGABLE)){
			return;
		}
		
		if(item.getAmount() < 0){
			return;
		}
		
		int slotMax = ItemInfoProvider.getSlotMax(item.getItemId());
		
		if(item.getAmount() < slotMax){
			
			double unitPrice = ItemInfoProvider.getUnitPrice(item.getItemId());
			
			int cost = (int) Math.round(unitPrice * (slotMax - item.getAmount()));
			
			if(mapleCharacter.getMeso() >= cost){
				item.setAmount(slotMax);
				inv.refreshItem(slot);
				mapleCharacter.giveMesos(-cost);
				mapleCharacter.getClient().sendPacket(PacketFactory.shopTransactionResult(0x8));
			}else{
				mapleCharacter.getClient().sendPacket(PacketFactory.shopTransactionResult(0x2));
				mapleCharacter.getClient().sendReallowActions();
			}
			
		}
		
	}
	
	@AllArgsConstructor
	public static enum CurrencyType {

		MESOS((byte) 0),
		PITCH((byte) 1);
		
		@Getter
		private final byte id;
		
	}

	
}
