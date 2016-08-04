package maplestory.inventory.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import database.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import maplestory.inventory.item.ItemStatInfo.ItemStat;

@EqualsAndHashCode(callSuper = false)
@Data
public class EquipItemInfo extends ItemStatInfo {

	private int upgradeSlots;
	private int level;
	private int itemLevel;
	private int hammerUpgrades;
	
	private int requiredLevel, requiredJob, requiredStr, requiredDex, requiredInt, requiredLuk;
	private int requiredFame;
	
	private boolean untradeableOnEquip;
	
	public EquipItemInfo() {
		itemLevel = 1;
	}
	
	@Override
	public Map<String, Integer> serialize() {
		Map<String, Integer> data = super.serialize();
		
		data.put("upgradeSlots", upgradeSlots);
		data.put("level", level);
		data.put("itemLevel", itemLevel);
		data.put("hammerUpgrades", hammerUpgrades);
		
		return data;
	}
	
	@Override
	public void deserialize(Map<String, Integer> data) {
		super.deserialize(data);
		
		level = data.getOrDefault("level", 0);
		itemLevel = data.getOrDefault("itemLevel", 0);
		hammerUpgrades = data.getOrDefault("hammerUpgrades", 0);
		upgradeSlots = data.getOrDefault("upgradeSlots", 0);
	}
	
	public String toString(){
		return super.toString();
	}
	
	public EquipItemInfo(ItemStatInfo statInfo) {
		super(statInfo);
		if(statInfo instanceof EquipItemInfo){
			EquipItemInfo eq = (EquipItemInfo) statInfo;
			upgradeSlots = eq.upgradeSlots;
			level = eq.level;
			itemLevel = eq.itemLevel;
			hammerUpgrades = eq.hammerUpgrades;
			requiredLevel = eq.requiredLevel;
			requiredJob = eq.requiredJob;
			requiredStr = eq.requiredStr;
			requiredDex = eq.requiredDex;
			requiredInt = eq.requiredInt;
			requiredLuk = eq.requiredLuk;
			requiredFame = eq.requiredFame;
		}else{
			itemLevel = 1;
		}
	}
	
/*	public EquipItemInfo(QueryResult result) {
		super(result);
		itemLevel = result.get("itemlevel");
		hammerUpgrades = result.get("hammer");
		upgradeSlots = result.get("upgradeslots");
	}*/

	private static interface GetStat {
		public int getStat(EquipItemInfo info);
	}

	public void addStats(ItemStatInfo bonus) {
		Map<ItemStat, Integer> stats = bonus.getStats();
		
		for(ItemStat stat : stats.keySet()){
			int mine = stat.getStat(this);
			int other = stat.getStat(bonus);
			
			stat.setStat(this, mine + other);
		}
		
	}
	
	@AllArgsConstructor
	public static enum EquipStat {
		
		UPGRADE_SLOTS("tuc", (e, v) -> e.setUpgradeSlots(v), e -> e.getUpgradeSlots()),
		RJOB("reqJob", (e, v) -> e.setRequiredJob(v), e -> e.getRequiredJob()),
		RSTR("reqSTR", (e, v) -> e.setRequiredStr(v), e -> e.getRequiredStr()),
		RDEX("reqDEX", (e, v) -> e.setRequiredDex(v), e -> e.getRequiredDex()),
		RINT("reqINT", (e, v) -> e.setRequiredInt(v), e -> e.getRequiredInt()),
		RLUK("reqLUK", (e, v) -> e.setRequiredLuk(v), e -> e.getRequiredLuk()),
		RFAME("reqPOP", (e, v) -> e.setRequiredFame(v), e -> e.getRequiredFame()),
		UNTRADEABLE_ON_EQUIP("equipTradeBlock", (e, v) -> e.setUntradeableOnEquip(v == 1), e -> e.isUntradeableOnEquip() ? 1 : 0)
		;

		private static final Map<String, EquipStat> byWzName = new HashMap<>();
		
		private final String wzName;
		private final BiConsumer<EquipItemInfo, Integer> setStat;
		private final GetStat getStat;
		
		static{
			for(EquipStat value : values()){
				byWzName.put(value.wzName, value);
			}
		}
		
		public void setStat(EquipItemInfo info, int value){
			setStat.accept(info, value);
		}
		
		public int getStat(EquipItemInfo info){
			return getStat.getStat(info);
		}
		
		public static EquipStat getByWzName(String wzName){
			return byWzName.get(wzName);
		}
		
	}

}
