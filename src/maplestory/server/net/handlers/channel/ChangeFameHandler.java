package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class ChangeFameHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		int oid = buf.readInt();
		byte mode = buf.readByte();
		
		int amount = mode == 0 ? -1 : 1;
		
		MapleCharacter chr = client.getCharacter();
		MapleCharacter target = chr.getMap().getPlayerById(oid);
		
		if(target == null || target.getId() == chr.getId()){
			client.sendPacket(PacketFactory.fameErrorResponse(1));
			return;
		}else if(chr.getLevel() < 15){
			client.sendPacket(PacketFactory.fameErrorResponse(2));
			return;
		}
		
		int fameResult = chr.giveFameTo(target, amount);
		
		if(fameResult == 0){
			client.sendPacket(PacketFactory.giveFameResponse(mode, target.getName(), target.getFame()));
			target.getClient().sendPacket(PacketFactory.receiveFame(mode, chr.getName()));
		}else if(fameResult == -3){
			if(target.getFame() + amount > 30000){
				chr.sendMessage(MessageType.POPUP, target.getName()+"'s fame can't get any higher!");
			}else{
				chr.sendMessage(MessageType.POPUP, target.getName()+"'s fame can't get any lower!");
			}
		}else if(fameResult == -1){
			client.sendPacket(PacketFactory.fameErrorResponse(3));
		}else if(fameResult == -2){
			client.sendPacket(PacketFactory.fameErrorResponse(4));
		}else{
			client.sendPacket(PacketFactory.fameErrorResponse(6));
		}
		
	}

}
