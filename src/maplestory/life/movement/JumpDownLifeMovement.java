package maplestory.life.movement;

import java.awt.Point;

import tools.data.output.MaplePacketWriter;
import io.netty.buffer.ByteBuf;
import lombok.ToString;
import maplestory.map.AbstractAnimatedMapleMapObject;
import maplestory.map.AbstractLoadedMapleLife;

@ToString
public class JumpDownLifeMovement implements LifeMovement {

	private short x, y;
	private short vx, vy;
	private short fh, fhLast;
	private byte state;
	private short duration;
	
	public JumpDownLifeMovement(ByteBuf buf) {
		x = buf.readShort();
		y = buf.readShort();
		vx = buf.readShort();
		vy = buf.readShort();
		fh = buf.readShort();
		fhLast = buf.readShort();
		state = buf.readByte();
		duration = buf.readShort();
	}
	
	@Override
	public void encode(MaplePacketWriter buf) {
		buf.write(16);
		buf.writeShort(x);
		buf.writeShort(y);
		buf.writeShort(vx);
		buf.writeShort(vy);
		buf.writeShort(fh);
		buf.writeShort(fhLast);
		buf.write(state);
		buf.writeShort(duration);
	}

	@Override
	public void translateLife(AbstractAnimatedMapleMapObject life) {
		life.setPosition(new Point(x, y));
		
		if(life instanceof AbstractLoadedMapleLife){
			((AbstractLoadedMapleLife) life).setFh(fh);
		}
	}
	
	@Override
	public MoveType getType() {
		return MoveType.JUMP_DOWN;
	}

}
