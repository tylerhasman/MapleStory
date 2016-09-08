package maplestory.inventory.item;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import provider.MapleCanvas;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import lombok.AllArgsConstructor;
import lombok.Data;
import maplestory.cashshop.CashShopItemData;
import maplestory.cashshop.CashShopPackage;
import maplestory.inventory.EquipSlot;
import maplestory.inventory.InventoryType;
import maplestory.inventory.MapleWeaponType;
import maplestory.inventory.item.EquipItemInfo.EquipStat;
import maplestory.inventory.item.ItemStatInfo.ItemStat;
import maplestory.inventory.item.SummoningBag.SummoningEntry;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.quest.MapleQuest;
import maplestory.server.MapleStory;
import maplestory.skill.MapleStatEffect;

public class ItemInfoProvider {

	private MapleDataProvider itemData, equipData, stringData, etcData;
	
	private MapleData cashStringData, eqpStringData, etcStringData, insStringData, petStringData, consumeStringData;
	
	private Map<Integer, MapleData> itemDataCache;
	
	private static ItemInfoProvider instance = null;

    private Map<Integer, MapleStatEffect> effectCache;
    
    private Map<Integer, CashShopItemData> cashShopItemCache;
    private Map<Integer, Integer> cashShopItemCacheByItemId;
    private Map<Integer, CashShopPackage> cashShopPackages;
    
    private List<Integer> allItemIdCache;
    private Map<Integer, String> itemNames;
	
	public ItemInfoProvider() {
		 itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
	     equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
	     stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
	     etcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
	     cashStringData = stringData.getData("Cash.img");
	     consumeStringData = stringData.getData("Consume.img");
	     eqpStringData = stringData.getData("Eqp.img");
	     etcStringData = stringData.getData("Etc.img");
	     insStringData = stringData.getData("Ins.img");
	     petStringData = stringData.getData("Pet.img");
	     itemDataCache = new HashMap<>();
	     effectCache = new HashMap<>();
	     cashShopItemCache = new HashMap<>();
	     cashShopItemCacheByItemId = new HashMap<>();
	     cashShopPackages = new HashMap<>();
	}
	
	public static void loadCashShop(){
		
		System.setProperty("wzpath", "wz/");
		
		MapleData commodity = getInstance().etcData.getData("Commodity.img");
		
		for(MapleData child : commodity){
			int sn = MapleDataTool.getIntConvert("SN", child);
			int itemId = MapleDataTool.getIntConvert("ItemId", child);
			int count = MapleDataTool.getIntConvert("Count", child);
			int price = MapleDataTool.getIntConvert("Price", child, 0);
			int period = MapleDataTool.getIntConvert("Period", child, 1000);
			boolean onSale = MapleDataTool.getIntConvert("OnSale", child, 0) == 1;
			
			CashShopItemData data = new CashShopItemData(sn, itemId, count, price, period, onSale);
			
			getInstance().cashShopItemCache.put(sn, data);
			getInstance().cashShopItemCacheByItemId.put(itemId, sn);
			
		}
		
		MapleData packageData = getInstance().etcData.getData("CashPackage.img");
		
		for(MapleData child : packageData){
			
			MapleData cashShopEntryIds = child.getChildByPath("SN");
			
			List<MapleData> children = cashShopEntryIds.getChildren();
			
			int[] ids = new int[children.size()];
			int packageId = Integer.valueOf(child.getName());
			
			for(int i = 0; i < ids.length;i++){
				ids[i] = MapleDataTool.getInt(children.get(i));
			}
			
			getInstance().cashShopPackages.put(packageId, new CashShopPackage(packageId, ids));
			
		}
		
		MapleStory.getLogger().info("Loaded "+getInstance().cashShopItemCache.size()+" cash shop items.");
		MapleStory.getLogger().info("Loaded "+getInstance().cashShopPackages.size()+" cash shop packages.");
		
	}
	
	private static MapleData getItemDataNoCache(int itemId){
		MapleData data = null;
		String id = "0" + String.valueOf(itemId);
		
		MapleDataDirectoryEntry root = getInstance().itemData.getRoot();
		for(MapleDataDirectoryEntry topDir : root.getSubdirectories()){
			for(MapleDataFileEntry file : topDir.getFiles()){
				if(file.getName().equals(id.substring(0, 4) + ".img")){
					data = getInstance().itemData.getData(topDir.getName()+"/"+file.getName());
					
					if(data == null){
						return null;
					}
					data = data.getChildByPath(id);
					return data;
				}else if(file.getName().equals(id.substring(1) + ".img")){
					return getInstance().itemData.getData(topDir.getName() + "/" + file.getName());
				}
			}
		}
		root = getInstance().equipData.getRoot();
		
		for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry file : topDir.getFiles()) {
                if (file.getName().equals(id + ".img")) {
                    return getInstance().equipData.getData(topDir.getName() + "/" + file.getName());
                }
            }
        }
		
		return data;
	}
	
	private static String getEquipmentSlot(int itemId){
		String ret = "";
        
        MapleData item = getItemData(itemId);
        
        if (item == null) {
            return null;
        }
        
        MapleData info = item.getChildByPath("info");
        
        if (info == null) {
            return null;
        }

        ret = MapleDataTool.getString("islot", info, "");
        
        return ret;
	}
	
	private static MapleData getItemData(int itemId){
		if(getInstance().itemDataCache.containsKey(itemId)){
			return getInstance().itemDataCache.get(itemId);
		}
		
		MapleData data = getItemDataNoCache(itemId);
		
		if(data != null){
			getInstance().itemDataCache.put(itemId, data);
		}
		
		return data;
	}
	
	public static MapleData getStringData(int itemId){
		String category = null;
		MapleData data = null;
		
		if(ItemType.CASH.isThis(itemId) && !ItemType.PET.isThis(itemId)){
			data = getInstance().cashStringData;
		}else if(ItemType.USE.isThis(itemId)){
			data = getInstance().consumeStringData;
		}else if(ItemType.ETC.isThis(itemId)){
			data = getInstance().etcStringData;
			category = "Etc";
		}else if(ItemType.SETUP.isThis(itemId)){
			data = getInstance().insStringData;
		}else if(ItemType.PET.isThis(itemId)){
			data = getInstance().petStringData;
		} else if ((itemId >= 1010000 && itemId < 1040000) || (itemId >= 1122000 && itemId < 1123000) || (itemId >= 1142000 && itemId < 1143000)) {
            data = getInstance().eqpStringData;
            category = "Eqp/Accessory";
        } else if (itemId >= 1000000 && itemId < 1010000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Cap";
        } else if (itemId >= 1102000 && itemId < 1103000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Cape";
        } else if (itemId >= 1040000 && itemId < 1050000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Coat";
        } else if (itemId >= 20000 && itemId < 22000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Face";
        } else if (itemId >= 1080000 && itemId < 1090000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Glove";
        } else if (itemId >= 30000 && itemId < 32000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Hair";
        } else if (itemId >= 1050000 && itemId < 1060000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Longcoat";
        } else if (itemId >= 1060000 && itemId < 1070000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Pants";
        } else if (itemId >= 1802000 && itemId < 1810000) {
            data = getInstance().eqpStringData;
            category = "Eqp/PetEquip";
        } else if (itemId >= 1112000 && itemId < 1120000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Ring";
        } else if (itemId >= 1092000 && itemId < 1100000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Shield";
        } else if (itemId >= 1070000 && itemId < 1080000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Shoes";
        } else if (itemId >= 1900000 && itemId < 2000000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Taming";
        } else if (itemId >= 1300000 && itemId < 1800000) {
            data = getInstance().eqpStringData;
            category = "Eqp/Weapon";
        }else{
        	data = getInstance().eqpStringData;
        	category = "Eqp/PetEquip";
        }
		
		if(data == null){
			return null;
		}
		
		
		if(category == null){
			return data.getChildByPath(String.valueOf(itemId));
		}else{
			return data.getChildByPath(category + "/" + itemId);
		}
	}
	
	public static int getSlotMax(int itemId){
		MapleData data = getItemData(itemId);
		if(data != null){
			MapleData entry = data.getChildByPath("info/slotMax");
			if(entry == null){
				if(InventoryType.getByItemId(itemId) == InventoryType.EQUIP){
					return 1;
				}else{
					return 100;
				}
			}else{
				return MapleDataTool.getInt(entry);
			}
		}
		
		return 0;
	}
	
	public static boolean isEquippable(MapleCharacter chr, EquipItem equip, int dst){
		 int id = equip.getItemId();
		 
		 String slotName = getEquipmentSlot(id);
		 
		 if(!EquipSlot.getFromTextSlot(slotName).isAllowed(dst, equip instanceof CashItem)){
			 return false;
		 }
		 
		 if(chr.getJob() == MapleJob.GM || chr.getJob() == MapleJob.SUPERGM){
			 return true;
		 }
		 
		 EquipItemInfo info = getEquipInfo(id);
		 int reqLevel = info.getRequiredLevel();
		 if(chr.getLevel() < reqLevel){
			 return false;
		 }else if(info.getRequiredStr() > chr.getStr()){
			 return false;
		 }else if(info.getRequiredDex() > chr.getDex()){
			 return false;
		 }else if(info.getRequiredLuk() > chr.getLuk()){
			 return false;
		 }else if(info.getRequiredInt() > chr.getInt()){
			 return false;
		 }else if(info.getRequiredFame() > 0 && info.getRequiredFame() > chr.getFame()){
			 return false;
		 }
		 
		 return true;
	}
	
	private static int getValue(int itemId, String name, int def){
		MapleData item = getItemData(itemId);
		
		if(item == null){
			throw new IllegalArgumentException("Unknown itemId "+itemId);
		}
		
		MapleData entry = item.getChildByPath(name);
		
		if(entry != null){
			return MapleDataTool.getInt(entry);
		}
		
		return def;
	}
	
	public static int getMesoSackSize(int itemId){
		return getValue(itemId, "info/meso", 1);
	}
	
	public static int getPrice(int itemId){
		return getValue(itemId, "info/price", 1);
	}
	
	public static double getUnitPrice(int itemId){
		return getValue(itemId, "info/unitPrice", 1);
	}
	
	public static int getPrice(int itemId, int amount){
		return (int) Math.max(getUnitPrice(itemId) * amount, 1);
	}
	
	public static ItemStatInfo getStatInfo(int itemId){
		ItemStatInfo stats = new ItemStatInfo();
		
		MapleData item = getItemData(itemId);
		
		if(item == null){
			throw new IllegalArgumentException("Unknown item "+itemId);
		}
		
		MapleData info = item.getChildByPath("info");
		
		for(MapleData data : info.getChildren()){
			
			ItemStat stat = ItemStat.getByWzName(data.getName());
			
			if(stat != null){
				stat.setStat(stats, MapleDataTool.getInt(data));
			}
			
		}
		
		return stats;
	}
	
	public static ScrollStatInfo getScrollStatInfo(int itemId){
		if(!ItemType.SCROLL.isThis(itemId)){
			throw new IllegalArgumentException(itemId+" isn't a scroll!");
		}
		
		MapleData item = getItemData(itemId);
		
		if(item == null){
			throw new IllegalArgumentException("Unknown item "+itemId);
		}
		
		MapleData info = item.getChildByPath("info");
		
		int cursed = MapleDataTool.getInt("cursed", info, 0);
		int success = MapleDataTool.getInt("success", info, 100);
		
		ScrollStatInfo ssi = new ScrollStatInfo(getStatInfo(itemId), cursed, success);
		
		if(item.getChildByPath("req") != null){
			ssi.setItemsUseableOn(new ArrayList<>());
			for(MapleData child : item.getChildByPath("req")){
				int reqId = MapleDataTool.getInt(child);
				ssi.getItemsUseableOn().add(reqId);
			}
		}
		
		return ssi;
		
	}
	
	public static EquipItemInfo getEquipInfo(int itemId){
		if(InventoryType.getByItemId(itemId) != InventoryType.EQUIP && !ItemType.CHAIR.isThis(itemId)){
			throw new IllegalArgumentException(itemId+" isn't an equip or chair");
		}
		
		EquipItemInfo info = new EquipItemInfo(getStatInfo(itemId));
		
		MapleData item = getItemData(itemId);
		
		MapleData data = item.getChildByPath("info");
		
		for(MapleData child : data.getChildren()){
			EquipStat stat = EquipStat.getByWzName(child.getName());
			
			if(stat != null){
				stat.setStat(info, MapleDataTool.getInt(child));
			}
		}
		
		return info;
	}
	
	public static boolean isQuestItem(int itemId) {
		MapleData data = getItemData(itemId);
		
		boolean questItem = MapleDataTool.getIntConvert("info/quest", data, 0) == 1;
		
		return questItem;
	}
	
	public static ItemInfoProvider getInstance() {
		if (instance == null) {
			instance = new ItemInfoProvider();
		}

		return instance;
	}

	public static List<SummoningEntry> getSummoningBagEntries(int itemId) {

		List<SummoningEntry> entries = new ArrayList<>();
		
		MapleData data = getItemData(itemId);
		MapleData summons = data.getChildByPath("mob");
		if(summons != null){
			for(MapleData child : summons.getChildren()){
				int id = MapleDataTool.getInt("id", child);
				int prob = MapleDataTool.getInt("prob", child);
				entries.add(new SummoningEntry(id, prob));
			}
		}
		
		return entries;
	}

	public static MapleStatEffect getItemEffect(int itemId) {
		MapleStatEffect ret = getInstance().effectCache.get(itemId);
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if (item == null) {
                return null;
            }
            MapleData spec = item.getChildByPath("spec");
            ret = MapleStatEffect.loadItemEffectFromData(spec, itemId);
            getInstance().effectCache.put(itemId, ret);
        }
        return ret;
	}

	public static boolean noCancelMouse(int itemId) {
        MapleData item = getItemData(itemId);
        if (item == null) {
            return false;
        }
        return MapleDataTool.getIntConvert("info/noCancelMouse", item, 0) == 1;
	}

	public static MapleWeaponType getWeaponType(int itemId) {
		int cat = (itemId / 10000) % 100;
		MapleWeaponType[] type = { MapleWeaponType.SWORD1H, MapleWeaponType.AXE1H, MapleWeaponType.BLUNT1H, MapleWeaponType.DAGGER, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.WAND,
				MapleWeaponType.STAFF, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.SWORD2H, MapleWeaponType.AXE2H, MapleWeaponType.BLUNT2H, MapleWeaponType.SPEAR, MapleWeaponType.POLE_ARM, MapleWeaponType.BOW, MapleWeaponType.CROSSBOW,
				MapleWeaponType.CLAW, MapleWeaponType.KNUCKLE, MapleWeaponType.GUN };
		if (cat < 30 || cat > 49) {
			return MapleWeaponType.NOT_A_WEAPON;
		}
		return type[cat - 30];
	}

	public static String getItemName(int itemId) {
		if(instance.itemNames == null){
			instance.itemNames = new HashMap<>();
		}
		if(instance.itemNames.containsKey(itemId)){
			return instance.itemNames.get(itemId);
		}
		MapleData strings = getStringData(itemId);
		if (strings == null) {
			return null;
		}
		String ret = MapleDataTool.getString("name", strings, null);
		instance.itemNames.put(itemId, ret);
		return ret;
	}

	public static List<Integer> getAllItemIds(){
		/*if (instance.allItemIdCache == null) {
			instance.allItemIdCache = instance.getAllItems();
		}

		return instance.allItemIdCache;*/
		return instance.getAllItems();
	}
	
	private List<Integer> getAllItems(){
		List<Integer> ids = new ArrayList<>();
		
		MapleData[] allStringData = new MapleData[] { eqpStringData.getChildByPath("Eqp"), etcStringData.getChildByPath("Etc"), insStringData, petStringData, consumeStringData, cashStringData };
	    
		
		for(MapleData stringData : allStringData){
			
			int id = -1;
			
			for(MapleData child : stringData.getChildren()){
				if(stringData.getName().equals("Eqp")){
					for(MapleData cat : child){
						id = Integer.parseInt(cat.getName());
						ids.add(id);
					}
				}else{
					id = Integer.parseInt(child.getName());
					ids.add(id);
				}
				
			}
			
			
		}
		
		/*for(MapleDataDirectoryEntry directory : getInstance().itemData.getRoot().getSubdirectories()){

			for(MapleDataFileEntry sub : directory.getFiles()){
				
				MapleData data = getInstance().itemData.getData(directory.getName()+"/"+sub.getName());
				
				for(MapleData child : data){
					
					if(child.getName().startsWith("0")){
						String idStr = child.getName().substring(1);
						ids.add(Integer.parseInt(idStr));
					}
					
					
				}
				
			}
		}
		
		for(MapleDataDirectoryEntry directory : getInstance().equipData.getRoot().getSubdirectories()){

			if(directory.getName().equals("Dragon") || directory.getName().equals("Afterimage") || directory.getName().equals("Face") || directory.getName().equals("Hair")){
				continue;
			}
			
			for(MapleDataFileEntry dataFile : directory.getFiles()){
				
				MapleData data = getInstance().equipData.getData(directory.getName()+"/"+dataFile.getName());
				
				for(MapleData child : data){
					
					if(child.getName().startsWith("0")){
						String idStr = child.getName().substring(1);
						ids.add(Integer.parseInt(idStr));
					}
					
					
				}
				
			}
			
		}*/
		
		return ids;
	}
	
	public static SkillBookData getSkillBookData(int itemId) {
		SkillBookData data = null;
		
		MapleData item = getItemData(itemId);
		
		if(item == null){
			throw new IllegalArgumentException(itemId+" is unknown.");
		}
		
		MapleData info = item.getChildByPath("info");
		if(info == null){
			throw new IllegalArgumentException(itemId+" has no skill information! Is it even a skillbook?");
		}
		
		int masterLevel = MapleDataTool.getInt("masterLevel", info, 0);
		int reqLevel = MapleDataTool.getInt("reqSkillLevel", info, 0);
		int successRate = MapleDataTool.getInt("success", info, 0);
		MapleData skillData = info.getChildByPath("skill");
		
		int[] skills = new int[skillData.getChildren().size()];
		
		for(int i = 0; i < skills.length;i++){
			int skillId = MapleDataTool.getInt(String.valueOf(i), skillData, 0);
			
			if(skillId == 0){
				break;
			}
			skills[i] = skillId;
		}
		
		data = new SkillBookData(masterLevel, reqLevel, successRate, skills);
		
		
		return data;
	}

	@Data
	@AllArgsConstructor
	public static class SkillBookData {
		
		private final int masterLevel, requiredLevel, successRate;
		private final int[] skills;
		
		
	}

	public static boolean isDropRestricted(int itemId) {
		MapleData data = getItemData(itemId);
        boolean bRestricted = MapleDataTool.getIntConvert("info/tradeBlock", data, 0) == 1;
        if (!bRestricted) {
        	bRestricted = MapleDataTool.getIntConvert("info/accountSharable", data, 0) == 1;
        }
        if (!bRestricted) {
            bRestricted = MapleDataTool.getIntConvert("info/quest", data, 0) == 1;
        }
		return bRestricted;
	}

	public static CashShopItemData getCashShopItemData(int entryId){
		return instance.cashShopItemCache.get(entryId);
	}

	public static CashShopItemData getCashShopItemDataByItemId(int itemId) {
		return getCashShopItemData(instance.cashShopItemCacheByItemId.get(itemId));
	}

	public static CashShopPackage getCashShopPackage(int boughtId) {
		return getInstance().cashShopPackages.get(boughtId);
	}

	public static int getProjectileWatkBonus(int itemId) {
		MapleData data = getItemData(itemId);
		int atk = MapleDataTool.getInt("info/incPAD", data, 0);
		
		return atk;
	}

	public static boolean isCashItem(int itemId) {
		if(ItemType.CASH.isThis(itemId)){
			return true;
		}
		
		if(InventoryType.getByItemId(itemId) == InventoryType.EQUIP){
			return getEquipInfo(itemId).isCash();
		}
		return false;
	}

	public static int getQuestMedalId(int questId) {
		MapleData questInfo = MapleQuest.getQuestData().getData("QuestInfo.img");
		MapleData questData = questInfo.getChildByPath(String.valueOf(questId));
		int medal = MapleDataTool.getInt("viewMedalItem", questData, -1);
		return medal;
	}

	public static int getItemWidth(int id) {
		MapleData itemData = getItemData(id);
		
		MapleData infoData = itemData.getChildByPath("info");
		if(infoData == null){
			return 0;
		}
		
		MapleCanvas canvas = (MapleCanvas) infoData.getChildByPath("icon").getData();
		
		return canvas.getWidth();
	}
	
}
