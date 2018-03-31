package maplestory.channel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.MessageType;
import maplestory.client.MapleClient;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapFactory;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleServer;
import maplestory.server.MapleStory;
import maplestory.server.net.PacketFactory;
import maplestory.world.MapleGlobalWorld;
import maplestory.world.World;
import tools.TimerManager;

public class MapleVirtualChannel implements MapleChannel {

	private int id;
	
	protected MapleMapFactory mapFactory;
	
	private Logger logger;
	
	private World world;
	
	private Map<String, MapleEvent> events;
	
	public MapleVirtualChannel(int id, World world) {
		this.id = id;
		this.world = world;
		events = new HashMap<>();
		mapFactory = new MapleMapFactory(world.getId(), id);
		
		logger = LoggerFactory.getLogger("["+world.getName()+" Channel "+(id + 1)+"]");
		
		if(MapleStory.getServerConfig().isMapUnloadingEnabled()){
			TimerManager.scheduleRepeatingTask(new Runnable() {
				
				@Override
				public void run() {
					mapFactory.unloadDeadMaps();
				}
				
			}, 1000, MapleStory.getServerConfig().getMapUnloadTime() + 5000, TimeUnit.MILLISECONDS);
		}
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public List<MapleCharacter> getPlayers() {
		return world.getPlayerStorage().getAllPlayers().
				stream().
				filter(pl -> pl.getClient().getChannelId() == id)
				.collect(Collectors.toList());
	}

	@Override
	public int getConnectedPlayerCount() {
		int count = 0;
		for(MapleCharacter character : world.getPlayerStorage().getAllPlayers()){
			if(character.getClient().getChannelId() == id){
				count++;
			}
		}
		return count;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof MapleVirtualChannel){
			MapleVirtualChannel other = (MapleVirtualChannel) obj;
			
			if(other.world == world && other.id == id){
				return true;
			}
		}
		
		return super.equals(obj);
	}

	@Override
	public void broadcastMessage(MessageType type, String text) {
		broadcastPacket(PacketFactory.getServerMessagePacket(type, text, id, false));
	}

	@Override
	public void broadcastPacket(byte[] packet) {
		for(MapleCharacter chr : getPlayers()){
			chr.getClient().sendPacket(packet);
		}
	}

	@Override
	public void shutdown() {
		for(MapleCharacter chr : getPlayers()){
			chr.getClient().closeConnection();
		}
	}

	@Override
	public MapleCharacter getPlayerById(int id) {
		for(MapleCharacter chr : getPlayers()){
			if(chr.getId() == id){
				return chr;
			}
		}
		return null;
	}

	@Override
	public MapleCharacter getPlayerByName(String name) {
		for(MapleCharacter chr : getPlayers()){
			if(chr.getName().equalsIgnoreCase(name)){
				return chr;
			}
		}
		return null;
	}

	@Override
	public void connect(MapleClient client) {
		
		if(client.getCharacter().isCashShopOpen()){
			try {
				int port = world.getVirtualPort();
				
				InetAddress address = InetAddress.getByName(MapleStory.getServerConfig().getChannelServerIp());
				
				client.sendPacket(PacketFactory.getChannelChange(address, port));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}else{
			int currentMapId = client.getCharacter().getMapId();
			
			MapleMap map = getMap(currentMapId);
			
			client.setChannelId(id);
			client.getCharacter().changeMap(map);	
		}
		
	}

	@Override
	public void initialConnection(MapleClient client, int sessionId) {
		
		try {
			int port = world.getVirtualPort();
			
			InetAddress address = InetAddress.getByName(MapleStory.getServerConfig().getChannelServerIp());
			
			client.sendChannelAddress(address, port, sessionId);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public MapleMap getMap(int id) {
		
		if(MapleStory.getServerConfig().isCrossWorldMap(id) && !(world instanceof MapleGlobalWorld)) {
			return MapleServer.getCrossWorldChannel().getMap(id);
		}
		
		return mapFactory.getMap(id);
	}
	
	@Override
	public boolean isMapLoaded(int id) {
		if(MapleStory.getServerConfig().isCrossWorldMap(id) && !(world instanceof MapleGlobalWorld)) {
			return MapleServer.getCrossWorldChannel().isMapLoaded(id);
		}
		
		return mapFactory.isMapLoaded(id);
	}
	
	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public Collection<MapleEvent> getEvents() {
		return events.values();
	}

	@Override
	public MapleEvent getEvent(String id) {
		return events.get(id);
	}
	
	@Override
	public void registerEvent(String id, MapleEvent event) {
		events.put(id, event);
	}

}
