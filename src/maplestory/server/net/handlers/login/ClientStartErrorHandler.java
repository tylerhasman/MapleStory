package maplestory.server.net.handlers.login;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;

public class ClientStartErrorHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		String error = readMapleAsciiString(buf);
		
		client.getLogger().warn("Started with error "+error);
	}

}
