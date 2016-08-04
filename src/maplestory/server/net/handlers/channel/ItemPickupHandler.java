package maplestory.server.net.handlers.channel;

import java.awt.Point;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.map.MapleMapItem;
import maplestory.map.MapleMapObject;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class ItemPickupHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.readInt();//Timestamp
		buf.readByte();
		
		Point position = readPosition(buf);
		
		int itemObjectId = buf.readInt();
		
		MapleCharacter chr = client.getCharacter();
		
		chr.getMap().getObjectLock().lock();
		
		try{
			MapleMapObject obj = chr.getMap().getObject(itemObjectId);
			
			if(!(obj instanceof MapleMapItem)){
				return;
			}
			
			MapleMapItem itemDrop = (MapleMapItem) obj;
			
			itemDrop.pickup(chr);
			
		}finally{
			chr.getMap().getObjectLock().unlock();
		}
		

		
	}

}
