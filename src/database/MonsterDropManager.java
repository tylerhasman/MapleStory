package database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.MonsterDropManager.MonsterDrop;
import lombok.Data;
import lombok.ToString;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.player.monsterbook.MonsterBook;
import maplestory.server.MapleStory;
import maplestory.util.Randomizer;

public class MonsterDropManager {

	private static MonsterDropManager instance = null;
	
	private LocalDatabase data;
	
	private Map<Integer, List<MonsterDrop>> cache;
	private List<MonsterDrop> globalDrops;
	
	MonsterDropManager() {
		try {
			Class.forName("org.sqlite.JDBC");
			data = new LocalDatabase("jdbc:sqlite:monster_drops.db", "", "");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		cache = new HashMap<>();
		globalDrops = new ArrayList<>();
		loadGlobalDrops();
	}
	
	private void loadGlobalDrops(){
		try {
			globalDrops.clear();
			List<QueryResult> results = data.query("SELECT * FROM `global_drop_data`");
			
			for(QueryResult result : results){
				
				int id = result.get("itemid");
				int chance = result.get("chance");
				int min = result.get("min_drop");
				int max = result.get("max_drop");
				
				MonsterDrop drop = MonsterDrop.create(id, chance, min, max);
				
				globalDrops.add(drop);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static MonsterDropManager getInstance(){
		if (instance == null) {
			instance = new MonsterDropManager();
		}

		return instance;
	}
	
	public List<MonsterDrop> getGlobalDrops() {
		return globalDrops;
	}
	
	public synchronized List<MonsterDrop> getPossibleDrops(int monsterId){
		
		if(cache.containsKey(monsterId)){
			return cache.get(monsterId);
		}
		
		List<MonsterDrop> drops = new ArrayList<>();	
		
		try{
			
			List<QueryResult> results = data.query("SELECT * FROM `drop_data` WHERE `monsterid`=?", monsterId);
			
			for(QueryResult result : results){
				
				int id = result.get("itemid");
				int chance = result.get("chance");
				int min = result.get("min_drop");
				int max = result.get("max_drop");
				
				MonsterDrop drop = MonsterDrop.create(id, chance, min, max);
				
				drops.add(drop);
				
			}
			
		}catch(SQLException e){
			
		}
		
		int cardId = MonsterBook.getMonsterCardId(monsterId);
		
		if(cardId >= 0){
			int itemId = ItemInfoProvider.getMonsterBookItemId(cardId);
			drops.add(MonsterDrop.create(itemId, MonsterBook.getCardDropChance(monsterId), 1, 1));
		}
		
		cache.put(monsterId, drops);
		
		return drops;
	}

	@ToString(includeFieldNames=true)
	@Data
	public static class MonsterDrop {
		
		public static final int MAX_CHANCE = 1000000;
		
		private final Item item;
		private final int chance;
		private final int min, max;
		
		public boolean shouldDrop(int dropRate){
			return (Randomizer.nextInt(MAX_CHANCE) * dropRate) <= chance;
		}
		
		public int getAmount(){
			int amount = Randomizer.nextInt(max);
			
			if(amount < min){
				amount = min;
			}
			
			return amount;
		}
		
		public Item getItem(){
			if(item == null){
				return null;
			}
			return item.copy();
		}

		public int getItemId() {
			if(item == null){
				return 0;
			}
			return item.getItemId();
		}

		public static MonsterDrop create(int itemId, int chance, int max, int min){
			if(itemId == 0){
				return new MonsterDrop(null, chance, min, max);
			}
			return create(ItemFactory.getItem(itemId, 1), chance, max, min);
		}
		
		public static MonsterDrop create(Item item, int chance, int max, int min) {
			return new MonsterDrop(item, chance, min, max);
		}
		
	}
	
}
