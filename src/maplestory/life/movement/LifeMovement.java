package maplestory.life.movement;

import tools.data.output.MaplePacketWriter;
import maplestory.map.AbstractAnimatedMapleMapObject;

public interface LifeMovement {
	
	public void encode(MaplePacketWriter buf);
	
	public void translateLife(AbstractAnimatedMapleMapObject life);
	
	public MoveType getType();
	
}
