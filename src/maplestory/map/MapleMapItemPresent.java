package maplestory.map;

import java.awt.Point;

import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.player.MapleCharacter;

public class MapleMapItemPresent extends MapleMapItem {

	private Item[] drops;
	
	public MapleMapItemPresent(Item[] drops, int owner, Point location, DropType dropType, MapleMap map, MapleMapObject source) {
		super(ItemFactory.getItem(4031306, 1), owner, location, dropType, map, source);
		this.drops = drops;
	}
	
	public MapleMapItemPresent(int mesos, int owner, Point location, DropType dropType, MapleMap map, MapleMapObject source) {
		super(null, owner, location, dropType, map, source);
		setMesos(mesos);
	}

	@Override
	public void pickup(MapleCharacter chr) {
		
		if(chr.getId() != getOwner()) {
			return;
		}
		
		if(getMesos() > 0){
			int each = getMesos() / 4;
			
			if(each <= 0){
				each = 1;
			}
			
			int xPos = -60;
			
			for(int i = 0; i < 4;i++){
				Point p = (Point) getPosition().clone();
				p.x += xPos;
				getMap().dropMesos(each, p, this);
				xPos += 30;
			}
		}else{
			int xPos = -(drops.length * 30) / 2;
			
			for(int i = 0; i < drops.length;i++){
				Point p = (Point) getPosition().clone();
				p.x += xPos;
				getMap().dropItem(drops[i], p, this);
				xPos += 30;
			}
		}
		
		destroy();
		chr.getClient().sendReallowActions();
		
	}

}
