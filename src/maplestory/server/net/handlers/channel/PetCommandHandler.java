package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.player.MaplePetInstance;
import maplestory.server.net.MaplePacketHandler;

public class PetCommandHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		MapleCharacter chr = client.getCharacter();
		
		int petId = buf.readInt();
		int index = chr.getPetSlot(petId);
		
		if(index == -1){
			return;
		}
		
		MaplePetInstance pet = chr.getPets()[index];
		
		buf.skipBytes(5);
		
		byte command = buf.readByte();
		
	}

}
