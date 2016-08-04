package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.PetItem;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class SpawnPetHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		buf.skipBytes(4);
		
		short slot = buf.readShort();
		
		Item item = client.getCharacter().getInventory(InventoryType.CASH).getItem(slot);
		
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
		
		if(chr.isPetSpawned(item.getItemId())){
			chr.despawnPet((PetItem) item);
			client.sendReallowActions();
		}else{
			chr.spawnPet((PetItem) item);
			
			client.sendReallowActions();
		}
		
	}

}
