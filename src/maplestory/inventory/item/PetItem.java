package maplestory.inventory.item;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import maplestory.util.StringUtil;

public interface PetItem extends CashItem {

	public String getPetName();
	
	public void setPetName(String name);
	
	public int getCloseness();
	
	public void setCloseness(int closeness);
	
	public int getPetLevel();
	
	public void setPetLevel(int level);
	
	public int getFullness();
	
	public void setFullness(int fullness);
	
	public boolean isSummoned();
	
	public void setSummoned(boolean summoned);
	
	public PetDataSnapshot createPetSnapshot();
	
	@Data @AllArgsConstructor
	public static class PetDataSnapshot {
		private String petName;
		private int closeness, petLevel, fullness;
		private boolean summoned;
		
		public String serialize(){
			Map<String, Object> map = new HashMap<>();
			
			map.put("name", petName);
			map.put("closeness", closeness);
			map.put("petLevel", petLevel);
			map.put("fullness", fullness);
			map.put("summoned", summoned);
			
			return map.toString();
		}
		
		public void deserialize(String data) {
			
			Map<String, String> map = StringUtil.toMap(data, str -> str);
			
			petName = map.get("name");
			closeness = Integer.parseInt(map.getOrDefault("closeness", "0")); 
			petLevel = Integer.parseInt(map.getOrDefault("petLevel", "0")); 
			fullness = Integer.parseInt(map.getOrDefault("fullness", "0")); 
			summoned = Boolean.parseBoolean(map.getOrDefault("summoned", "false"));
			
		}
	}

	
}
