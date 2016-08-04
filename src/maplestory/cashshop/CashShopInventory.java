package maplestory.cashshop;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.MapleDatabase;
import database.QueryResult;
import lombok.Getter;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;

public class CashShopInventory {

	@Getter
	private List<Item> items;
	
	CashShopInventory() {
		items = new ArrayList<>();
	}
	
	public void commitChanges(){
		//TODO
	}
	
	public static CashShopInventory getCashInventory(int accountId){
		
		CashShopInventory inv = new CashShopInventory();
		
		try{
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `itemid` FROM `cash_items` WHERE `account`=?", accountId);
		
			for(QueryResult result : results){
				Item item = ItemFactory.getItem(result.get("itemid"), 1);
				
				inv.items.add(item);
			}
		
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return inv;
		
	}

}
