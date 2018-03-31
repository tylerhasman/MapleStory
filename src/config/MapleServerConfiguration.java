package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.world.RateManager;
import maplestory.world.RateManager.Rates;

@Getter
public class MapleServerConfiguration {

	private int mapleVersion;
	
	private String jdbc, dbUser, dbPassword;
	
	private int channelLoad;
	
	private int characterSlots;
	
	private int worlds;
	
	private String channelServerIp;
	
	private boolean picEnabled;
	
	private boolean beginnerPartiesAllowed;
	
	private boolean playerKickedOnError;
	
	private boolean scriptCachingEnabled;
	
	private boolean verboseDatabaseEnabled;
	
	private boolean mapUnloadingEnabled;
	private long mapUnloadTime;
	private int mapNeighborThreshold;
	
	private long defaultCashItemExpireTime;
	
	private boolean cacheCashShopWalletsEnabled;
	
	private boolean autoSaveEnabled;
	private long autoSaveInterval;
	
	private int g_expRate, g_mesoRate, g_questExpRate, g_dropRate;//Global rates
	
	private int loginPort, channelPort;
	
	private boolean autoRegisterEnabled;
	
	private Map<Integer, WorldConfiguration> worldConfigurations;
	
	private boolean virtualChannelsEnabled;
	
	private Set<Integer> crossWorldMaps;
	
	private List<String> events;
	
	@Getter
	private int startingMapAran, startingMapBeginner, startingMapCygnus;
	
	public MapleServerConfiguration(File file) throws IOException {
		Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		
		mapleVersion = config.getInt("server_version");
		
		jdbc = config.getString("database.jdbc");
		dbUser = config.getString("database.user");
		dbPassword = config.getString("database.pass");
		verboseDatabaseEnabled = config.getBoolean("database.verbose");
		
		worlds = config.getInt("server.worlds");
		
		loginPort = config.getInt("server.login_port");
		channelPort = config.getInt("server.channel_ports");
		channelLoad = config.getInt("server.channel_load");
		
		picEnabled = config.getBoolean("server.pic_enabled");
		characterSlots = config.getInt("server.character_slots");
		channelServerIp = config.getString("server.channel_server_ip");
		
		virtualChannelsEnabled = config.getBoolean("server.virtual_channels");
		
		playerKickedOnError = config.getBoolean("server.kick_on_error");
		scriptCachingEnabled = config.getBoolean("server.cache_scripts");
		autoRegisterEnabled = config.getBoolean("server.auto_register");
		
		beginnerPartiesAllowed = config.getBoolean("game.allow_beginner_parties");
		
		mapUnloadingEnabled = config.getBoolean("game.map_unloading.enabled");
		mapUnloadTime = config.getLong("game.map_unloading.unload_time");
		mapNeighborThreshold = config.getInt("game.map_unloading.neighbor_threshold");
		
		startingMapAran = config.getInt("game.starting_maps.aran");
		startingMapBeginner = config.getInt("game.starting_maps.beginner");
		startingMapCygnus = config.getInt("game.starting_maps.cygnus");
		
		defaultCashItemExpireTime = config.getLong("game.default_cash_item_expire");
		
		cacheCashShopWalletsEnabled = config.getBoolean("game.cache_wallets");
		
		autoSaveEnabled = config.getBoolean("game.auto_save.enabled");
		autoSaveInterval = config.getLong("game.auto_save.interval");
		
		g_expRate = config.getInt("game.rates.exp");
		g_mesoRate = config.getInt("game.rates.meso");
		g_questExpRate = config.getInt("game.rates.quest");
		g_dropRate = config.getInt("game.rates.drop");
		
		crossWorldMaps = new HashSet<>(config.getIntList("cross_world_maps"));
		
		events = config.getStringList("events");
		
		worldConfigurations = new HashMap<>();
		
		for(int i = 0; i < worlds;i++){
			
			String name = config.getString("world."+i+".name");
			int channels = config.getInt("world."+i+".channels");

			int expRate = config.getInt("world."+i+".rates.exp", g_expRate);
			int mesoRate = config.getInt("world."+i+".rates.meso", g_mesoRate);
			int dropRate = config.getInt("world."+i+".rates.drop", g_dropRate);
			int questRate = config.getInt("world."+i+".rates.quest", g_questExpRate);
			
			worldConfigurations.put(i, new WorldConfiguration(name, channels, 
					new RateManager(Rates.builder()
					.exp(expRate)
					.meso(mesoRate)
					.drop(dropRate)
					.quest(questRate)
					.build())));
			
		}
		
	}
	
	public MapleServerConfiguration() {
		
	}

	public boolean isCrossWorldMap(int id) {
		return crossWorldMaps.contains(id);
	}
	
	public WorldConfiguration getWorldConfiguration(int id) {
		if(worldConfigurations.containsKey(id)){
			return worldConfigurations.get(id);
		}
		return new WorldConfiguration("Unknown", 0, RateManager.STATIC_RATE_MANAGER);
	}

	@Getter
	@AllArgsConstructor
	public static class WorldConfiguration {
		
		private final String name;
		private final int channels;
		private final RateManager rates;
		
	}
	
}
