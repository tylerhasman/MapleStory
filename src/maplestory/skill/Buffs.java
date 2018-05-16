package maplestory.skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constants.MapleBuffStat;
import constants.MapleStat;
import tools.TimerManager;
import tools.TimerManager.MapleTask;

public class Buffs {

	private Map<Integer, MapleStatEffect> effects;
	
	private Map<MapleBuffStat, Integer> values;
	
	private Map<Integer, Long> startTimes;
	
	public Buffs() {
		effects = new HashMap<>();
		values = new HashMap<>();
		startTimes = new HashMap<>();
	}
	
	public void setBuffedValue(MapleBuffStat stat, int val) {
		values.put(stat, val);
	}
	
	public int getBuffedValue(MapleBuffStat stat) {
		return values.getOrDefault(stat, 0);
	}
	
	public Collection<MapleStatEffect> getEffects(){
		return new ArrayList<>(effects.values());
	}
	
	public void addBuff(MapleStatEffect effect) {
		effects.put(effect.getSourceId(), effect);
	}
	
	public void removeBuff(int skillId) {
		effects.remove(skillId);
		
		//startTimes.put(effect.getSourceId(), System.currentTimeMillis());
	}
	
	public long getStartTime(int skillId) {
		return startTimes.getOrDefault(skillId, 0L);
	}
	
	
	public boolean hasBuff(MapleStatEffect effect) {
		return effects.containsKey(effect.getSourceId());
	}
	
	public Collection<MapleBuffStat> getBuffStats(){
		Set<MapleBuffStat> stats = new HashSet<>();
		
		for(MapleStatEffect effect : effects.values()) {
			stats.addAll(effect.getStatups());
		}
		
		return stats;
	}

	public void removeIfHasBuffStat(MapleBuffStat stat) {
		for(MapleStatEffect effect : getEffects()) {
			if(effect.getStatups().contains(stat)) {
				removeBuff(effect.getSourceId());
			}
		}
	}

	public boolean hasBuffStat(MapleBuffStat stat) {
		for(MapleStatEffect effect : getEffects()) {
			if(effect.getStatups().contains(stat)) {
				return true;
			}
		}
		return false;
	}

	public MapleStatEffect getSourceOf(MapleBuffStat stat) {
		for(MapleStatEffect effect : getEffects()) {
			if(effect.getStatups().contains(stat)) {
				return effect;
			}
		}
		return null;
	}
	
}
