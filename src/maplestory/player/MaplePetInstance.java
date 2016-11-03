package maplestory.player;

import java.awt.Point;
import lombok.Getter;
import lombok.Setter;
import maplestory.client.MapleClient;
import maplestory.inventory.item.PetItem;
import maplestory.life.movement.MovementPath;
import maplestory.map.AbstractLoadedMapleLife;
import maplestory.map.MapleMapObjectType;
import maplestory.server.MapleServer;

public class MaplePetInstance {

	@Getter
	private PetItem source;
	
	private int owner, world;
	
	@Getter @Setter
	private Point position;
	
	@Getter @Setter
	private int foothold;
	
	@Getter @Setter
	private int stance;
	
	public MaplePetInstance(MapleCharacter owner, PetItem source) {
		this.owner = owner.getId();
		this.world = owner.getWorldId();
		this.source = source;
	}
	
	public MapleCharacter getOwner(){
		return MapleServer.getWorld(world).getPlayerStorage().getById(owner);
	}
	
	public void move(MovementPath path){
		FakePetLife fpl = new FakePetLife(position, stance);
		
		path.translateLife(fpl);
		
		position = fpl.getPosition();
		stance = fpl.getStance();
		foothold = fpl.getFh();
	}
	
	private static class FakePetLife extends AbstractLoadedMapleLife {

		public FakePetLife(Point position, int stance) {
			super(0);
			setPosition(position);
			setStance(stance);
		}

		@Override
		public void sendSpawnData(MapleClient client) {
			
		}

		@Override
		public void sendDestroyData(MapleClient client) {
			
		}

		@Override
		public MapleMapObjectType getType() {
			return null;
		}
		
	}
	
}
