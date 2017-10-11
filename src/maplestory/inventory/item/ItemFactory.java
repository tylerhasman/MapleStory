package maplestory.inventory.item;

import maplestory.inventory.InventoryType;
import maplestory.inventory.item.ItemInfoProvider.SkillBookData;
import maplestory.inventory.item.PetItem.PetDataSnapshot;
import maplestory.server.MapleStory;
import maplestory.util.Randomizer;

import database.QueryResult;

public class ItemFactory {
	
	public static long getRandomId(){
		return Math.abs(Randomizer.nextLong());
	}
	
	public static Item getItem(int itemId, int amount, String owner, long expiryTime){
		long expiryDate = System.currentTimeMillis() + expiryTime;
		
		PetDataSnapshot petData = null;
		
		EquipItemInfo equipInfo = null;
		
		if(expiryTime == -1){
			expiryDate = -1;
		}
		
		if(ItemType.PET.isThis(itemId)){
			petData = new PetDataSnapshot(ItemInfoProvider.getItemName(itemId), 0, 1, 100, false);
		}
		
		if(InventoryType.getByItemId(itemId) == InventoryType.EQUIP){
			equipInfo = ItemInfoProvider.getEquipInfo(itemId);
		}
		
		long uniqueId = Math.abs(Randomizer.nextInt());
		
		Item item = getItem(itemId, amount, owner, expiryDate, uniqueId, equipInfo, 0, petData);
		
		return item;
		
	}
	
	private static Item getItem(int itemId, int amount, String owner, long expirationDate, long uid, EquipItemInfo info, int flag, PetDataSnapshot petData){
		Item item = null;
		
		if(ItemType.PET.isThis(itemId)){
			item = new MaplePetItem(itemId, amount, owner, expirationDate, uid, petData);
		}else if(ItemType.CASH.isThis(itemId)){
			item = new MapleCashItem(itemId, amount, owner, expirationDate, uid);
		}else if(InventoryType.getByItemId(itemId) == InventoryType.EQUIP){
			if(ItemInfoProvider.isCashItem(itemId)){
				item = new MapleEquipCashItem(itemId, amount, owner, expirationDate, uid, info);
			}else{
				item = new MapleEquipItem(itemId, amount, owner, info);
			}
		}else if(ItemType.SCROLL.isThis(itemId)){
			ScrollStatInfo ssi = ItemInfoProvider.getScrollStatInfo(itemId);
			
			item = new MapleScrollItem(itemId, amount, owner, ssi.getDestroyRate(), ssi.getSuccessRate(), ssi);
		}else if(ItemType.SUMMONING_BAG.isThis(itemId)){
			item = new MapleSummoningBag(itemId, amount, ItemInfoProvider.getSummoningBagEntries(itemId));
		}else if(ItemType.MASTERY_BOOK.isThis(itemId)){
			SkillBookData data = ItemInfoProvider.getSkillBookData(itemId);
			if(data == null){
				throw new NullPointerException("data is null with itemId "+itemId);
			}
			item = new MapleSkillBook(itemId, amount, data.getMasterLevel(), data.getRequiredLevel(), data.getSkills(), data.getSuccessRate());
		}else if(ItemType.BOX_ITEM.isThis(itemId)){
			item = new MapleBoxItem(itemId, amount, owner);
		}else{
			item = new MapleItem(itemId, amount, owner);
		}
		
		item.setFlag(flag);
		
		return item;
	}
	
	public static Item getItem(int itemId, int amount, String owner){
		return getItem(itemId, amount, owner, MapleStory.getServerConfig().getDefaultCashItemExpireTime());
	}
	
	public static Item getItem(int itemId, int amount){
		return getItem(itemId, amount, null); 
	}

	public static Item getItem(QueryResult result) {
		
		int itemid = result.get("itemid");
		int amount = result.get("amount");
		String owner = result.get("owner");
		int flag = result.get("flag");
		long expiration = result.get("expiration");
		long uid = result.get("unique_id");
		EquipItemInfo itemInfo = null;
		PetDataSnapshot petData = null;
		if(!result.isNull("data")){
			if(ItemType.PET.isThis(itemid)){
				petData = new PetDataSnapshot(null, 0, 0, 0, false);
				String data = result.get("data");
				petData.deserialize(data);
			}else{
				itemInfo = new EquipItemInfo();
				String data = result.get("data");
				itemInfo.deserialize(data);
			}
		}
		
		Item item = getItem(itemid, amount, owner, expiration, uid, itemInfo, flag, petData);
		
		return item;
	}
	
}
