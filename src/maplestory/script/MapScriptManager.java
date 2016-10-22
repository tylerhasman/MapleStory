package maplestory.script;

import maplestory.map.MapleMap;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MapScriptManager extends AbstractScriptManager {

	private int map;
	
	public MapScriptManager(MapleMap map, MapleCharacter chr) {
		super(chr);
		this.map = map.getMapId();
	}
	
	public MapleMap getMap(){
		return getCharacter().getClient().getChannel().getMapFactory().getMap(map);
	}
	
	public void grantGodlyStats(){
		getClient().sendPacket(PacketFactory.aranGodlyStats());
	}

}
