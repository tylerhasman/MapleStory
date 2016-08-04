package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import constants.ExpTable;
import constants.MessageType;

public class UseMountFoodHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.skipBytes(6);
		
		int itemid = buf.readInt();
		
		MapleCharacter chr = client.getCharacter();
		
		if(chr.getInventory(InventoryType.USE).countById(itemid) > 9){
			
			if(chr.getMount() != null && chr.getMount().getTiredness() > 0){
				chr.getMount().setTiredness(Math.max(chr.getMount().getTiredness()-30, 0));
				chr.getMount().setExp(2 * chr.getMount().getLevel() + 6 + chr.getMount().getExp());
				
				int level = chr.getMount().getLevel();
				boolean levelup = chr.getMount().getExp() >= ExpTable.getMountExpNeededForLevel(level) && level < 31;
				
				if(levelup){
					chr.getMount().setLevel(level + 1);
				}
				
				chr.getMap().broadcastPacket(PacketFactory.updateMount(chr.getId(), chr.getMount(), levelup));
				
				chr.getInventory(InventoryType.USE).removeItem(itemid, 1);
			
			}else{
				if(chr.getMount() == null){
					chr.sendMessage(MessageType.POPUP, "You must be riding a mount to use "+ItemInfoProvider.getItemName(itemid));
				}else if(chr.getMount().getTiredness() > 0){
					chr.sendMessage(MessageType.POPUP, "Your mount is not tired!");
				}
			}
			
		}
		
		client.sendReallowActions();
	}

}
