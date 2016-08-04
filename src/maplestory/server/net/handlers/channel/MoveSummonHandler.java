package maplestory.server.net.handlers.channel;

import java.awt.Point;
import java.util.List;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleSummon;
import maplestory.life.movement.LifeMovementFragment;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MoveSummonHandler extends MovementPacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int oid = buf.readInt();
		
		Point startPos = readPosition(buf);
		List<LifeMovementFragment> res = parseMovement(buf);
		
		MapleCharacter chr = client.getCharacter();
		
		MapleSummon chosen = chr.getSummonByObjectId(oid);
		
		if(chosen != null){
			updatePosition(res, chosen, 0);
			chr.getMap().broadcastPacket(PacketFactory.moveSummon(chr.getId(), oid, startPos, res), chr.getId());
		}
	}

}
