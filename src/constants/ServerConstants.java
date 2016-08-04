package constants;

import java.util.concurrent.TimeUnit;

public class ServerConstants {

	public static final String SERVER_NAME = "TobiMS";
	
	public static final int MAPLE_VERSION = 83;
	
	public static final String JDBC = "jdbc:mysql://127.0.0.1/maplestory?autoReconnect=true&useSSL=false";
	public static final String DB_USER = "root";
	public static final String DB_PASS = "";

	public static final int CHANNEL_LOAD = 50;

	public static final boolean ENABLE_PIC = true;

	public static final int CHARACTER_SLOTS = 6;

	public static final String CHANNEL_SERVER_IP = "23.16.34.98";
	
	public static final boolean ALLOW_BEGINNER_PARTIES = false;

	public static final boolean KICK_ON_ERROR = false;

	public static final boolean CACHE_SCRIPTS = false;
	
	public static final boolean VERBOSE_DATABASE = false;

	public static final boolean ENABLE_MAP_UNLOADING = true;
	public static final long EMPTY_MAP_UNLOAD_TIME = 5000;//the number of milliseconds before a map will unload if there are no players in it

	public static final int MAP_NEIGHBOR_UNLOAD_THRESHHOLD = 1;//If a map has more loaded neighbor maps than this number it wont be unloaded 

	public static final long DEFAULT_CASH_ITEM_EXPIRE_TIME = TimeUnit.DAYS.toMillis(15);

	public static final boolean CACHE_CASH_SHOP_WALLETS = true;
	
	public static final boolean ENABLE_AUTO_SAVE = true;
	public static final long AUTO_SAVE_INTERVAL = TimeUnit.MINUTES.toMillis(5);
	
	public static final int EXP_RATE = 50;
	public static final int MESO_RATE = 10;
	public static final int QUEST_EXP_RATE = 10;

	public static final int LOGIN_PORT = 8484;
	public static final int CHANNEL_PORT = 8485;
	
}
