package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class WhisperHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		byte mode = buf.readByte();
		
		if(mode == 6){
			String recipient = readMapleAsciiString(buf);
			
			String text = readMapleAsciiString(buf);
			
			MapleCharacter chr = client.getWorld().getPlayerStorage().getByName(recipient);
			
			if(chr != null){
				chr.getClient().sendPacket(PacketFactory.whisper(client.getCharacter().getName(), client.getChannelId(), text));
			
				client.sendPacket(PacketFactory.whisperReply(chr.getName(), 1));
			}else{
				client.sendPacket(PacketFactory.whisperReply(recipient, 0));
			}
		}
	}

}
