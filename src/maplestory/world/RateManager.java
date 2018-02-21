package maplestory.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import maplestory.player.MapleCharacter;

public class RateManager {

	public static final RateManager STATIC_RATE_MANAGER = new RateManager(Rates.builder().build());

	private Rates globalRates;
	
	private Map<Integer, Rates> byCharacterRates;
	private Map<Integer, Map<String, RateModifier>> modifiers;
	
	public RateManager(Rates globalRates) {
		this.globalRates = globalRates;
		modifiers = new HashMap<>();
		byCharacterRates = new HashMap<>();
	}
	
	public void addModifier(MapleCharacter chr, String modId, RateModifier mod) {
		if(!modifiers.containsKey(chr.getId())) {
			modifiers.put(chr.getId(), new HashMap<>());
		}
		modifiers.get(chr.getId()).put(modId, mod);
	}
	
	public void removeModifier(MapleCharacter chr, String modId) {
		if(modifiers.containsKey(chr.getId())) {
			modifiers.get(chr.getId()).remove(modId);
		}
	}
	
	public boolean hasModifier(MapleCharacter chr, String modId) {
		return modifiers.get(chr.getId()).containsKey(modId);
	}
	
	public void setGlobalRate(RateType type, float val) {
		type.setter.accept(globalRates, val);
	}
	
	public float getGlobalRate(RateType type) {
		return type.getter.apply(globalRates);
	}
	
	public void setCharacterRate(MapleCharacter chr, RateType type, float val) {

		Rates rates = null;
		if(byCharacterRates.containsKey(chr.getId())) {
			rates = byCharacterRates.get(chr.getId());
		}else {
			byCharacterRates.put(chr.getId(), rates = globalRates.clone());
		}
		
		type.setter.accept(rates, val);
	}
	
	public float getCharacterRate(MapleCharacter chr, RateType type) {
		return getCharacterRate(chr, type, true);
	}
	
	public float modify(MapleCharacter chr, RateType type, float val) {
		if(modifiers.containsKey(chr.getId())) {
			for(RateModifier mod : modifiers.get(chr.getId()).values()) {
				val = mod.modify(type, val);
			}
		}
		return val;
	}
	
	public float getCharacterRate(MapleCharacter chr, RateType type, boolean applyModifiers) {
		float rate;
		
		if(byCharacterRates.containsKey(chr.getId())) {
			rate = type.getter.apply(byCharacterRates.get(chr.getId()));
		}else {
			rate = getGlobalRate(type);
		}
	
		if(applyModifiers) {
			if(modifiers.containsKey(chr.getId())) {
				for(RateModifier mod : modifiers.get(chr.getId()).values()) {
					rate = mod.modify(type, rate);
				}
			}	
		}
		
		return rate;
	}
	
	@Setter
	@Builder
	@Getter
	public static class Rates implements Cloneable {
		private float exp, meso, drop, quest;
		
		@Override
		public Rates clone() {
			return new Rates(exp, meso, drop, quest);
		}
		
	}
	
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	public static enum RateType {
		EXP(rates -> rates.exp, (rates, val) -> rates.exp = val),
		MESO(rates -> rates.meso, (rates, val) -> rates.meso = val),
		DROP(rates -> rates.drop, (rates, val) -> rates.drop = val),
		QUEST(rates -> rates.quest, (rates, val) -> rates.quest = val)
		;
		
		private final Function<Rates, Float> getter;
		private final BiConsumer<Rates, Float> setter;
	}
	
	public static interface RateModifier {
		
		public float modify(RateType type, float rate);
		
	}
	
}
