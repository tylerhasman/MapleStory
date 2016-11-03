package maplestory.inventory.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import maplestory.server.MapleStory;
import maplestory.skill.MapleStatEffect;
import me.tyler.mdf.MapleFile;
import me.tyler.mdf.Node;

public class ItemInfoProvider {

	private MapleFile itemData, equipData, stringData, etcData;
	
	private Node cashStringData, eqpStringData, etcStringData, insStringData, petStringData, consumeStringData;
	
	private Map<Integer, Node> itemDataCache;
	
	private static ItemInfoProvider instance;

    private Map<Integer, MapleStatEffect> effectCache;
    
    private Map<Integer, CashShopItemData> cashShopItemCache;
    private Map<Integer, Integer> cashShopItemCacheByItemId;
    private Map<Integer, CashShopPackage> cashShopPackages;
    
    private List<Integer> allItemIdCache;
    private Map<Integer, String> itemNames;
	
	public ItemInfoProvider() {
		 itemData = MapleStory.getDataFile("Item.mdf");
	     equipData = MapleStory.getDataFile("Character.mdf");
	     stringData = MapleStory.getDataFile("String.mdf");
	     etcData = MapleStory.getDataFile("Etc.mdf");
	     cashStringData = stringData.getRootNode().readNode("Cash.img");
	     consumeStringData = stringData.getRootNode().readNode("Consume.img");
	     eqpStringData = stringData.getRootNode().readNode("Eqp.img");
	     etcStringData = stringData.getRootNode().readNode("Etc.img");
	     insStringData = stringData.getRootNode().readNode("Ins.img");
	     petStringData = stringData.getRootNode().readNode("Pet.img");
	     itemDataCache = new HashMap<>();
	     effectCache = new HashMap<>();
	     cashShopItemCache = new HashMap<>();
	     cashShopItemCacheByItemId = new HashMap<>();
	     cashShopPackages = new HashMap<>();
	}
	
	public static void loadCashShop(){
		
		Node commodity = getInstance().etcData.getRootNode().readNode("Commodity.img");
		
		for(Node child : commodity){
			int sn = child.readInt("SN");
			int itemId = child.readInt("ItemId");
			int count = child.readInt("Count");
			int price = child.readInt("Price", 0);
			int period = child.readInt("Period", 1000);
			boolean onSale = child.readInt("OnSale", 0) == 1;
			
			CashShopItemData data = new CashShopItemData(sn, itemId, count, price, period, onSale);
			
			getInstance().cashShopItemCache.put(sn, data);
			getInstance().cashShopItemCacheByItemId.put(itemId, sn);
			
		}
		
		Node packageData = getInstance().etcData.getRootNode().readNode("CashPackage.img");
		
		for(Node child : packageData){
			
			Node cashShopEntryIds = child.readNode("SN");
			
			Collection<Node> children = cashShopEntryIds.getChildren();
			
			int[] ids = new int[children.size()];
			int packageId = Integer.valueOf(child.getName());
			
			Iterator<Node> it = children.iterator();
			
			for(int i = 0; i < ids.length;i++){
				ids[i] = cashShopEntryIds.readInt(it.next().getName());
			}
			
			getInstance().cashShopPackages.put(packageId, new CashShopPackage(packageId, ids));
			
		}
		
		MapleStory.getLogger().info("Loaded "+getInstance().cashShopItemCache.size()+" cash shop items.");
		MapleStory.getLogger().info("Loaded "+getInstance().cashShopPackages.size()+" cash shop packages.");
		
	}
	
	private static Node getItemDataNoCache(int itemId){
		Node data = null;
		String id = "0" + String.valueOf(itemId);
		
		Node root = getInstance().itemData.getRootNode();
		for(Node topDir : root){
			for(Node file : topDir){
				if(file.getName().equals(id.substring(0, 4) + ".img")){
					data = getInstance().itemData.getRootNode().getChild(topDir.getName()+"/"+file.getName());
					
					if(data == null){
						return null;
					}
					data = data.getChild(id);
					return data;
				}else if(file.getName().equals(id.substring(1) + ".img")){
					return getInstance().itemData.getRootNode().getChild(topDir.getName() + "/" + file.getName());
				}
			}
		}
		root = getInstance().equipData.getRootNode();
		
		for (Node topDir : root) {
            for (Node file : topDir) {
                if (file.getName().equals(id + ".img")) {
                    return getInstance().equipData.getRootNode().getChild(topDir.getName() + "/" + file.getName());
                }
            }
        }
		
		return data;
	}
	
	private static String getEquipmentSlot(int itemId){
		String ret = "";
        
        Node item = getItemData(itemId);
        
        if (item == null) {
            return null;
        }
        
        Node info = item.getChild("info");
        
        if (info == null) {
            return null;
        }

        ret = info.readString("islot", "");
        
        return ret;
	}
	
	private static Node getItemData(int itemId){
		if(getInstance().itemDataCache.containsKey(itemId)){
			return getInstance().itemDataCache.get(itemId);
		}
		
		Node data = getItemDataNoCache(itemId);
		
		if(data != null){
			getInstance().itemDataCache.put(itemId, data);
		}
		
		return data;
	}
	
	public static Node getStringData(int itemId){
		String category = null;
		Node data = null;
		
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
			return data.getChild(String.valueOf(itemId));
		}else{
			return data.getChild(category + "/" + itemId);
		}
	}
	
	public static int getSlotMax(int itemId){
		Node data = getItemData(itemId);
		if(data != null){
			Node entry = data.getChild("info/slotMax");
			if(entry == null){
				if(InventoryType.getByItemId(itemId) == InventoryType.EQUIP){
					return 1;
				}else{
					return 100;
				}
			}else{
				return entry.intValue();
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
		Node item = getItemData(itemId);
		
		if(item == null){
			throw new IllegalArgumentException("Unknown itemId "+itemId);
		}
		
		Node entry = item.getChild(name);
		
		if(entry != null){
			return (int) entry.getValue();
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
		
		Node item = getItemData(itemId);
		
		if(item == null){
			throw new IllegalArgumentException("Unknown item "+itemId);
		}
		
		Node info = item.getChild("info");
		
		for(Node data : info){
			
			ItemStat stat = ItemStat.getByWzName(data.getName());
			
			if(stat != null){
				stat.setStat(stats, data.intValue());
			}
			
		}
		
		return stats;
	}
	
	public static ScrollStatInfo getScrollStatInfo(int itemId){
		if(!ItemType.SCROLL.isThis(itemId)){
			throw new IllegalArgumentException(itemId+" isn't a scroll!");
		}
		
		Node item = getItemData(itemId);
		
		if(item == null){
			throw new IllegalArgumentException("Unknown item "+itemId);
		}
		
		Node info = item.getChild("info");
		
		int cursed = info.readInt("cursed", 0);
		int success = info.readInt("success", 100);
		
		ScrollStatInfo ssi = new ScrollStatInfo(getStatInfo(itemId), cursed, success);
		
		if(item.getChild("req") != null){
			ssi.setItemsUseableOn(new ArrayList<>());
			for(Node child : item.getChild("req")){
				int reqId = (int) child.getValue();
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
		
		Node item = getItemData(itemId);
		
		Node data = item.getChild("info");
		
		for(Node child : data.getChildren()){
			EquipStat stat = EquipStat.getByWzName(child.getName());
			
			if(stat != null){
				stat.setStat(info, child.intValue());
			}
		}
		
		return info;
	}
	
	public static boolean isQuestItem(int itemId) {
		Node data = getItemData(itemId);
		
		boolean questItem = data.readNode("info").readInt("quest", 0) == 1;
		
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
		
		Node data = getItemData(itemId);
		Node summons = data.getChild("mob");
		if(summons != null){
			for(Node child : summons.getChildren()){
				int id = child.readInt("id");
				int prob = child.readInt("prob");
				entries.add(new SummoningEntry(id, prob));
			}
		}
		
		return entries;
	}

	public static MapleStatEffect getItemEffect(int itemId) {
		MapleStatEffect ret = getInstance().effectCache.get(itemId);
        if (ret == null) {
            Node item = getItemData(itemId);
            if (item == null) {
                return null;
            }
            Node spec = item.getChild("spec");
            ret = MapleStatEffect.loadItemEffectFromData(spec, itemId);
            getInstance().effectCache.put(itemId, ret);
        }
        return ret;
	}

	public static boolean noCancelMouse(int itemId) {
        Node item = getItemData(itemId);
        if (item == null) {
            return false;
        }
        return item.readNode("info").readInt("noCancelMouse", 0) == 1;
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
		if(getInstance().itemNames == null){
			getInstance().itemNames = new HashMap<>();
		}
		if(getInstance().itemNames.containsKey(itemId)){
			return getInstance().itemNames.get(itemId);
		}
		Node strings = getStringData(itemId);
		if (strings == null) {
			return null;
		}
		String ret = strings.readString("name");
		getInstance().itemNames.put(itemId, ret);
		return ret;
	}

	public static List<Integer> getAllItemIds(){
		if (getInstance().allItemIdCache == null) {
			getInstance().allItemIdCache = getInstance().getAllItems();
		}

		return getInstance().allItemIdCache;
	}
	
	private List<Integer> getAllItems(){
		List<Integer> ids = new ArrayList<>();
		
		Node[] allStringData = new Node[] { eqpStringData.getChild("Eqp"), etcStringData.getChild("Etc"), insStringData, petStringData, consumeStringData, cashStringData };
	    
		
		for(Node stringData : allStringData){
			
			int id = -1;
			
			for(Node child : stringData.getChildren()){
				if(stringData.getName().equals("Eqp")){
					for(Node cat : child){
						id = Integer.parseInt(cat.getName());
						ids.add(id);
					}
				}else{
					id = Integer.parseInt(child.getName());
					ids.add(id);
				}
				
			}
			
			
		}
		
		return ids;
	}
	
	public static SkillBookData getSkillBookData(int itemId) {
		SkillBookData data = null;
		
		Node item = getItemData(itemId);
		
		if(item == null){
			throw new IllegalArgumentException(itemId+" is unknown.");
		}
		
		Node info = item.getChild("info");
		if(info == null){
			throw new IllegalArgumentException(itemId+" has no skill information! Is it even a skillbook?");
		}
		
		int masterLevel = info.readInt("masterLevel", 0);
		int reqLevel = info.readInt("reqSkillLevel", 0);
		int successRate = info.readInt("success", 0);
		Node skillData = info.getChild("skill");
		
		int[] skills = new int[skillData.getChildren().size()];
		
		for(int i = 0; i < skills.length;i++){
			int skillId = skillData.readInt(String.valueOf(i), 0);
			
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
		Node data = getItemData(itemId);
		Node info = data.readNode("info");
        boolean bRestricted = info.readInt("tradeBlock", 0) == 1;
        if (!bRestricted) {
        	bRestricted = info.readInt("accountSharable", 0) == 1;
        }
        if (!bRestricted) {
            bRestricted = info.readInt("quest", 0) == 1;
        }
		return bRestricted;
	}

	public static CashShopItemData getCashShopItemData(int entryId){
		return getInstance().cashShopItemCache.get(entryId);
	}

	public static CashShopItemData getCashShopItemDataByItemId(int itemId) {
		return getCashShopItemData(getInstance().cashShopItemCacheByItemId.get(itemId));
	}

	public static CashShopPackage getCashShopPackage(int boughtId) {
		return getInstance().cashShopPackages.get(boughtId);
	}

	public static int getProjectileWatkBonus(int itemId) {
		Node data = getItemData(itemId);
		int atk = data.readNode("info").readInt("incPAD", 0);
	
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
		/*Node questInfo = MapleQuest.getQuestData().getData("QuestInfo.img");
		Node questData = questInfo.getChild(String.valueOf(questId));
		int medal = questData.readInt("viewMedalItem", -1);
		return medal;*/
		return 0;
	}

	public static int getItemWidth(int id) {
/*		Node itemData = getItemData(id);
		
		Node infoData = itemData.getChild("info");
		if(infoData == null){
			return 0;
		}
		
		MapleCanvas canvas = (MapleCanvas) infoData.getChild("icon").getData();
		
		return canvas.getWidth();*/
		return 20;
	}
	
	
	
	public static boolean isConsumedOnPickup(int itemId){
		
		Node data = getItemData(itemId);
		
		if(data.hasChild("spec")){
			return data.readByte("spec/consumeOnPickup", (byte) 0) == 1;
		}
		
		return false;
		
	}

	public static int getMonsterBookItemId(int cardId) {
		return 2380000 + cardId;
	}
	
}
