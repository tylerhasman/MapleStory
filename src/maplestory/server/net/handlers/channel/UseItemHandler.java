package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class UseItemHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		MapleCharacter chr = client.getCharacter();
		
		if(!chr.isAlive()){
			client.sendReallowActions();
			return;
		}
		
		buf.skipBytes(4);
		
		byte slot = (byte) buf.readShort();
		int itemId = buf.readInt();
		
		Item toUse = chr.getInventory(InventoryType.USE).getItem(slot);
		
		if(toUse != null && toUse.getAmount() > 0 && toUse.getItemId() == itemId){
			if(handleDispelItems(client, itemId, slot)){
				return;
			}
			
			if(toUse.isA(ItemType.TOWN_SCROLL)){
				if(ItemInfoProvider.getItemEffect(toUse.getItemId()).applyTo(chr)){
					remove(client, slot);
				}
				return;
			}
			
			remove(client, slot);
			ItemInfoProvider.getItemEffect(toUse.getItemId()).applyTo(chr);
			//Check berserk here I guess
			
		}
	}
	
	private boolean handleDispelItems(MapleClient client, int itemId, byte slot){
		if (itemId == 2022178 || itemId == 2022433 || itemId == 2050004) {
            //Dispel all debuffs
            remove(client, slot);
            return true;
		} else if (itemId == 2050001) {
			//Dispell darkness
			remove(client, slot);
			return true;
		} else if (itemId == 2050002) {
			//Dispel weakness
			remove(client, slot);
			return true;
        } else if (itemId == 2050003) {
        	//Dispel seal and curse
            remove(client, slot);
            return true;
        }
		
		return false;
	}
	
    private void remove(MapleClient client, short slot) {
    	client.getCharacter().getInventory(InventoryType.USE).removeItemFromSlot(slot, 1);
        client.sendReallowActions();
    }


}
