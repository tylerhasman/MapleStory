package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.PetItem;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class SpawnPetHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		buf.skipBytes(4);
		
		short slot = buf.readShort();
		
		CashItem item = (CashItem) client.getCharacter().getInventory(InventoryType.CASH).getItem(slot);
		
		if(item == null){
			client.sendReallowActions();
			return;
		}
		
		if(!(item instanceof PetItem)){
			client.getLogger().warn(client.getCharacter().getName()+" tried to spawn pet but it isn't a PetItem it's a "+item.getClass().getSimpleName());
			client.sendReallowActions();
			return;
		}
		
		MapleCharacter chr = client.getCharacter();
		
		System.out.println(chr.isPetSpawned(item.getUniqueId())+" "+item);
		
		if(chr.isPetSpawned(item.getUniqueId())){
			chr.despawnPet((PetItem) item);
			client.sendReallowActions();
		}else{
			chr.spawnPet((PetItem) item);
			
			client.sendReallowActions();
		}
		
	}

}
