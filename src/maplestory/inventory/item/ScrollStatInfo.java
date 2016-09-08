package maplestory.inventory.item;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ScrollStatInfo extends ItemStatInfo {

	@Getter @Setter
	private int destroyRate, successRate;
	
	@Getter @Setter
	private List<Integer> itemsUseableOn;

	public ScrollStatInfo(ItemStatInfo info, int dr, int sr) {
		super(info);
		destroyRate = dr;
		successRate = sr;
	}
	
	public boolean isItemUseableOn(int itemId){
		if(itemsUseableOn == null){
			return true;
		}
		return itemsUseableOn.contains(itemId);
	}
	
}
