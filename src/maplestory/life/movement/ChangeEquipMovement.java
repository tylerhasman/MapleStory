package maplestory.life.movement;

import tools.data.output.MaplePacketWriter;
import io.netty.buffer.ByteBuf;
import lombok.ToString;
import maplestory.map.AbstractAnimatedMapleMapObject;

@ToString
public class ChangeEquipMovement implements LifeMovement {

	private byte equipSlot;
	
	public ChangeEquipMovement(ByteBuf buf) {
		equipSlot = buf.readByte();
	}
	
	@Override
	public void encode(MaplePacketWriter buf) {
		buf.write(10);
		buf.write(equipSlot);
	}

	@Override
	public void translateLife(AbstractAnimatedMapleMapObject life) {
		
	}
	
	@Override
	public MoveType getType() {
		return MoveType.CHANGE_EQUIP;
	}

}
