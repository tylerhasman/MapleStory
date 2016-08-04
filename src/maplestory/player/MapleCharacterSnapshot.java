package maplestory.player;

import java.sql.SQLException;
import java.util.List;

import constants.LoginStatus;
import database.MapleDatabase;
import database.QueryResult;
import lombok.Data;
import lombok.ToString;
import maplestory.server.MapleServer;
import maplestory.world.World;

@ToString
@Data
public class MapleCharacterSnapshot {

	private final String name;
	private final int job;
	private final int id;
	private final int level;
	private final int channel, world;
	private final int mapId;
	
	MapleCharacterSnapshot(MapleCharacter chr){
		name = chr.getName();
		job = chr.getJob().getId();
		id = chr.getId();
		level = chr.getLevel();
		channel = chr.getClient().getChannelId();
		world = chr.getClient().getWorldId();
		mapId = chr.getMapId();
	}
	
	private MapleCharacterSnapshot(String name, int job, int id, int level, int world, int mapId) {
		this.name = name;
		this.job = job;
		this.id = id;
		this.level = level;
		this.world = world;
		this.mapId = mapId;
		channel = -1;
	}
	
	
	/**
	 * Initialize a blank snapshot
	 */
	public MapleCharacterSnapshot(){
		name = "";
		job = 0;
		id = 0;
		level = 0;
		channel = 0;
		world = 0;
		mapId = 0;
	}
	
	/**
	 * Not guaranteed to return a character, may return null if they are no longer online
	 * @return the character
	 */
	public MapleCharacter getLiveCharacter(){
		if(world == -1) {
			return null;
		}
		World w = MapleServer.getWorld(world);
		
		if(w == null){
			return null;
		}
		
		return w.getPlayerStorage().getById(id);
	}
	
	public boolean isOnline(){
		MapleCharacter live = getLiveCharacter();
		
		if(live != null){
			return live.getClient().getLoginStatus() == LoginStatus.IN_GAME;
		}
		
		return false;
	}
	
	public static MapleCharacterSnapshot createDatabaseSnapshot(int characterId){
		MapleCharacterSnapshot snapshot = null;
		
		try {
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `name`,`job`,`level`,`map`,`world` FROM `characters` WHERE `id`=?", characterId);
		
			if(results.size() == 0){
				return null;
			}
			
			QueryResult result = results.get(0);
			
			String name = result.get("name");
			int job = result.get("job");
			int level = result.get("level");
			int map = result.get("map");
			int world = result.get("world");
			
			snapshot = new MapleCharacterSnapshot(name, job, characterId, level, world, map);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return snapshot;
	}


}
