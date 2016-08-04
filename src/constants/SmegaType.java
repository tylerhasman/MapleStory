package constants;

import lombok.Getter;

public enum SmegaType {

	REGULAR(5072000),
	HEARTS(5073000),
	SKULL(5074000),
	DIABLO(5390000),
	C9(5390001),
	LOVE(5072002),
	TRIPLE(5077000),
	CUTE_TIGER(5390005),
	ANGRY_TIGER(5390006)
	;
	
	@Getter
	private final int itemId;
	
	SmegaType(int id) {
		itemId = id;
	}
	
}
