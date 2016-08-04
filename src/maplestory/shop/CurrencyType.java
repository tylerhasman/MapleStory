package maplestory.shop;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum CurrencyType {

	MESOS((byte) 0),
	PITCH((byte) 1);
	
	@Getter
	private final byte id;
	
}
