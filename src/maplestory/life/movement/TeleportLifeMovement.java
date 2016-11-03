package maplestory.life.movement;

import java.awt.Point;

import tools.data.output.MaplePacketWriter;
import io.netty.buffer.ByteBuf;
import lombok.ToString;
import maplestory.map.AbstractAnimatedMapleMapObject;

@ToString
public class TeleportLifeMovement implements LifeMovement {

	private int x, y;
	private int vx, vy;
	private byte state;
	
	private byte type;
	
	public TeleportLifeMovement(byte type, ByteBuf buf) {
		this.type = type;
		
		x = buf.readShort();
		y = buf.readShort();
		vx = buf.readShort();
		vy = buf.readShort();
		
		state = buf.readByte();
		
	}
	
	@Override
	public void encode(MaplePacketWriter buf) {
		buf.write(type);
		buf.writeShort(x);
		buf.writeShort(y);
		buf.writeShort(vx);
		buf.writeShort(vy);
		buf.write(state);
	}

	@Override
	public void translateLife(AbstractAnimatedMapleMapObject life) {
		life.setPosition(new Point(x, y));
		life.setStance(state);
	}

	@Override
	public MoveType getType() {
		return MoveType.TELEPORT;
	}

}
