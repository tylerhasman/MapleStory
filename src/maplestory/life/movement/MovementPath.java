package maplestory.life.movement;

import tools.data.output.MaplePacketWriter;

import java.util.LinkedList;
import java.util.List;

import maplestory.map.AbstractAnimatedMapleMapObject;

public class MovementPath {

	private LifeMovement[] movements;
	
	protected MovementPath(LifeMovement[] movements) {
		this.movements = movements;
	}
	
	@Override
	public String toString() {
		String s = "";
		
		for(LifeMovement move : movements){
			s += move.toString()+";";
		}
		
		return s;
	}
	
	public void translateLife(AbstractAnimatedMapleMapObject life){
		for(LifeMovement lm : movements){
			lm.translateLife(life);
		}
	}
	
	public byte[] serialize(){
		int encodedSize = calculateEncodedSize();
		MaplePacketWriter buf = new MaplePacketWriter(encodedSize);
		
		List<LifeMovement> valid = getValidMoves();
		
		buf.write(valid.size());
		
		for(LifeMovement lm : valid){
			lm.encode(buf);
		}
		
		return buf.getPacket();
	}
	
	private List<LifeMovement> getValidMoves(){
		List<LifeMovement> moves = new LinkedList<>();
		
		for(LifeMovement move : movements){
			if(move.getType() != MoveType.UNKNOWN){
				moves.add(move);
			}
		}
		
		return moves;
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
