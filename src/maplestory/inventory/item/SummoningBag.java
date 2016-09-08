package maplestory.inventory.item;

import java.awt.Point;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import maplestory.map.MapleMap;

public interface SummoningBag extends Item {

	@AllArgsConstructor
	@Data
	public static class SummoningEntry {
		
		private final int monsterId;
		private final int spawnChance;
		
	}
	
	public List<SummoningEntry> getSummons();

	public void useBag(MapleMap map, Point location);

}
