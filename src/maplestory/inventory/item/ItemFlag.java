package maplestory.inventory.item;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ItemFlag {

	LOCK(0x01),
	/**
	 * If an item has this flag then they won't slip on ice
	 */
	SPIKES(0x02),
	COLD(0x04),
	UNTRADEABLE(0x08),
	KARMA(0x10),
	UNKN(0x40),
	PET_COME(0x80),
	ACCOUNT_SHARING(0x100)
	;
	
	@Getter
	private final int bit;
	
	
	
}
