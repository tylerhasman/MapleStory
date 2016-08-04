package maplestory.life;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.MapleElement;
import constants.ElementalEffectiveness;
import lombok.Data;
import maplestory.life.MapleLifeFactory.BanishInfo;
import maplestory.life.MapleLifeFactory.LoseItem;
import maplestory.life.MapleLifeFactory.SelfDestruction;
import maplestory.util.Pair;

@Data
public class MapleMonsterStats {

	private int hp, mp, exp, level;
	private int PADamage, PDDamage, MADamage, MDDamage;
	private int attack;
	private int removeAfter;
	private boolean boss, explosiveReward, ffaLoot;
	private boolean undead;
	private String name;
	private int buffToGive;
	private int CP;
	private boolean removeOnMiss;
	private Pair<Integer, Integer> coolDamage;
	private List<LoseItem> loseItems = new ArrayList<>();
	private SelfDestruction selfDestruction;
	private boolean firstAttack;
	private int dropPeriod;
	private int tagColor;
	private int tagBgColor;
	private Map<String, Integer> animationTimes = new HashMap<>();
	private List<Integer> revives;
	private List<Pair<Integer, Integer>> skills = new ArrayList<>();
	private BanishInfo banishInfo;
	private Map<MapleElement, ElementalEffectiveness> effectivenesses = new HashMap<>();
	
	public void addLoseItem(LoseItem i){
		loseItems.add(i);
	}
	
	public void setEffectiveness(MapleElement ele, ElementalEffectiveness eleEffect){
		effectivenesses.put(ele, eleEffect);
	}
	
	public void setAnimationTime(String name, int delay){
		animationTimes.put(name, delay);
	}

	public boolean isEffective(MapleElement element) {
		
		ElementalEffectiveness effectiveness = effectivenesses.getOrDefault(element, ElementalEffectiveness.NORMAL);
		
		if(effectiveness.doesBlockEffect()){
			return false;
		}
		
		return true;
	}
	
}
