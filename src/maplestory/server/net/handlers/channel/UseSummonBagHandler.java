package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.SummoningBag;
import maplestory.server.net.MaplePacketHandler;

public class UseSummonBagHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		if(!client.getCharacter().isAlive()){
			client.sendReallowActions();
			return;
		}
		
		buf.skipBytes(4);
		
		byte slot = (byte) buf.readShort();
		int itemId = buf.readInt();
		
		Item toUse = client.getCharacter().getInventory(InventoryType.USE).getItem(slot);
		
		if(toUse != null && toUse.getAmount() > 0 && toUse.getItemId() == itemId){
			
			if(toUse instanceof SummoningBag){
				SummoningBag bag = (SummoningBag) toUse;
				bag.useBag(client.getCharacter().getMap(), client.getCharacter().getPosition());
				
				client.getCharacter().getInventory(InventoryType.USE).removeItemFromSlot(slot, 1);
			}else{
				client.getLogger().warn("Summoning Bag "+itemId+" isn't an instance of SummoningBag, it is a "+toUse.getClass().getSimpleName());
			}
			
		}
		
		client.sendReallowActions();
		
	}

}
