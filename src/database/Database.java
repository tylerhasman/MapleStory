package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import maplestory.server.MapleStory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import config.MapleServerConfiguration;

public class Database {

	private HikariDataSource ds;
	
	private static Logger log = LoggerFactory.getLogger("[Database]");
	
	public Database(String jdbc, String username, String password) {
		ds = new HikariDataSource(getConfig(jdbc, username, password));
	}
	
	public boolean isValid(){
		return ds != null && !ds.isClosed();
	}
	
	public Connection getNewConnection() throws SQLException{
		try{
			return ds.getConnection();	
		}finally{
		}
	}
	
	public int execute(String script, Object... args) throws SQLException{
		return executeWithKeys(script, false, args).getNumRowsChanged();
	}
	
	public ExecuteResult execute(BatchedScript script, boolean returnGeneratedKeys) throws SQLException {
		if(script.getBatches().size() == 0) {
			return new ExecuteResult(0, Collections.emptyList());
		}
		long start = System.currentTimeMillis();
		try(Connection con = getNewConnection()){
			
			try(PreparedStatement ps = con.prepareStatement(script.getScript(), returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)){
				
				for(int batchId : script.getBatches().keySet()) {
					
					Object[] args = script.getBatches().get(batchId);
					
					for(int i = 0; i < args.length;i++){
						if(args[i] == null) {
							throw new IllegalArgumentException("Cannot pass unknown 'null' value to mysql. Use PossibleNullValue class");
						}
						if(args[i] instanceof PossibleNullValue) {
							PossibleNullValue pnv = (PossibleNullValue) args[i];
							
							ps.setObject(i + 1, pnv.getValue(), pnv.getSqlType());
						}else {
							ps.setObject(i + 1, args[i]);	
						}
					}
					
					ps.addBatch();
					
				}
				
				int nRowsChanged = Arrays.stream(ps.executeBatch()).sum();
				List<Integer> generatedKeys = null;
				
				if(returnGeneratedKeys){
					try(ResultSet keys = ps.getGeneratedKeys()){
						
						generatedKeys = new ArrayList<>();
						
						while(keys.next()){
							generatedKeys.add(keys.getInt(1));
						}
						
					}
					
				}
				
				return new ExecuteResult(nRowsChanged, generatedKeys);
				
			}
		}catch(SQLException e){
			log.error("Issue with script '"+script+"'");
			throw e;
		}finally{
			if(MapleStory.getServerConfig().isVerboseDatabaseEnabled())
				log.debug(script+" executed in "+(System.currentTimeMillis() - start)+" ms");
		}
	}
	
	public ExecuteResult executeWithKeys(String script, boolean returnGeneratedKeys, Object... args) throws SQLException{
		BatchedScript batch = new BatchedScript(script);
		batch.addBatch(args);
		
		return execute(batch, returnGeneratedKeys);
	}
	
	public List<QueryResult> query(String script, Object... args) throws SQLException{
		long start = System.currentTimeMillis();
		try(Connection con = getNewConnection()){
			
			try(PreparedStatement ps = con.prepareStatement(script)){
				
				for(int i = 0; i < args.length;i++){
					ps.setObject(i + 1, args[i]);
				}
				
				return createResults(ps.executeQuery());
				
			}
			
		}finally{
			if(MapleStory.getServerConfig().isVerboseDatabaseEnabled())
				log.debug(script+" query executed in "+(System.currentTimeMillis() - start)+" ms");
		}
		
	}
	
	private static List<QueryResult> createResults(ResultSet set) throws SQLException{
		
		List<QueryResult> results = new ArrayList<>();
		
		while(set.next()){
			results.add(new QueryResult(set));
		}
		
		return results;
		
	}
	
	public HikariConfig getConfig(String jdbc, String username, String password){
		
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbc);
		config.setUsername(username);
		config.setPassword(password);
		
		config.setConnectionTimeout(30000);
		
		config.setIdleTimeout(10000);
		
		config.setMaximumPoolSize(30);
		
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.setConnectionTestQuery("SELECT 1");
		
		return config;
		
	}
	
}
