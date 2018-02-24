package maplestory.channel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
import maplestory.world.World;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tools.TimerManager;

public class MapleCrossWorldChannel implements MapleChannel {


	private int id;
	
	protected MapleMapFactory mapFactory;
	
	private Logger logger;
	
	private int virtualPort;
	
	public MapleCrossWorldChannel(int worldId, int id, int virtualPort) {
		this.id = id;
		mapFactory = new MapleMapFactory(worldId, id);
		
		this.virtualPort = virtualPort;
		
		logger = LoggerFactory.getLogger("[Cross-Channel "+(id + 1)+"]");
		
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
		List<MapleCharacter> chr = new ArrayList<>();
		
		for(MapleMap map : mapFactory.getLoadedMaps()) {
			chr.addAll(map.getPlayers());
		}
		
		return chr;
	}

	@Override
	public int getConnectedPlayerCount() {
		return mapFactory.getLoadedMaps().stream().mapToInt(map -> map.getPlayers().size()).sum();
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
				
				InetAddress address = InetAddress.getByName(MapleStory.getServerConfig().getChannelServerIp());
				
				client.sendPacket(PacketFactory.getChannelChange(address, virtualPort));
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
			
			InetAddress address = InetAddress.getByName(MapleStory.getServerConfig().getChannelServerIp());
			
			client.sendChannelAddress(address, virtualPort, sessionId);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public MapleMap getMap(int id) {
		return mapFactory.getMap(id);
	}
	
	@Override
	public World getWorld() {
		throw new NotImplementedException();
	}

}
