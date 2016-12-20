package maplestory.player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import database.ExecuteResult;
import database.MapleDatabase;
import database.QueryResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class MapleNote {

	private final int id;
	
	private final String from;
	private final String content;
	private final int fame;
	private final long creationTime;
	
	public static MapleNote createNote(MapleCharacter character, String from, String content, int fame){
		
		try{
			
			long creation = System.currentTimeMillis();
			ExecuteResult result = MapleDatabase.getInstance().executeWithKeys("INSERT INTO `notes` (`character`, `content`,`from`,`fame`,`creation_time`) VALUES (?, ?, ?, ?, ?)", true, character.getId(), content, from, fame, creation);
			
			int id = result.getGeneratedKeys().get(0);
			
			return new MapleNote(id, from, content, fame, creation);
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return new MapleNote(-1, from, content, fame, System.currentTimeMillis());
	}
	
	public static Collection<MapleNote> loadNotes(int characterId){
		
		List<MapleNote> notes = new ArrayList<>();
		
		try {
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `content`,`from`,`fame`,`id`,`creation_time` FROM `notes` WHERE `character`=?", characterId);
			
			for(QueryResult result : results){
				String content = result.get("content");
				String from = result.get("from");
				int fame = result.get("fame");
				int id = result.get("id");
				long creationTime = result.get("creation_time");
				
				notes.add(new MapleNote(id, from, content, fame, creationTime));
				
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return notes;
	}
	
}
