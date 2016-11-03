package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.movement.MovementPath;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MovePetHandler extends MovementPacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		int petId = buf.readInt();
		buf.skipBytes(8);
		
		MovementPath moves = parseMovement(buf);
		
		MapleCharacter chr = client.getCharacter();
		int slot = chr.getPetSlot(petId);
		
		if(slot == -1){
			return;
		}
		
		chr.getPets()[slot].move(moves);
		chr.getMap().broadcastPacket(PacketFactory.movePet(chr, chr.getPets()[slot], slot, moves), chr.getId());
	}

}
