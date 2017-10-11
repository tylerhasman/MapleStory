package maplestory.server.net.handlers.login;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.util.Hex;

public class CharListRequestHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.readByte();
        int world = buf.readByte();
        
        client.setWorldId(world);
        client.setChannelId(buf.readByte());
        
        client.sendPacket(PacketFactory.getCharList(client, world));
	}

}
