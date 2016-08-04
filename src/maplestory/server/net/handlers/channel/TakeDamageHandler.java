package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;

public class TakeDamageHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		buf.readInt();
		//byte damageFrom = buf.readByte();
		buf.skipBytes(1);//Do this because we commented out the above line
		buf.readByte();
		int damage = buf.readInt();
		
		client.getCharacter().damage(damage);
		
		/*int objectId = 0, monsterId = 0, pgmr = 0, direction = 0;
		int pos_x = 0, pos_y = 0, fake = 0;
		
		if(damageFrom != -3){
			
			monsterId = buf.readInt();
			oid = buf.readInt();
			
		}else{
			//Handle other types I guess?
		}*/
		
	}

}
