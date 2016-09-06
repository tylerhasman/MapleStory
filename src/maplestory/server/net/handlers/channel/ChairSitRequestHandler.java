package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class ChairSitRequestHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		int chairId = buf.readInt();
		
		if(ItemType.CHAIR.isThis(chairId)){
			
			if(ItemInfoProvider.getEquipInfo(chairId).getRequiredLevel() <= client.getCharacter().getLevel()){
				
				client.getCharacter().setActiveChair(chairId);
				
				client.getCharacter().getMap().broadcastPacket(PacketFactory.portalChairEffect(client.getCharacter()), client.getCharacter().getId());
				
				client.sendReallowActions();
				
			}else{
				client.closeConnection();
				client.getLogger().warn("Client tried to sit in chair above level req");
			}
			
		}else{
			client.closeConnection();
			client.getLogger().warn("Client tried to sit in non-chair item "+chairId);
		}
		
	}

}
