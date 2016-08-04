package maplestory.inventory.storage;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.ExecuteResult;
import database.MapleDatabase;
import database.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.EquipItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.PetItem;
import maplestory.player.MapleCharacter;

public class MapleStorageBox {

	private Map<InventoryType, List<Item>> items;
	@Getter
	private int size;
	@Getter
	private int mesos;
	
	private WeakReference<MapleCharacter> owner;
	
	MapleStorageBox(int maxSize) {
		items = new HashMap<>();
		for(InventoryType inv : InventoryType.values()){
			items.put(inv, new ArrayList<>(size));
		}
	}
	
	public void addMesos(int amount){
		mesos += amount;
	}
	
	public static MapleStorageBox getStorage(MapleClient client) throws SQLException {
		
		List<QueryResult> clientResult = MapleDatabase.getInstance().query("SELECT `storage_meso`,`storage_size` FROM `accounts` WHERE `id`=?", client.getId());
		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT * FROM `storage_items` LEFT JOIN `storage_item_equip_data` ON storage_items.id=storage_item_equip_data.inventory_id WHERE `account`=?", client.getId());
		List<Item> items = new ArrayList<>();
		int maxSize = clientResult.get(0).get("storage_size");
		int mesos = clientResult.get(0).get("storage_meso");
		
		for(QueryResult itemResult : results){
			Item item = ItemFactory.getItem(itemResult);
			
			items.add(item);
		}
		
		MapleStorageBox inv = new MapleStorageBox(maxSize);
		inv.mesos = mesos;
		
		for(Item item : items){
			inv.addItem(item);
		}
		
		inv.size = maxSize;
		inv.owner = new WeakReference<MapleCharacter>(client.getCharacter());
		
		return inv;
	}

	public Item getItem(InventoryType type, int slot){
		return items.get(type).get(slot);
	}
	
	public void commitChanges(int accountId) throws SQLException {
		
		MapleDatabase.getInstance().execute("UPDATE `accounts` SET `storage_meso`=?,`storage_size`=? WHERE `id`=?", mesos, size, accountId);
		MapleDatabase.getInstance().execute("DELETE FROM `storage_items` WHERE `account`=?", accountId);
		
		int slot = 0;
		
		for(Item item : getItems()){
			saveItem(item, slot, accountId);
			slot++;
		}
		
	}
	
	private void saveItem(Item item, int slot, int accountId) throws SQLException{
		String script = "INSERT INTO `storage_items` (`inventory_type`,`slot`,`account`,`itemid`,`amount`,`owner`,`flag`,`expiration`,`unique_id`,`pet_id`) VALUES (?,?,?,?,?,?,?,?,?,?)";
    	
		InventoryType iv = InventoryType.getByItemId(item.getItemId());
		
    	long expirationDate = -1;
    	long unique_id = -1;
    	int petId = -1;

		if(item instanceof CashItem){
			expirationDate = ((CashItem)item).getExpirationDate();
			unique_id = ((CashItem)item).getUniqueId();
		}
		
		if(item instanceof PetItem){
			petId = (int) ((PetItem)item).getUniqueId();
		}
    	
    	ExecuteResult result = MapleDatabase.getInstance().executeWithKeys(script, true, iv.getId(), slot, accountId, item.getItemId(), item.getAmount(), item.getOwner(), item.getFlag(), expirationDate, unique_id, petId);
    	int dbID = result.getGeneratedKeys().get(0);
    	if(item instanceof EquipItem){
    		EquipItem eq = (EquipItem) item;
    		String script2 = "INSERT INTO `storage_item_equip_data` (`inventory_id`,`upgradeslots`,`level`,`str`,`dex`,`int`,`luk`,`hp`,`mp`,`watk`,`matk`,`wdef`,`mdef`,`acc`,`avoid`,`hands`,`speed`,`jump`,`hammer`,`itemlevel`,`itemexp`,`ringid`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    		MapleDatabase.getInstance().execute(script2, dbID, 
    				eq.getUpgradeSlotsAvailble(), eq.getLevel(),
    				eq.getStr(), eq.getDex(), eq.getInt(),
    				eq.getLuk(), eq.getHp(), eq.getMp(),
    				eq.getWeaponAttack(), eq.getMagicAttack(), eq.getWeaponDefense(),
    				eq.getMagicDefense(), eq.getAccuracy(), eq.getAvoid(),
    				eq.getHands(), eq.getSpeed(), eq.getJump(), eq.getHammerUpgrades(),
    				eq.getItemLevel(), 0, -1);
    	}
	}


	public int getDepositItemCost() {
		return 500;
	}

	public void addItem(Item item) {
		items.get(InventoryType.getByItemId(item.getItemId())).add(item);
	}

	public boolean isFull() {
		return sumOfItems() >= size;
	}
	
	public int sumOfItems(){
		int sum = 0;
		for(InventoryType inv : items.keySet()){
			sum += items.get(inv).size();
		}
		
		return sum;
	}

	public int getWithdrawItemCost() {
		if(owner == null){
			return 0;
		}
		
		MapleCharacter chr = owner.get();
		
		if(chr == null){
			return 0;
		}
		
		if(chr.getMapId() == 910000000){
			return 1000;
		}
		
		return 0;
	}

	public void removeItem(InventoryType type, int slot) {
		items.get(type).remove(slot);
	}

	public Collection<Item> getItems() {
		
		List<Item> combined = new ArrayList<>();
		
		for(InventoryType inv : items.keySet()){
			combined.addAll(items.get(inv));
		}
		
		return combined;
	}
	
	public Collection<Item> getItems(InventoryType type){
		return items.get(type);
	}
	
	@AllArgsConstructor
	public static enum StoragePacketType {
		
		OPEN(0x16),
		MESO_UPDATE(0x13),
		ERR_PLAYER_INVENTORY_FULL(0x0A),
		ERR_STORAGE_FULL(0x11),
		ERR_NOT_ENOUGH_MESOS(0x0B),
		ERR_ONE_OF_A_KIND(0x0C),
		ADD_ITEM(0xD),
		TAKE_OUT_ITEM(0x9)
		;
		
		@Getter
		private final int code;
		
		
	}


	
}
