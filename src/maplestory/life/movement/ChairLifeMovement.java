package maplestory.life.movement;

import java.awt.Point;

import tools.data.output.MaplePacketWriter;
import io.netty.buffer.ByteBuf;
import lombok.ToString;
import maplestory.map.AbstractAnimatedMapleMapObject;
import maplestory.map.AbstractLoadedMapleLife;

@ToString
public class ChairLifeMovement implements LifeMovement {

	private int x, y;
	private short fh;
	private byte state;
	private short duration;
	
	public ChairLifeMovement(ByteBuf buf) {
		x = buf.readShort();
		y = buf.readShort();
		fh = buf.readShort();
		state = buf.readByte();
		duration = buf.readShort();
	}
	
	@Override
	public void encode(MaplePacketWriter buf) {
		buf.write(11);
		buf.writeShort(x);
		buf.writeShort(y);
		buf.writeShort(fh);
		buf.write(state);
		buf.writeShort(duration);
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
	public MoveType getType() {
		return MoveType.CHAIR;
	}

}
