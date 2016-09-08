package maplestory.player;

import java.awt.Point;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import maplestory.inventory.item.PetItem;
import maplestory.life.movement.AbsoluteLifeMovement;
import maplestory.life.movement.LifeMovement;
import maplestory.life.movement.LifeMovementFragment;
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
	
	public void move(List<LifeMovementFragment> frags){
		for(LifeMovementFragment frag : frags){
			if(frag instanceof LifeMovement){
				if(frag instanceof AbsoluteLifeMovement){
					position = frag.getPosition();
				}
				
				stance = ((LifeMovement)frag).getNewstate();
			}
		}
	}
	
}
