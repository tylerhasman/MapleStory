package maplestory.server;

import java.sql.SQLException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import maplestory.client.MapleClient;
import constants.ServerConstants;
import database.MapleDatabase;

public class Register {

	public static void main(String[] args) throws SQLException {
		
		Scanner scanner = new Scanner(System.in);
		
		Logger logger = LoggerFactory.getLogger(Register.class);
		
		logger.info("Enter username: ");
		String username = scanner.nextLine();
		logger.info("Enter password: ");
		String pw = scanner.nextLine();
		
		MapleDatabase.createInstance(ServerConstants.JDBC, ServerConstants.DB_USER, ServerConstants.DB_PASS);
		
		boolean result = MapleClient.registerAccount(username, pw);
		
		if(result){
			logger.info("Account created!");
		}else{
			logger.info("Account failed to create, another account already has the username "+username);
		}
		
		scanner.close();
		
	}
	
}
