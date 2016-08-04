package maplestory.server.net.handlers.login;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;

public class ServerListRequestHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		client.sendWorldList();
	}

}
