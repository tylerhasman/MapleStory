package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.party.PartyOperationType;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class DenyPartyInviteHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.skipBytes(1);
		MapleCharacter from = client.getWorld().getPlayerStorage().getByName(readMapleAsciiString(buf));
		
		if(from != null){
			from.getClient().sendPacket(PacketFactory.partyDenyInvite(client.getCharacter()));
		}
	}

}
