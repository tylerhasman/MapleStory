package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.world.World;

public class GroupChatHandler extends MaplePacketHandler {

	public static enum GroupChatType {
		
		BUDDY(0),
		PARTY(1),
		GUILD(2)
		;
		
		private GroupChatType(int code) {
			this.code = (byte) code;
		}
		
		@Getter
		private final byte code;
		
		public static GroupChatType getById(int id){
			for(GroupChatType type : values()){
				if(type.code == id){
					return type;
				}
			}
			return null;
		}
		
	}
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		byte typeId = buf.readByte();
		
		GroupChatType type = GroupChatType.getById(typeId);
		
		if(type == null){
			client.getWorld().getLogger().warn("Unknown GroupChatType "+typeId+" from "+client.getCharacter().getName());
			return;
		}
		
		byte numRecipients = buf.readByte();
		int[] recipents = new int[numRecipients];
		for(int i = 0; i < numRecipients;i++){
			recipents[i] = buf.readInt();
		}
		
		String message = readMapleAsciiString(buf);
		
		MapleCharacter chr = client.getCharacter();
		
		chr.chat(message, type);
		
	}

}
