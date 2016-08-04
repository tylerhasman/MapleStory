package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class HealOverTimeHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		MapleCharacter chr = client.getCharacter();
		
		/*int timestamp = */buf.readInt();
		buf.skipBytes(4);
		short amountHp = buf.readShort();
		short amountMp = buf.readShort();
		
		if(amountHp != 0){
			chr.restoreHp(amountHp);	
		}
		
		if(amountMp != 0 && amountMp < 1000){
			chr.restoreMp(amountMp);
		}
		
	}

}
