package maplestory.inventory.item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import database.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import maplestory.util.StringUtil;

@ToString
@Data
public class ItemStatInfo {
	private int str, dex, int_, luk;
	private int hp, mp;
	private int weaponAttack, magicAttack;
	private int weaponDefense, magicDefense;
	private int accuracy;
	private int avoid, speed, hands, jump;
	private boolean cash;
	private boolean fs;
	
	public ItemStatInfo(){
		
	}
	
	public ItemStatInfo(ItemStatInfo info){
		str = info.str;
		dex = info.dex;
		int_ = info.int_;
		luk = info.luk;
		hp = info.hp;
		mp = info.mp;
		weaponAttack = info.weaponAttack;
		magicAttack = info.magicAttack;
		weaponDefense = info.weaponDefense;
		magicDefense = info.magicDefense;
		accuracy = info.accuracy;
		avoid = info.avoid;
		speed = info.speed;
		hands = info.hands;
		jump = info.jump;
		cash = info.cash;
		hands = info.hands;
	}
	
	public Map<String, Integer> serialize(){
		
		Map<String, Integer> data = new HashMapNoPutZero<String>();
		
		data.put("str", str);
		data.put("dex", dex);
		data.put("int", int_);
		data.put("luk", luk);
		data.put("hp", hp);
		data.put("mp", mp);
		data.put("watk", weaponAttack);
		data.put("matk", magicAttack);
		data.put("wdef", weaponDefense);
		data.put("mdef", magicDefense);
		data.put("acc", accuracy);
		data.put("avoid", avoid);
		data.put("speed", speed);
		data.put("hands", hands);
		data.put("jump", jump);
		
		return data;
		
	}
	
	public void deserialize(String data){
		deserialize(StringUtil.toMap(data, str -> Integer.parseInt(str)));
	}
	
	public void deserialize(Map<String, Integer> data){
		str = data.getOrDefault("str", 0);
		dex = data.getOrDefault("dex", 0);
		int_ = data.getOrDefault("int", 0);
		luk = data.getOrDefault("luk", 0);
		hp = data.getOrDefault("hp", 0);
		mp = data.getOrDefault("mp", 0);
		weaponAttack = data.getOrDefault("watk", 0);
		magicAttack = data.getOrDefault("matk", 0);
		weaponDefense = data.getOrDefault("wdef", 0);
		magicDefense = data.getOrDefault("mdef", 0);
		accuracy = data.getOrDefault("acc", 0);
		avoid = data.getOrDefault("avoid", 0);
		hands = data.getOrDefault("hands", 0);
		speed = data.getOrDefault("speed", 0);
		jump = data.getOrDefault("jump", 0);
		hands = data.getOrDefault("hands", 0);
	}
	
/*	public ItemStatInfo(QueryResult result) {
		str = result.get("str");
		dex = result.get("dex");
		int_ = result.get("int");
		luk = result.get("luk");
		hp = result.get("hp");
		mp = result.get("mp");
		weaponAttack = result.get("watk");
		magicAttack = result.get("matk");
		weaponDefense = result.get("wdef");
		magicDefense = result.get("mdef");
		accuracy = result.get("acc");
		avoid = result.get("avoid");
		hands = result.get("hands");
		speed = result.get("speed");
		jump = result.get("jump");
		hands = result.get("hands");
		int itemId = result.get("itemid");
		cash = ItemInfoProvider.getStatInfo(itemId).isCash();
	}*/

	private static interface GetStat {
		public int getStat(ItemStatInfo info);
	}
	
	public Map<ItemStat, Integer> getStats(){
		
		Map<ItemStat, Integer> stats = new HashMap<>();
		
		for(ItemStat stat : ItemStat.values()){
			int v = stat.getStat(this);
			if(v != 0){
				stats.put(stat, v);
			}
		}
		
		return stats;
		
	}
	
	@AllArgsConstructor
	public static enum ItemStat {
		
		/*UPGRADE_SLOTS("tuc", (e, v) -> e.setUpgradeSlots(v), e -> e.getUpgradeSlots()),*/
		STR("incSTR", (e, v) -> e.setStr(v), e -> e.getStr()),
		DEX("incDEX", (e, v) -> e.setDex(v), e -> e.getDex()),
		INT("incINT", (e, v) -> e.setInt_(v), e -> e.getInt_()),
		LUK("incLUK", (e, v) -> e.setLuk(v), e -> e.getLuk()),
		WATK("incPAD", (e, v) -> e.setWeaponAttack(v), e -> e.getWeaponAttack()),
		MATK("incMAD", (e, v) -> e.setMagicAttack(v), e -> e.getMagicAttack()),
		WDEF("incPDD", (e, v) -> e.setWeaponDefense(v), e -> e.getWeaponDefense()),
		MDEF("incMDD", (e, v) -> e.setMagicDefense(v), e -> e.getMagicDefense()),
		ACC("incACC", (e, v) -> e.setAccuracy(v), e -> e.getAccuracy()),
		EVA("incEVA", (e, v) -> e.setAvoid(v), e -> e.getAvoid()),
		SPEED("incSpeed", (e, v) -> e.setSpeed(v), e -> e.getSpeed()),
		JUMP("incJump", (e, v) -> e.setJump(v), e -> e.getJump()),
		HP("incMHP", (e, v) -> e.setHp(v), e -> e.getHp()),
		MP("incMMP", (e, v) -> e.setMp(v), e -> e.getMp()),
		CASH("cash", (e, v) -> e.setCash(v == 1), e -> e.isCash() ? 1 : 0),
		FS("fs", (e, v) -> e.setFs(v == 1), e -> e.isFs() ? 1 : 0)
		;

		private static final Map<String, ItemStat> byWzName = new HashMap<>();
		
		private final String wzName;
		private final BiConsumer<ItemStatInfo, Integer> setStat;
		private final GetStat getStat;
		
		static{
			for(ItemStat value : values()){
				byWzName.put(value.wzName, value);
			}
		}
		
		public void setStat(ItemStatInfo info, int value){
			setStat.accept(info, value);
		}
		
		public int getStat(ItemStatInfo info){
			return getStat.getStat(info);
		}
		
		public static ItemStat getByWzName(String wzName){
			return byWzName.get(wzName);
		}
		
	}
	
	private static class HashMapNoPutZero<K> extends HashMap<K, Integer>{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4129633701186408156L;

		@Override
		public Integer put(K key, Integer value) {
			if(value == 0){
				return 0;
			}
			return super.put(key, value);
		}
		
	}
	
}
