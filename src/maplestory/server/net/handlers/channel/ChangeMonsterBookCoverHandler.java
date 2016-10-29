package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class ChangeMonsterBookCoverHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		int id = buf.readInt();
		
		MapleCharacter chr = client.getCharacter();
		
		if(chr.getMonsterBook().changeCover(id % 10000)){
			client.sendPacket(PacketFactory.monsterBookCover(id));
		}else{
			chr.sendMessage(MessageType.POPUP, "Failed to change monster book cover to "+id+"\r\nPlease report this incident.");
		}
		
		
	}
	
}
