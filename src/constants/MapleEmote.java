package constants;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;


public enum MapleEmote {

	
	DEFAULT(0),
	SMIRK(1),
	BIG_SMILE(2),
	SARCASTIC(3),
	CRYING(4),
	ANGRY(5),
	OOPS(6),
	EYES_CLOSED(7),
	SMOOCH(11, true),
	DRAGON_BREATH(21, true),
	FLAMING(15, true),
	RAY(16, true),
	BLEH(22, true),
	SPARKLING_EYES(14, true),
	SWEETNESS(10, true),
	WINK(12, true),
	GOO_GOO(17, true),
	HEART_EYES(GOO_GOO.id, true),
	SICK(8, true),
	QUEASY(SICK.id, true),
	GHOST(18, true),
	WHOA_WHOA(GHOST.id, true),
	DROOL(20, true),
	OUCH(13, true),
	CONSTANT_SIGH(19, true),
	SIGH(CONSTANT_SIGH.id, true)
	;
	
	@Getter
	private final int id;
	
	@Getter
	private final boolean cashShop;
	
	private static final Map<Integer, MapleEmote> emotes = new HashMap<>();
	
	static{
		for(MapleEmote emote : values()){
			emotes.put(emote.id, emote);
		}
	}
	
	private MapleEmote(int id) {
		this(id, false);
	}
	
	private MapleEmote(int id, boolean cash) {
		this.id = id;
		cashShop = cash;
	}
	
	public static MapleEmote byId(int id){
		return emotes.get(id);
	}
	
}
