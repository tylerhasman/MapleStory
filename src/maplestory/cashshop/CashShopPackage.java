package maplestory.cashshop;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.inventory.item.ItemInfoProvider;

@AllArgsConstructor
public class CashShopPackage {

	@Getter
	private final int packageId;
	
	private final int[] cashShopEntryIds;
	
	public List<CashShopItemData> getItems(){
		
		List<CashShopItemData> items = new ArrayList<>(cashShopEntryIds.length);
		
		for(int i : cashShopEntryIds){
			
			items.add(ItemInfoProvider.getCashShopItemData(i));
			
		}
		
		return items;
		
	}
	
}
