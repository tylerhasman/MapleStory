package maplestory.world;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.MapleServerConfiguration;
import constants.MessageType;
import database.MapleDatabase;
import database.QueryResult;
import lombok.Getter;
import maplestory.channel.MapleChannel;
import maplestory.channel.MapleSocketChannel;
import maplestory.channel.MapleVirtualChannel;
import maplestory.client.MapleClient;
import maplestory.client.MapleMessenger;
import maplestory.guild.GuildNotFoundException;
import maplestory.guild.MapleGuild;
import maplestory.life.MapleHiredMerchant;
import maplestory.party.MapleParty;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleServer;
import maplestory.server.MapleStory;
import maplestory.server.net.MapleConnectionHandler;
import maplestory.server.net.PacketFactory;
import tools.TimerManager;
import tools.TimerManager.MapleTask;

public class World {

	private List<MapleChannel> channels;
	private int id;
	@Getter
	private PlayerStorage playerStorage;
	
	private Map<Integer, MapleParty> parties;
	
	private Map<Integer, MapleGuild> guilds;
	
	private Map<Integer, MapleMessenger> messengers;
	
	private Map<Integer, MapleHiredMerchant> merchants;
	
	private Logger logger;
	
	@Getter
	private RateManager rates;
	
	@Getter
	private EventFlag eventFlag;
	
	@Getter
	private RankManager rankManager;
	
	private Channel virtualChannelSocket;
	private int virtualPort;
	
	public World(int id, int numChannels, EventLoopGroup eventLoopGroupBoss, EventLoopGroup eventLoopGroupWorker) {
		this.id = id;
		logger = LoggerFactory.getLogger("["+getName()+"]");
		playerStorage = new PlayerStorage();
		parties = Collections.synchronizedMap(new HashMap<>());
		guilds = Collections.synchronizedMap(new HashMap<>());
		messengers = Collections.synchronizedMap(new HashMap<>());
		merchants = Collections.synchronizedMap(new HashMap<>());
		loadAllGuilds();
		channels = new ArrayList<>(numChannels);
		for(int i = 0; i < numChannels;i++){
			if(!MapleStory.getServerConfig().isVirtualChannelsEnabled()){
				channels.add(new MapleSocketChannel(i, MapleStory.getNextChannelPort(), this, eventLoopGroupBoss, eventLoopGroupWorker));
			}else{
				channels.add(new MapleVirtualChannel(i, this));
			}
		}
		eventFlag = EventFlag.NONE;
		
		if(MapleStory.getServerConfig().isVirtualChannelsEnabled()){
			ServerBootstrap b = new ServerBootstrap();
			
			b.group(eventLoopGroupBoss, eventLoopGroupWorker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				
				protected void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new MapleConnectionHandler(id, 0));
				};
				
			})
			.option(ChannelOption.SO_BACKLOG, 128);
			
			virtualPort = MapleStory.getNextChannelPort();
			
			virtualChannelSocket = b.bind(virtualPort).channel();
			if(virtualChannelSocket.isOpen()){
				logger.info("Virtual Channel bound to "+virtualPort);	
			}else{
				logger.error("Virtual Channel failed to bind to port "+virtualPort);
			}
		}
		rates = MapleStory.getServerConfig().getWorldConfiguration(id).getRates();
		rankManager = new RankManager(this);
		rankManager.updateRankings();
		
		TimerManager.schedule(() -> rankManager.updateRankings(), 1, TimeUnit.HOURS);//Update every hour
	}
	
	public Channel getVirtualChannelSocket() {
		if(!MapleStory.getServerConfig().isVirtualChannelsEnabled()){
			throw new IllegalStateException("virtual channels are not enabled");
		}
		return virtualChannelSocket;
	}
	
	public int getVirtualPort() {
		if(!MapleStory.getServerConfig().isVirtualChannelsEnabled()){
			throw new IllegalStateException("virtual channels are not enabled");
		}
		return virtualPort;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	private void loadAllGuilds() {
		try {
			List<QueryResult> guildIds = MapleDatabase.getInstance().query("SELECT `id` FROM `guilds` WHERE `world`=?", id);
			
			for(QueryResult result : guildIds){
				int id = result.get("id");
				
				try {
					MapleGuild guild = MapleGuild.loadFromDatabase(id);
					
					guilds.put(guild.getGuildId(), guild);
					
				} catch (GuildNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		logger.info("Loaded "+guilds.size()+" guilds.");
	}
	
	public int getId() {
		return id;
	}
	
	public Collection<MapleParty> getParties(){
		synchronized (parties) {
			return parties.values();	
		}
	}
	
	public List<MapleChannel> getChannels() {
		return channels;
	}
	
	public MapleParty getParty(int id){
		return parties.get(id);
	}
	
	public void registerParty(MapleParty party){
		parties.put(party.getPartyId(), party);
	}
	
	public void unregisterParty(MapleParty party){
		if(!party.isDisbanded()){
			party.disband();
		}
		
		parties.remove(party.getPartyId());
	}
	
	public void registerMerchant(MapleHiredMerchant merchant) {
		merchants.put(merchant.getOwnerId(), merchant);
	}
	
	public void unregisterMerchant(MapleHiredMerchant merchant) {
		merchants.remove(merchant.getOwnerId());
		merchant.remove();
	}
	
	public MapleHiredMerchant getMerchantByOwner(int ownerId) {
		return merchants.get(ownerId);
	}
	
	public MapleChannel getChannelById(int id){
		for(MapleChannel mc : channels){
			if(mc.getId() == id){
				return mc;
			}
		}
		return null;
	}
	
	public int getChannelCount(){
		return channels.size();
	}
	
	public int getPlayerCount(){
		int count = 0;
		for(MapleChannel ch : channels){
			count += ch.getConnectedPlayerCount();
		}
		return count;
	}
	
	public int getMaxPlayers(){
		return MapleStory.getServerConfig().getChannelLoad() * getChannelCount();
	}
	
	public void broadcastPacket(byte[] packet){
		for(MapleChannel ch : channels){
			ch.broadcastPacket(packet);
		}
	}

	public String getName() {
		return MapleStory.getServerConfig().getWorldConfiguration(id).getName();
	}

	public String getEventMessage(MapleClient client) {
		
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		String am_pm = cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
		
		StringBuilder buf = new StringBuilder();
		buf.append("Welcome #b"+client.getUsername()+"\r\n\r\n");
		buf.append("#kThe time is now "+hour+":"+String.format("%02d", minute)+" "+am_pm+"\r\n");
		buf.append("There are "+MapleServer.getInstance().getOnlinePlayerCount()+" players online.\r\n");

		buf.append("#rHappy Mapling");
		
		return buf.toString();
	}
	
	public MapleGuild getGuild(int guildId){
		synchronized (guilds) {
			return guilds.get(guildId);
		}
	}
	
	public Collection<MapleGuild> getGuilds(){
		synchronized (guilds) {
			return guilds.values();
		}
	}

	public void registerGuild(MapleGuild newGuild) {
		synchronized (guilds) {
			guilds.put(newGuild.getGuildId(), newGuild);
		}
	}
	
	public void unregisterGuild(MapleGuild guild){
		synchronized (guilds) {
			guilds.remove(guild.getGuildId());
		}
	}

	public void broadcastMessage(MessageType type, String msg) {
		broadcastPacket(PacketFactory.getServerMessagePacket(type, msg, 1, false));
	}
	
	public MapleMessenger createMessenger(MapleCharacter creator){
		MapleMessenger ms = new MapleMessenger(creator);
		
		messengers.put(ms.getUniqueId(), ms);
		
		return ms;
	}
	
	public void createMapleTvEvent(MapleCharacter main, MapleCharacter partner, int type, int duration, String... messages){

		List<String> messageList = new ArrayList<>(Arrays.asList(messages));
		
		while(messageList.size() < 5){
			messageList.add("");
		}
		
		broadcastPacket(PacketFactory.enabledTv());
		broadcastPacket(PacketFactory.sendTv(main, messageList, type, partner));
		
		TimerManager.schedule(() -> {
			broadcastPacket(PacketFactory.removeTv());
		}, duration);
		
	}

	public MapleMessenger getMessenger(int messengerId) {
		return messengers.get(messengerId);
	}
	
	public static enum EventFlag {
		
		NONE,
		EVENT,
		NEW,
		HOT;
		
	}
	
}
