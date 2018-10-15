package maplestory.server.net.handlers.login;

import io.netty.buffer.ByteBuf;
import maplestory.channel.MapleChannel;
import maplestory.client.MapleClient;
import maplestory.server.MapleStory;
import maplestory.server.net.MaplePacketHandler;

public class CharSelectHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		if(MapleStory.getServerConfig().isPicEnabled()) {
			client.closeConnection();
			return;
		}
		
		int id = buf.readInt();
	
		//String macs = readMapleAsciiString(buf);
		
		MapleChannel channel = client.getChannel();
		
		channel.initialConnection(client, id);
		
	}

}
