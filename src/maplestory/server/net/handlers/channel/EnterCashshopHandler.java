package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class EnterCashshopHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		MapleCharacter chr = client.getCharacter();
		
		chr.openCashshop();
		
	}

}
