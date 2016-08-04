package maplestory.server.net.handlers;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.MapleServer;
import maplestory.server.MapleStory;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.RecvOpcode;
import maplestory.util.Hex;

public class DefaultMaplePacketHandler extends MaplePacketHandler {

	private final int type;
	
	public DefaultMaplePacketHandler(int type) {
		this.type = type;
	}
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		RecvOpcode op = RecvOpcode.getById(type);
		
		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		
		if(op == null){
			return;
		}
		
		if(client.getCharacter() == null){
			client.getLogger().warn("Unhandled Packet: "+op.name()+" "+Hex.toHex(data));
		}else{
			if(!client.getCharacter().isCashShopOpen()){
				client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "Unhandled Packet: "+op.name()+" "+Hex.toHex(data));
			}else{
				MapleStory.getLogger().info(client.getCharacter().getName()+" >> Unhandled Packet (Inside Cashshop): "+op.name()+" "+Hex.toHex(data));
			}
			
		}
		
		
	}

}
