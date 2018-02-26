package maplestory.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import tools.TimerManager;
import constants.MessageType;
import lombok.Getter;
import maplestory.channel.MapleChannel;
import maplestory.channel.MapleCrossWorldChannel;
import maplestory.channel.MapleSocketChannel;
import maplestory.channel.MapleVirtualChannel;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapFactory;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MapleConnectionHandler;
import maplestory.world.MapleGlobalWorld;
import maplestory.world.MapleWorld;
import maplestory.world.World;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class MapleServer implements Runnable{

	public static final int CROSS_WORLD_ID = 99;
	
	private static boolean crashed = true;
	
	@Getter
	private static MapleServer instance;

	private List<World> worlds;
	
	private ChannelFuture serverChannel;
	
	@Getter
	private EventLoopGroup eventLoopGroupBoss, eventLoopGroupWorker;
	
	private World globalWorld;
	
	public MapleServer(int numWorlds) {
		if(instance != null){
			throw new IllegalStateException("MapleServer already created!");
		}
		eventLoopGroupBoss = new NioEventLoopGroup();
		eventLoopGroupWorker = new NioEventLoopGroup();
		instance = this;
		globalWorld = new MapleGlobalWorld();
		worlds = new ArrayList<>(numWorlds);
		for(int i = 0; i < numWorlds;i++){
			
			int channels = MapleStory.getServerConfig().getWorldConfigurations().get(i).getChannels();
			
			MapleWorld world = new MapleWorld(i, channels, eventLoopGroupBoss, eventLoopGroupWorker);
			
			worlds.add(world);
		
			world.getLogger().info("Loading events");
			world.reloadEvents();
			
		}
		
		if(MapleStory.getServerConfig().isAutoSaveEnabled()){
			TimerManager.scheduleRepeatingTask(new Runnable() {
				
				@Override
				public void run() {
					MapleStory.getLogger().info("Auto saving");
					for(World world : worlds){
						world.broadcastMessage(MessageType.LIGHT_BLUE_TEXT, "The world is currently auto-saving, please expect momentary lag");
						for(MapleCharacter chr : world.getPlayerStorage().getAllPlayers()){
							try {
								chr.saveToDatabase(false);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
				}
				
			}, MapleStory.getServerConfig().getAutoSaveInterval(), MapleStory.getServerConfig().getAutoSaveInterval(), TimeUnit.MILLISECONDS);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(crashed){
					MapleStory.getLogger().info("Crash detected, saving player data");
					
					for(World world : worlds){
						for(MapleCharacter chr : world.getPlayerStorage().getAllPlayers()){
							try {
								chr.saveToDatabase(false);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
						
				}
				
			}
		}));
		
		Thread consoleListener = new Thread(new ConsoleListener());
		
		consoleListener.setDaemon(true);
		consoleListener.start();
	}
	

	
	public static List<World> getWorlds() {
		return instance.worlds;
	}
	
	public static World getWorld(int id) {
		if(id == CROSS_WORLD_ID) {
			return getInstance().globalWorld;
		}
		if(getWorlds().size() <= id || id < 0) {
			return null;
		}
		return getWorlds().get(id);
	}
	
	public void shutdown(){
		eventLoopGroupWorker.shutdownGracefully();
		eventLoopGroupBoss.shutdownGracefully();
		MapleStory.getLogger().info("Closing login channel");
		serverChannel.channel().close();
		for(World world : worlds){
			world.getLogger().info("Cleaning up world.");
			world.getGuilds().forEach(guild -> guild.saveGuild());
			for(MapleChannel channel : world.getChannels()){
				channel.getLogger().info("Cleaning up channel");
				channel.shutdown();
			}
		}
		MapleStory.getLogger().info("Shutting down tasks");
		TimerManager.shutdown();
		crashed = false;
		MapleStory.getLogger().info("Server shutdown");
	}
	
	@Override
	public void run() {

		ServerBootstrap b = new ServerBootstrap();
		
		b.group(eventLoopGroupBoss, eventLoopGroupWorker)
		.channel(NioServerSocketChannel.class)
		.childHandler(new ChannelInitializer<SocketChannel>() {
			
			protected void initChannel(SocketChannel channel) throws Exception {
				
				channel.pipeline().addLast(new MapleConnectionHandler(-1, -1));
				
			};
			
		})
		.option(ChannelOption.SO_BACKLOG, 128)
		.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		try {
			serverChannel = b.bind(MapleStory.getServerConfig().getLoginPort()).sync();
			MapleStory.getLogger().info("Login server bound to port "+MapleStory.getServerConfig().getLoginPort());
			
			serverChannel.channel().closeFuture().sync();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			eventLoopGroupWorker.shutdownGracefully();
			eventLoopGroupBoss.shutdownGracefully();
			MapleStory.getLogger().info("Login server closed...");
		}
		
		
	}

	public int getOnlinePlayerCount() {
		
		int online = 0;
		
		for(World world : getWorlds()){
			for(MapleChannel ch : world.getChannels()){
				online += ch.getConnectedPlayerCount();
			}
		}
		
		return online;
	}

	public static MapleChannel getCrossWorldChannel(){
		return getInstance().globalWorld.getChannelById(0);
	}
	
	public static MapleChannel getChannel(int world, int channel) {
		return getWorld(world).getChannelById(channel);
	}

	
	
}

