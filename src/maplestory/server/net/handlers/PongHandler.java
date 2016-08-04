package maplestory.server.net.handlers;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;

public class PongHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
	
		client.receivePong();
		
		//client.getLogger().info("Ping is "+client.getPing());
		
	}

}
