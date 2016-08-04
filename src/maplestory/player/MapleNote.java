package maplestory.player;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MapleNote {

	private final int id;
	
	private final String from;
	private final String content;
	private final int fame;
	private final long creationTime;
	
}
