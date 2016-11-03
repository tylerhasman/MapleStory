package maplestory.life.movement;

import java.awt.Point;

import tools.data.output.MaplePacketWriter;
import lombok.ToString;
import maplestory.map.AbstractAnimatedMapleMapObject;
import maplestory.map.AbstractLoadedMapleLife;
import maplestory.map.AbstractMapleMapObject;
import io.netty.buffer.ByteBuf;

@ToString
public class AbsoluteLifeMovement implements LifeMovement {

	private byte type;
	private int x, y;
	private int vx, vy;
	private int fh;
	private byte state;
	private short duration;
	
	public AbsoluteLifeMovement(byte type, ByteBuf buf) {
		this.type = type;
		
		x = buf.readShort();
		y = buf.readShort();
		vx = buf.readShort();
		vy = buf.readShort();
		fh = buf.readShort();
		state = buf.readByte();
		duration = buf.readShort();
	}
	
	public AbsoluteLifeMovement(Point position) {
		x = (int) position.getX();
		y = (int) position.getY();
		vx = 0;
		vy = 0;
		type = 0;
		duration = 10;
	}

	@Override
	public void translateLife(AbstractAnimatedMapleMapObject life) {
		life.setPosition(new Point(x, y));
		life.setStance(state);
		
		if(life instanceof AbstractLoadedMapleLife){
			((AbstractLoadedMapleLife) life).setFh(fh);
		}
	}
	
	@Override
	public void encode(MaplePacketWriter buf) {
		buf.write(type);
		buf.writeShort(x);
		buf.writeShort(y);
		buf.writeShort(vx);
		buf.writeShort(vy);
		buf.writeShort(fh);
		buf.write(state);
		buf.writeShort(duration);
	}
	
	@Override
	public MoveType getType() {
		return MoveType.ABSOLUTE;
	}

}
