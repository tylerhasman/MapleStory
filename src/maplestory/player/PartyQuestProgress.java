package maplestory.player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import database.MapleDatabase;
import database.QueryResult;
import lombok.Getter;
import lombok.Setter;

public class PartyQuestProgress {

	@Getter @Setter
	private int riceCakesGiven;
	@Getter @Setter
	private boolean riceHatGiven;
	
	private List<String> obtainedPartyQuestItems;
	
	PartyQuestProgress() {
		riceCakesGiven = 0;
		riceHatGiven = false;
		obtainedPartyQuestItems = new ArrayList<>();
	}
	
	public void addPartyQuestItem(String id) {
		if(hasPartyQuestItem(id)) {
			return;
		}
		obtainedPartyQuestItems.add(id);
	}
	
	public boolean hasPartyQuestItem(String id) {
		return obtainedPartyQuestItems.contains(id);
	}
	
	public void removePartyQuestItem(String id) {
		obtainedPartyQuestItems.remove(id);
	}
	
	private String serializeObtained() {
		
		String str = "";
		
		for(String s : obtainedPartyQuestItems) {
			str += s+",";
		}
		
		return str;
	}
	
	private static List<String> deserializeObtained(String serialized){
		
		return Arrays.asList(serialized.split(","));
		
	}
	
	public void saveToDatabase(int characterId) throws SQLException{
		
		if(MapleDatabase.getInstance().query("SELECT * FROM `party_quest` WHERE `player`=?", characterId).size() == 0) {
			
			MapleDatabase.getInstance().execute("INSERT INTO `party_quest` (`player`, `items`) VALUES (?, ?)", characterId, "");
			
		}
		
		MapleDatabase.getInstance().execute("UPDATE `party_quest` SET `rice_cakes_given`=?,`rice_hat_given`=?, `items`=? WHERE `player`=?", riceCakesGiven, riceHatGiven, serializeObtained(), characterId);
		
	}
	
	public static PartyQuestProgress getPartyQuestProgress(int characterId){
		
		PartyQuestProgress progress = new PartyQuestProgress();
		
		try{
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `rice_cakes_given`,`rice_hat_given`,`items` FROM `party_quest` WHERE `player`=?", characterId);
			
			if(results.size() > 0){
				
				QueryResult result = results.get(0);
				
				progress.riceCakesGiven = result.get("rice_cakes_given");
				progress.riceHatGiven = (int) result.get("rice_hat_given") == 1;
				progress.obtainedPartyQuestItems = deserializeObtained(result.get("items"));
				
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return progress;
		
	}
	
}
