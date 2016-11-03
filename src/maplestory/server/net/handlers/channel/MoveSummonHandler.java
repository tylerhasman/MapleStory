package maplestory.server.net.handlers.channel;

import java.awt.Point;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleSummon;
import maplestory.life.movement.MovementPath;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MoveSummonHandler extends MovementPacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int oid = buf.readInt();
		
		Point startPos = readPosition(buf);
		MovementPath res = parseMovement(buf);
		
		MapleCharacter chr = client.getCharacter();
		
		MapleSummon chosen = chr.getSummonByObjectId(oid);
		
		if(chosen != null){
			res.translateLife(chosen);
			chr.getMap().broadcastPacket(PacketFactory.moveSummon(chr.getId(), oid, startPos, res), chr.getId());
		}
	}

}
