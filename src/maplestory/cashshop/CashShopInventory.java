package maplestory.cashshop;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import database.MapleDatabase;
import database.QueryResult;
import lombok.ToString;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.MapleCashItem;
import maplestory.inventory.item.MapleEquipCashItem;
import maplestory.server.MapleStory;

@ToString
public class CashShopInventory {
	
	private List<CashItem> items;
	
	private final int accountId;
	
	CashShopInventory(int account) {
		items = new ArrayList<>();
		accountId = account;
	}
	
	
	public void addItem(Item item){
		if(!(item instanceof CashItem)){
			throw new IllegalArgumentException("Only cash items may be addeed! Attempted Addition: "+item.toString());
		}
		items.add((CashItem) item);
		commitChanges();
	}
	
	public void removeItem(Item item){
		items.remove(item);
		commitChanges();
	}
	
	public int numItems(){
		return items.size();
	}
	
	public Collection<CashItem> getItems(){
		return Collections.unmodifiableCollection(items);
	}
	
	public void commitChanges(){
		
		try {
			MapleDatabase.getInstance().execute("DELETE FROM `cash_items` WHERE `account`=?", accountId);
		
			for(CashItem item : items){
				MapleDatabase.getInstance().execute("INSERT INTO `cash_items` (`itemid`, `account`, `unique_id`, `amount`) VALUES (?, ?, ?, ?)", item.getItemId(), accountId, item.getUniqueId(), item.getAmount());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public Item findById(int itemId) {
		
		for(CashItem item : items){
			if(item.getUniqueId() == itemId){
				return item;
			}
		}
		
		return null;
	}
	
	public static CashShopInventory getCashInventory(int accountId){
		
		CashShopInventory inv = new CashShopInventory(accountId);
		
		try{
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `itemid`,`unique_id`,`amount` FROM `cash_items` WHERE `account`=?", accountId);
		
			for(QueryResult result : results){
				CashItem item = null;
				
				int itemId = result.get("itemid");
				int amount = result.get("amount");
				long uniqueId = result.get("unique_id");
				
				if(InventoryType.getByItemId(itemId) != InventoryType.EQUIP){
					item = new MapleCashItem(itemId, amount, null, MapleStory.getServerConfig().getDefaultCashItemExpireTime() + System.currentTimeMillis(), uniqueId);
				}else{
					item = new MapleEquipCashItem(itemId, amount, MapleStory.getServerConfig().getDefaultCashItemExpireTime() + System.currentTimeMillis(), uniqueId, ItemInfoProvider.getEquipInfo(itemId));
				}
				
				inv.items.add(item);
			}
		
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return inv;
		
	}



}
