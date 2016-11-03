package maplestory.life.movement;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public enum MoveType {

	ABSOLUTE(14, 0, 5, 17),
	RELATIVE(8, 1, 2, 6, 12, 13, 16),
	TELEPORT(10, 3, 4, 7, 8, 9, 14),
	CHANGE_EQUIP(2, 10),
	CHAIR(10, 11),
	JUMP_DOWN(16, 15),
	ARAN(9, 21),
	UNKNOWN(0, -1)
	;
	
	private static final Map<Integer, MoveType> mapping = new HashMap<>();
	
	private final int[] ids;
	
	@Getter
	private final int encodedSize;
	
	private MoveType(int encodedSize, int... ids) {
		this.encodedSize = encodedSize;
		this.ids = ids;
	}

	private static void initializeMap(){
		for(MoveType mt : values()){
			for(int id : mt.ids){
				mapping.put(id, mt);
			}
		}
	}
	
	public static MoveType getMoveType(int id){
		if(mapping.size() == 0){
			initializeMap();
		}
		return mapping.getOrDefault(id, UNKNOWN);
	}
	
}
