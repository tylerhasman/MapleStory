package maplestory.life.movement;

import tools.data.output.MaplePacketWriter;
import maplestory.map.AbstractAnimatedMapleMapObject;

public class MovementPath {

	private LifeMovement[] movements;
	
	protected MovementPath(LifeMovement[] movements) {
		this.movements = movements;
	}
	
	public void translateLife(AbstractAnimatedMapleMapObject life){
		for(LifeMovement lm : movements){
			lm.translateLife(life);
		}
	}
	
	public byte[] serialize(){
		int encodedSize = calculateEncodedSize();
		MaplePacketWriter buf = new MaplePacketWriter(encodedSize);
		
		buf.write(movements.length);
		
		for(LifeMovement lm : movements){
			lm.encode(buf);
		}
		
		return buf.getPacket();
	}
	
	private int calculateEncodedSize(){
		int size = 0;
		for(LifeMovement lm : movements){
			size += lm.getType().getEncodedSize();
		}
		
		return size + 1;
	}

	public static MovementPath singleton(AbsoluteLifeMovement absoluteLifeMovement) {
		return new MovementPath(new LifeMovement[] {absoluteLifeMovement});
	}
	
}
