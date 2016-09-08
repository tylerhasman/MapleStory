package maplestory.server;

import java.sql.SQLException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMConfiguration;

import lombok.Getter;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.skill.SkillFactory;
import constants.ServerConstants;
import database.MapleDatabase;
import database.MonsterDropManager;

public class MapleStory {
	
	@Getter
	private static Logger logger = LoggerFactory.getLogger("[MapleStory]");
	
	public static void main(String[] args) {
		
		System.setProperty("wzpath", "wz/");
		
		long timeToTake = System.currentTimeMillis();
        
		logger.info("Loading items");
		
		ItemInfoProvider.getInstance();
		ItemInfoProvider.loadCashShop();
		
		logger.info("Items loaded in "+((System.currentTimeMillis() - timeToTake) / 1000.0)+" seconds");
		
		timeToTake = System.currentTimeMillis();
		
        logger.info("Loading skills");
        
        SkillFactory.loadAllSkills();
        
        logger.info("Skills loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
        
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

}
