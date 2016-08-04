package database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class QueryResult {

	private Map<String, Object> objects;
	private String countKey;
	
	protected QueryResult(ResultSet set) throws SQLException {
		
		ResultSetMetaData metadata = set.getMetaData();
		
		objects = new HashMap<>(metadata.getColumnCount());
		
		for(int i = 0; i < metadata.getColumnCount();i++){
			String key = metadata.getColumnName(i + 1);
			
			if(key.startsWith("COUNT(")){
				countKey = key;
			}
			
			objects.put(key, set.getObject(i + 1));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key){
		if(isNull(key)){
			return null;
		}
		return (T) objects.get(key);
	}
	
	public boolean isNull(String key){
		return objects.get(key) == null;
	}
	
	public long getCountResult(){
		return (long) objects.get(countKey);
	}
	
	@Override
	public String toString() {
		
		String s = "";
		
		for(String key : objects.keySet()){
			s += key + "="+objects.get(s)+" ## ";
		}
		
		return s;
	}
	
}
