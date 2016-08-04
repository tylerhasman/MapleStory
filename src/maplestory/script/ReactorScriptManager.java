package maplestory.script;

import lombok.Getter;
import maplestory.map.MapleMap;
import maplestory.map.MapleReactor;
import maplestory.player.MapleCharacter;

public class ReactorScriptManager extends AbstractScriptManager {

	@Getter
	private MapleReactor reactor;
	
	public ReactorScriptManager(MapleCharacter chr, MapleReactor reactor) {
		super(chr);
		this.reactor = reactor;
	}
	
	
	@Override
	public MapleMap getMap() {
		return reactor.getMap();
	}

}
