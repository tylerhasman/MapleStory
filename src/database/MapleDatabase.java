package database;

import java.sql.Connection;
import java.sql.SQLException;

import lombok.Getter;

public class MapleDatabase extends Database {

	@Getter
	private static MapleDatabase instance;
	
	protected MapleDatabase(String jdbc, String username, String password) {
		super(jdbc, username, password);
	}
	
	public static void createInstance(String jdbc, String username, String pw){
		instance = new MapleDatabase(jdbc, username, pw);
	}
	
	public static Connection getConnection() throws SQLException{
		return instance.getNewConnection();
	}
	
}
