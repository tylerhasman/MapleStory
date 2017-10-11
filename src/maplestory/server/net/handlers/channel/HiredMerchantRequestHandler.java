package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class HiredMerchantRequestHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		MapleCharacter chr = client.getCharacter();
		
		if(validMap(chr.getMapId())){
			
			if(hasItemsAtFredrick(chr)){
				client.sendPacket(PacketFactory.hiredMerchantResult(0x09));//Retrived items from fredrick first
			}else{
				client.sendPacket(PacketFactory.hiredMerchantResult(0x07));
			}
			
		}else{
			chr.sendMessage(MessageType.PINK_TEXT, "You cannot open your hired merchant here.");
		}
		
	}

	private boolean hasItemsAtFredrick(MapleCharacter chr){
		return false;
	}
	
	private boolean validMap(int id){
		return id > 910000000 && id < 910000023;
	}
	
}
