package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class NpcAnimationHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		if(buf.readableBytes() == 6){//Talking
			int id = buf.readInt();
			short something = buf.readShort();
			client.sendPacket(PacketFactory.getNpcTalkPacket(id, something));
		}else if(buf.readableBytes() > 6){//Moving
			
			byte[] bytes = new byte[buf.readableBytes() - 9];
			
			buf.readBytes(bytes);
			
			client.sendPacket(PacketFactory.getNpcActionPacket(bytes));
			
		}
		
	}

}
