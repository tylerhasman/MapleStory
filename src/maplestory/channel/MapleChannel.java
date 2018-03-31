package maplestory.channel;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;

import constants.MessageType;
import maplestory.client.MapleClient;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapFactory;
import maplestory.player.MapleCharacter;
import maplestory.world.World;

public interface MapleChannel {

	public int getId();
	
	public MapleMap getMap(int id);
	
	public boolean isMapLoaded(int id);
	
	public Logger getLogger();
	
	public List<MapleCharacter> getPlayers();
	
	public int getConnectedPlayerCount();
	
	public void broadcastMessage(MessageType type, String text);
	
	public void broadcastPacket(byte[] packet);
	
	public void shutdown();
	
	public MapleCharacter getPlayerById(int id);
	
	public MapleCharacter getPlayerByName(String name);
	
	public void connect(MapleClient client);
	
	public void initialConnection(MapleClient client, int sessionId);
	
	public World getWorld();
	
	public Collection<MapleEvent> getEvents();
	
	public MapleEvent getEvent(String id);
	
	public void registerEvent(String id, MapleEvent event);
	
}
