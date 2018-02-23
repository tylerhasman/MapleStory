package maplestory.script;

import maplestory.map.MapleMap;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleServer;
import maplestory.server.MapleStory;
import maplestory.server.net.PacketFactory;

public class MapScriptManager extends AbstractScriptManager {

	private int map;
	private int channel;
	private int world;
	
	public MapScriptManager(MapleMap map, MapleCharacter chr) {
		super(chr);
		this.map = map.getMapId();
	}
	
	public MapleMap getMap(){
		return MapleServer.getChannel(world, channel).getMap(map);
	}
	
	public void grantGodlyStats(){
		getClient().sendPacket(PacketFactory.aranGodlyStats());
	}

}
