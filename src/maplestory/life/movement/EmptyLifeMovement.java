package maplestory.life.movement;

import tools.data.output.MaplePacketWriter;
import maplestory.map.AbstractAnimatedMapleMapObject;

public class EmptyLifeMovement implements LifeMovement {

	private static final EmptyLifeMovement INSTANCE = new EmptyLifeMovement();
	
	private EmptyLifeMovement() {
		
	}
	
	@Override
	public void encode(MaplePacketWriter buf) {

	}

	@Override
	public void translateLife(AbstractAnimatedMapleMapObject life) {

	}
	
	public static EmptyLifeMovement create() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "EMPTY_MOVE";
	}
	
	@Override
	public MoveType getType() {
		return MoveType.UNKNOWN;
	}
	
}
