package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.map.MapleMagicDoor;
import maplestory.map.MapleMapObject;
import maplestory.server.net.MaplePacketHandler;

public class UseMagicDoorHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		int oid = buf.readInt();
		boolean toTown = buf.readByte() == 0;
		
		MapleMagicDoor door = null;
		
		for(MapleMapObject obj : client.getCharacter().getMap().getObjects()){
			if(obj instanceof MapleMagicDoor){
				MapleMagicDoor door2 = (MapleMagicDoor) obj;
				
				if(door2.getOwner().getId() == oid){
					door = door2;
					break;
				}
			}
		}
		
		if(door != null){
			door.warp(client.getCharacter(), toTown);
		}else{
			client.sendReallowActions();
		}
		
	}

}
