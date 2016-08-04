package maplestory.inventory.item;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ScrollResult {

	private final boolean success;
	private final boolean destroyed;
	private final EquipItem result;
	
}
