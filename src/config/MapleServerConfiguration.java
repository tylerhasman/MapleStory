package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
	
	private int expRate, mesoRate, questExpRate, dropRate;
	
	private int loginPort, channelPort;
	
	private boolean autoRegisterEnabled;
	
	private Map<Integer, WorldConfiguration> worldConfigurations;
	
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
		
		playerKickedOnError = config.getBoolean("server.kick_on_error");
		scriptCachingEnabled = config.getBoolean("server.cache_scripts");
		autoRegisterEnabled = config.getBoolean("server.auto_register");
		
		beginnerPartiesAllowed = config.getBoolean("game.allow_beginner_parties");
		
		mapUnloadingEnabled = config.getBoolean("game.map_unloading.enabled");
		mapUnloadTime = config.getLong("game.map_unloading.unload_time");
		mapNeighborThreshold = config.getInt("game.map_unloading.neighbor_threshold");
		
		defaultCashItemExpireTime = config.getLong("game.default_cash_item_expire");
		
		cacheCashShopWalletsEnabled = config.getBoolean("game.cache_wallets");
		
		autoSaveEnabled = config.getBoolean("game.auto_save.enabled");
		autoSaveInterval = config.getLong("game.auto_save.interval");
		
		expRate = config.getInt("game.rates.exp");
		mesoRate = config.getInt("game.rates.meso");
		questExpRate = config.getInt("game.rates.quest");
		dropRate = config.getInt("game.rates.drop");
		
		worldConfigurations = new HashMap<>();
		
		for(int i = 0; i < worlds;i++){
			
			String name = config.getString("world."+i+".name");
			int channels = config.getInt("world."+i+".channels");
			
			worldConfigurations.put(i, new WorldConfiguration(name, channels));
			
		}
		
	}
	
	public MapleServerConfiguration() {
		
	}

	public WorldConfiguration getWorldConfiguration(int id) {
		if(worldConfigurations.containsKey(id)){
			return worldConfigurations.get(id);
		}
		return new WorldConfiguration("Unknown", 0);
	}

	@Getter
	@AllArgsConstructor
	public static class WorldConfiguration {
		
		private final String name;
		private final int channels;
		
	}
	
}
