package maplestory.life.movement;

import io.netty.buffer.ByteBuf;

public class LifeMovementFactory {

	private static LifeMovement decodeMovement(ByteBuf buf){
		LifeMovement lm = null;
		
		byte typeId = buf.readByte();
		
		MoveType type = MoveType.getMoveType(typeId);
		
		switch(type){
			case ABSOLUTE:
				lm = new AbsoluteLifeMovement(typeId, buf);
				break;
			case RELATIVE:
				lm = new RelativeLifeMovement(typeId, buf);
				break;
			case TELEPORT:
				//lm = new TeleportLifeMovement(typeId, buf);
				buf.skipBytes(9);
				lm = EmptyLifeMovement.create();
				break;
			case CHANGE_EQUIP:
				lm = new ChangeEquipMovement(buf);
				break;
			case CHAIR:
				lm = new ChairLifeMovement(buf);
				break;
			case JUMP_DOWN:
				lm = new JumpDownLifeMovement(buf);
				lm = EmptyLifeMovement.create();
				break;
			case ARAN:
				buf.skipBytes(3);
			default:
				lm = EmptyLifeMovement.create();
				System.out.println("Unknown movement "+type);
				break;
		}
		
		return lm;
	}
	
	public static MovementPath decodeMovements(ByteBuf buf){

		byte numMovements = buf.readByte();

		LifeMovement[] movements = new LifeMovement[numMovements];
		
		for(int i = 0; i < numMovements;i++){
			
			movements[i] = decodeMovement(buf);
			
		}
		
		return new MovementPath(movements);
	}
	
}
