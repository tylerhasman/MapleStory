package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.client.MapleMessenger;
import maplestory.server.net.MaplePacketHandler;

public class MessengerHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		byte mode = buf.readByte();
		
		MapleMessenger messenger = null;
		
		if(mode == 0x00){
			
		}
	}

}
