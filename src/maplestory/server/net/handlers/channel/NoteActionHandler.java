package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleNote;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class NoteActionHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		int action = buf.readByte();
		
		if(action == 0){
			String name = readMapleAsciiString(buf);
			String message = readMapleAsciiString(buf);
		}else if(action == 1){
			int num = buf.readByte();
			buf.skipBytes(2);
			
			int fame = 0;

			MapleCharacter chr = client.getCharacter();
			
			for(int i = 0; i < num;i++){
				int id = buf.readInt();
				
				MapleNote note = chr.getNote(id);
				
				if(note != null){
					fame += note.getFame();
				}
				
			}
			
			client.getCharacter().clearNotes();
			
			if(fame > 0){
				client.getCharacter().gainFame(fame);
				client.sendPacket(PacketFactory.showFameGain(fame));
			}
			
		}
	}

}
