package maplestory.channel;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.MessageType;

import tools.TimerManager;
import lombok.Getter;
import maplestory.map.MapleMapFactory;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleStory;
import maplestory.server.net.MapleConnectionHandler;
import maplestory.server.net.PacketFactory;
import maplestory.world.World;

public class MapleChannel {

	@Getter
	private int id;
	@Getter
	private int port;
	@Getter
	private Channel nettyChannel;
	@Getter
	private World world;
	@Getter
	private MapleMapFactory mapFactory;
	@Getter
	private Logger logger;
	
	//private EventLoopGroup boss, worker;
	
	public MapleChannel(int id, int port, World world, EventLoopGroup eventLoopGroupBoss, EventLoopGroup eventLoopGroupWorker) {
		mapFactory = new MapleMapFactory(world.getId(), id);
		this.id = id;
		this.port = port;
		this.world = world;
		logger = LoggerFactory.getLogger("["+world.getName()+" Channel "+(id + 1)+"]");
		/*boss = new NioEventLoopGroup();
		worker = new NioEventLoopGroup();*/
		
		ServerBootstrap b = new ServerBootstrap();
		
		b.group(eventLoopGroupBoss, eventLoopGroupWorker)
		.channel(NioServerSocketChannel.class)
		.childHandler(new ChannelInitializer<SocketChannel>() {
			
			protected void initChannel(SocketChannel channel) throws Exception {
				channel.pipeline().addLast(new MapleConnectionHandler(world.getId(), id));
			};
			
		})
		.option(ChannelOption.SO_BACKLOG, 128);
		
		nettyChannel = b.bind(port).channel();
		if(nettyChannel.isOpen()){
			logger.info("Bound to "+port);	
		}else{
			logger.error("Failed to bind to port "+port);
		}
		
		if(MapleStory.getServerConfig().isMapUnloadingEnabled()){
			TimerManager.scheduleRepeatingTask(new Runnable() {
				
				@Override
				public void run() {
					mapFactory.unloadDeadMaps();
				}
				
			}, 1000, MapleStory.getServerConfig().getMapUnloadTime() + 5000, TimeUnit.MILLISECONDS);
		}
		

	}
	
	public List<MapleCharacter> getPlayers(){
		return world.getPlayerStorage().getAllPlayers().
				stream().
				filter(pl -> pl.getClient().getChannelId() == id)
				.collect(Collectors.toList());
	}
	
	public int getConnectedPlayerCount(){
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
		
		if(obj instanceof MapleChannel){
			MapleChannel other = (MapleChannel) obj;
			
			if(other.world == world && other.id == id){
				return true;
			}
		}
		
		return super.equals(obj);
	}
	
	public void broadcastMessage(MessageType type, String text){
		broadcastPacket(PacketFactory.getServerMessagePacket(type, text, id, false));
	}
	
	public void broadcastPacket(byte[] packet){
		for(MapleCharacter chr : getPlayers()){
			chr.getClient().sendPacket(packet);
		}
	}

	public void shutdown() {
		for(MapleCharacter chr : getPlayers()){
			logger.debug("Saving "+chr.getName()+" to database before shutting down.");
			chr.getClient().closeConnection();
		}
		try {
			nettyChannel.close().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public MapleCharacter getPlayerById(int id) {
		for(MapleCharacter chr : getPlayers()){
			if(chr.getId() == id){
				return chr;
			}
		}
		return null;
	}
	
	public MapleCharacter getPlayerByName(String name){
		for(MapleCharacter chr : getPlayers()){
			if(chr.getName().equalsIgnoreCase(name)){
				return chr;
			}
		}
		return null;
	}
	
}
