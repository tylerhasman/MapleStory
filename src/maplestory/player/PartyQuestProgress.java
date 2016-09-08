package maplestory.player;

import java.sql.SQLException;
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
	
	PartyQuestProgress() {
		riceCakesGiven = 0;
		riceHatGiven = false;
	}
	
	public void saveToDatabase(int characterId) throws SQLException{
		
		MapleDatabase.getInstance().execute("UPDATE `party_quest` SET `rice_cakes_given`=?,`rice_hat_given`=? WHERE `player`=?", riceCakesGiven, riceHatGiven, characterId);
		
	}
	
	public static PartyQuestProgress getPartyQuestProgress(int characterId){
		
		PartyQuestProgress progress = new PartyQuestProgress();
		
		try{
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `rice_cakes_given`,`rice_hat_given` FROM `party_quest` WHERE `player`=?", characterId);
			
			if(results.size() > 0){
				
				QueryResult result = results.get(0);
				
				progress.riceCakesGiven = result.get("rice_cakes_given");
				progress.riceHatGiven = (int) result.get("rice_hat_given") == 1;
				
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return progress;
		
	}
	
}
