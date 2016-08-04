package maplestory.inventory.item;

import lombok.Getter;
import lombok.Setter;

public class ItemMeta {

	@Getter @Setter
	private String giftFrom = "";
	
	@Override
	public ItemMeta clone() {
		ItemMeta other = new ItemMeta();
		other.giftFrom = giftFrom;
		
		return other;
	}
	
}
