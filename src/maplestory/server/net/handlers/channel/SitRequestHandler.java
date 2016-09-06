package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.map.MapleMap;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class SitRequestHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		short chair = buf.readShort();
		MapleMap map = client.getCharacter().getMap();
		if(chair >= 0){
			client.getCharacter().setActiveChair(chair);
			client.sendPacket(PacketFactory.chairSitResponse(chair));
			client.sendReallowActions();
		}else if(client.getCharacter().getActiveChair() > 0){
			client.getCharacter().setActiveChair(0);
			map.broadcastPacket(PacketFactory.portalChairEffect(client.getCharacter()), client.getCharacter().getId());
			client.sendPacket(PacketFactory.chairSitResponse((short) -1));
		}
	}

}
