package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SpecialEffect {

	LEVEL_UP(0),
	PORTAL_SOUND(7),
	JOB_CHANGE(8),
	QUEST_COMPLETE(9);
	
	@Getter
	private final int packetId;
	
}
