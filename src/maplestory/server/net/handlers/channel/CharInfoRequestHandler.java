package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class CharInfoRequestHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.skipBytes(4);
		
		int cid = buf.readInt();
		
		MapleCharacter target = null;
		
		for(MapleCharacter chr : client.getCharacter().getMap().getPlayers()){
			if(chr.getId() == cid){
				target = chr;
				break;
			}
		}
		
		if(target != null){
			client.sendPacket(PacketFactory.openCharInfo(target));
		}
		
	}

}
