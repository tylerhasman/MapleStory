package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;

public class ChangeChannelHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int channel = buf.readByte();

		client.getCharacter().getMap().removePlayer(client.getCharacter());
		
		client.changeChannel(channel);
		
	}

}
