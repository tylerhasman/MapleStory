package maplestory.world;

import io.netty.channel.EventLoopGroup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.spi.SyncResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.MessageType;
import constants.ServerConstants;
import constants.SmegaType;
import database.MapleDatabase;
import database.QueryResult;
import lombok.Getter;
import maplestory.channel.MapleChannel;
import maplestory.client.MapleClient;
import maplestory.guild.GuildNotFoundException;
import maplestory.guild.MapleGuild;
import maplestory.party.MapleParty;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleServer;
import maplestory.server.net.PacketFactory;

public class World {

	private List<MapleChannel> channels;
	private int id;
	@Getter
	private PlayerStorage playerStorage;
	
	private Map<Integer, MapleParty> parties;
	
	private Map<Integer, MapleGuild> guilds;
	
	private Logger logger;
	
	public World(int id, int numChannels, EventLoopGroup eventLoopGroupBoss, EventLoopGroup eventLoopGroupWorker) {
		this.id = id;
		logger = LoggerFactory.getLogger("["+getName()+"]");
		playerStorage = new PlayerStorage();
		parties = Collections.synchronizedMap(new HashMap<>());
		guilds = Collections.synchronizedMap(new HashMap<>());
		loadAllGuilds();
		channels = new ArrayList<>(numChannels);
		for(int i = 0; i < numChannels;i++){
			channels.add(new MapleChannel(i, ServerConstants.CHANNEL_PORT+i, this, eventLoopGroupBoss, eventLoopGroupWorker));
		}
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	private void loadAllGuilds() {
		try {
			List<QueryResult> guildIds = MapleDatabase.getInstance().query("SELECT `id` FROM `guilds`");
			
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
		synchronized (parties) {
			return parties.get(id);
		}
	}
	
	public void registerParty(MapleParty party){
		synchronized (parties) {
			parties.put(party.getPartyId(), party);
		}
	}
	
	public void unregisterParty(MapleParty party){
		if(!party.isDisbanded()){
			party.disband();
		}
		
		synchronized (parties) {
			parties.remove(party.getPartyId());
		}
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
		return ServerConstants.CHANNEL_LOAD * getChannelCount();
	}
	
	public void broadcastPacket(byte[] packet){
		for(MapleChannel ch : channels){
			ch.broadcastPacket(packet);
		}
	}

	public String getName() {
		if(id == 0){
			return "Scania";
		}else{
			return "Unknown "+id;
		}
	}

	public String getEventMessage(MapleClient client) {
		
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		String am_pm = cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
		
		StringBuffer buf = new StringBuffer();
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
	
}
