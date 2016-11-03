package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class UseInnerPortalHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		MapleCharacter chr = client.getCharacter();
		
		buf.skipBytes(1);//Unknown 1 byte (always 01 ? )
		
		buf.skipBytes(6); //Unknown 6 bytes
		
		buf.readShort(); // Start X
		buf.readShort(); // Start Y
		
		short fx = buf.readShort();
		short fy = buf.readShort();
		
		chr.getPosition().setLocation(fx, fy);
		
	}

}
