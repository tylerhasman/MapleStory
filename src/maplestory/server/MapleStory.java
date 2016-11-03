package maplestory.server;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.skill.SkillFactory;
import me.tyler.mdf.MapleFile;
import constants.ServerConstants;
import database.MapleDatabase;
import database.MonsterDropManager;

public class MapleStory {
	
	@Getter
	private static Logger logger = LoggerFactory.getLogger("[MapleStory]");
	
	private static final File dataSourceFolder = new File("wz/");
	
	private static final Map<String, MapleFile> dataFiles = new HashMap<>();
	
	private static void loadMapleData(){

		loadMapleFile("Map.mdf");
		loadMapleFile("Character.mdf");
		loadMapleFile("Mob.mdf");
		loadMapleFile("Npc.mdf");
		loadMapleFile("Skill.mdf");
		loadMapleFile("String.mdf");
		loadMapleFile("Etc.mdf");
		loadMapleFile("Item.mdf");
		loadMapleFile("Quest.mdf");
		loadMapleFile("Reactor.mdf");
		

	}
	
	public static void main(String[] args) {

		long timeToTake = System.currentTimeMillis();
		
		loadMapleData();	
        
		logger.info("Loading items");
		
		ItemInfoProvider.loadCashShop();
		
        logger.info("Loading skills");
        
        SkillFactory.loadAllSkills();
        
        logger.info("Creating monster drop database connection");
        
        MonsterDropManager.getInstance();
        
        logger.info("Creating database connection pool.");
		
		try{
			MapleDatabase.createInstance(ServerConstants.JDBC, ServerConstants.DB_USER, ServerConstants.DB_PASS);
			MapleDatabase.getInstance().execute("UPDATE `accounts` SET `loggedin`=?", 0);
			logger.info("Database connection successful!");
		}catch(RuntimeException e){
			logger.error("Failed to start database,\r\n"+e);
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		MapleServer server = new MapleServer(1);
		
		logger.info("Server started in "+((System.currentTimeMillis() - timeToTake) / 1000.0)+" seconds");
		
		server.run();
		
	}

	private static MapleFile loadMapleFile(String path){

		File f = new File(dataSourceFolder, path);
		
		logger.info("Loading "+f.getName());
		
		MapleFile file;
		try {
			file = new MapleFile(f, false, true);
			dataFiles.put(path, file);
		} catch (IOException e) {
			logger.error("Failed to load "+f.getAbsolutePath(), e);
			return null;
		}
		
		return file;
	}
	
	public static MapleFile getDataFile(String fileName) {
		
		if(dataFiles.containsKey(fileName)){
			return dataFiles.get(fileName);
		}
		
		return loadMapleFile(fileName);
	}

}
