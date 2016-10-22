package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.map.MapleMapItem;
import maplestory.map.MapleMapObject;
import maplestory.player.MapleCharacter;
import maplestory.player.MaplePetInstance;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class PetLootHandler extends MaplePacketHandler {

	private static final int MESO_MAGNET = 1812000, ITEM_POUCH = 1812001;
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		MapleCharacter chr = client.getCharacter();
		
		int petSlot = buf.readInt();

		MaplePetInstance pet = chr.getPetByUniqueId(petSlot);
		
		if(pet == null){
			return;
		}
		
		buf.skipBytes(13);
		int oid = buf.readInt();
		
		MapleMapObject obj = chr.getMap().getObject(oid);
		if(obj == null){
			return;
		}
		
		if(obj instanceof MapleMapItem){
			MapleMapItem item = (MapleMapItem) obj;
			
			if(item.isMesoDrop() && chr.getInventory(InventoryType.EQUIPPED).countById(MESO_MAGNET) >= 1){
				chr.giveMesos(item.getMesos(), true, false);
				chr.getMap().broadcastPacket(PacketFactory.getPickupDroppedItemPacket(item, chr, pet));
				chr.getMap().removeObject(item.getObjectId());
			}else if(chr.getInventory(InventoryType.EQUIPPED).countById(ITEM_POUCH) >= 1){
				if(chr.getInventory(item.getItemId()).addItem(item.getItem())){
					chr.getMap().broadcastPacket(PacketFactory.getPickupDroppedItemPacket(item, chr, pet));
					chr.getMap().removeObject(item.getObjectId());	
				}
			}
		}
	}

}
