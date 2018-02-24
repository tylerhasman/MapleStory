package maplestory.world;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.MessageType;
import maplestory.channel.MapleChannel;
import maplestory.channel.MapleVirtualChannel;
import maplestory.client.MapleClient;
import maplestory.client.MapleMessenger;
import maplestory.guild.MapleGuild;
import maplestory.inventory.storage.MapleStorageBox;
import maplestory.life.MapleHiredMerchant;
import maplestory.party.MapleParty;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleServer;
import maplestory.server.MapleStory;
import maplestory.world.MapleWorld.EventFlag;
import maplestory.world.RateManager.Rates;

public class MapleGlobalWorld implements World {

	private MapleChannel channel;
	
	private Logger logger;
	
	private PlayerStorage players;
	
	private RateManager rates;
	
	public MapleGlobalWorld() {
		channel = new MapleVirtualChannel(0, this);
		logger = LoggerFactory.getLogger("Cross-World");
		players = new PlayerStorage();
		rates = new RateManager(Rates.builder()
				.exp(MapleStory.getServerConfig().getG_expRate())
				.drop(MapleStory.getServerConfig().getG_dropRate())
				.quest(MapleStory.getServerConfig().getG_questExpRate())
				.meso(MapleStory.getServerConfig().getG_mesoRate())
				.build());
	}
	
	@Override
	public void broadcastMessage(MessageType type, String msg) {
		channel.broadcastMessage(type, msg);
	}

	@Override
	public void broadcastPacket(byte[] data) {
		channel.broadcastPacket(data);
	}

	@Override
	public MapleMessenger createMessenger(MapleCharacter chr) {
		return new MapleMessenger(chr);
	}

	@Override
	public MapleChannel getChannelById(int id) {
		return channel;
	}

	@Override
	public int getChannelCount() {
		return 1;
	}

	@Override
	public List<MapleChannel> getChannels() {
		return Collections.singletonList(channel);
	}

	@Override
	public EventFlag getEventFlag() {
		return EventFlag.NONE;
	}

	@Override
	public String getEventMessage(MapleClient client) {
		return "Cross-World "+client.getId();
	}

	@Override
	public MapleGuild getGuild(int id) {
		return null;
	}

	@Override
	public Collection<MapleGuild> getGuilds() {
		return Collections.emptyList();
	}

	@Override
	public int getId() {
		return MapleServer.CROSS_WORLD_ID;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public int getMaxPlayers() {
		return 9999999;
	}

	@Override
	public MapleHiredMerchant getMerchantByOwner(int id) {
		return null;
	}

	@Override
	public MapleMessenger getMessenger(int id) {
		return null;
	}

	@Override
	public String getName() {
		return "Cross-World";
	}

	@Override
	public Collection<MapleParty> getParties() {
		return Collections.emptyList();
	}

	@Override
	public MapleParty getParty(int id) {
		return null;
	}

	@Override
	public PlayerStorage getPlayerStorage() {
		return players;
	}

	@Override
	public int getPlayerCount() {
		return players.getAllPlayers().size();
	}

	@Override
	public RankManager getRankManager() {
		return null;
	}

	@Override
	public RateManager getRates() {
		return rates;
	}

	@Override
	public int getVirtualPort() {
		return MapleStory.getServerConfig().getChannelPort();
	}

	@Override
	public void registerGuild(MapleGuild guild) {
		
	}

	@Override
	public void registerMerchant(MapleHiredMerchant merchant) {

	}

	@Override
	public void registerParty(MapleParty party) {

	}

	@Override
	public void unregisterGuild(MapleGuild guild) {

	}

	@Override
	public void unregisterMerchant(MapleHiredMerchant merchant) {

	}

	@Override
	public void unregisterParty(MapleParty party) {

	}

}
