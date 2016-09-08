package maplestory.inventory.item;

import java.awt.Point;
import java.util.List;
import maplestory.life.MapleLifeFactory;
import maplestory.life.MapleMonster;
import maplestory.map.MapleMap;
import maplestory.util.Randomizer;

public class MapleSummoningBag extends MapleItem implements SummoningBag {

	private List<SummoningEntry> summons;
	
	public MapleSummoningBag(int itemId, int amount, List<SummoningEntry> summons) {
		super(itemId, amount);
		this.summons = summons;
	}

	@Override
	public List<SummoningEntry> getSummons() {
		return summons;
	}

	@Override
	public void useBag(MapleMap map, Point location) {
		int fh = map.getFoothold(location);
		for(SummoningEntry entry : summons){
			
			if(Randomizer.nextInt(101) <= entry.getSpawnChance()){
				MapleMonster monster = MapleLifeFactory.getMonster(entry.getMonsterId());
				
				if(monster == null){
					continue;
				}
				
				monster.setFh(fh);
				monster.setPosition(location);
				
				map.spawnMonster(monster);
				
			}
			
		}
	}
	
	@Override
	public Item copyOf(int amount) {
		return new MapleSummoningBag(getItemId(), amount, summons);
	}


}
