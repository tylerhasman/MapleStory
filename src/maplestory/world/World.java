package maplestory.world;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;

import constants.MessageType;
import maplestory.channel.MapleChannel;
import maplestory.client.MapleClient;
import maplestory.client.MapleMessenger;
import maplestory.guild.MapleGuild;
import maplestory.life.MapleHiredMerchant;
import maplestory.party.MapleParty;
import maplestory.player.MapleCharacter;
import maplestory.server.net.handlers.channel.RangedAttackHandler;
import maplestory.world.MapleWorld.EventFlag;
import maplestory.world.RateManager.Rates;

public interface World {
	
	public void broadcastMessage(MessageType type, String msg);
	
	public void broadcastPacket(byte[] data);
	
	public MapleMessenger createMessenger(MapleCharacter chr);
	
	public MapleChannel getChannelById(int id);
	
	public int getChannelCount();
	
	public List<MapleChannel> getChannels();
	
	public EventFlag getEventFlag();
	
	public String getEventMessage(MapleClient client);
	
	public MapleGuild getGuild(int id);
	
	public Collection<MapleGuild> getGuilds();
	
	public int getId();
	
	public Logger getLogger();
	
	public int getMaxPlayers();
	
	public MapleHiredMerchant getMerchantByOwner(int id);
	
	public MapleMessenger getMessenger(int id);
	
	public String getName();
	
	public Collection<MapleParty> getParties();
	
	public MapleParty getParty(int id);
	
	public PlayerStorage getPlayerStorage();
	
	public int getPlayerCount();
	
	public RankManager getRankManager();
	
	public RateManager getRates();
	
	public int getVirtualPort();
	
	public void registerGuild(MapleGuild guild);
	
	public void registerMerchant(MapleHiredMerchant merchant);
	
	public void registerParty(MapleParty party);
	
	public void unregisterGuild(MapleGuild guild);
	
	public void unregisterMerchant(MapleHiredMerchant merchant);
	
	public void unregisterParty(MapleParty party);
	
	
}
