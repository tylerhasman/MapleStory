package maplestory.guild;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class MapleGuildRank {

	@Getter @Setter(value=AccessLevel.PROTECTED)
	private String name;
	
}
