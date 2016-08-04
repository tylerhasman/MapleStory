package maplestory.server.net.handlers.channel;


import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.MapleEquippedInventory;
import maplestory.server.net.MaplePacketHandler;

public class ItemMoveHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.skipBytes(4);
		InventoryType type = InventoryType.getById(buf.readByte());
		
		if(type == null){
			client.getLogger().warn("Unknown inventory type "+type);
			return;
		}
		
		byte src = (byte) buf.readShort();
		byte action = (byte) buf.readShort();
		
		short amount = buf.readShort();
		
		MapleEquippedInventory equipInventory = (MapleEquippedInventory) client.getCharacter().getInventory(InventoryType.EQUIPPED);
		Inventory inv = client.getCharacter().getInventory(type);
		
		if(src < 0 && action > 0){
			equipInventory.unequip(src, action);
		}else if(action < 0){
			equipInventory.equip(src, action);
		}else if(action == 0){
			if(src < 0){
				equipInventory.dropItem(src, amount);
			}else{
				inv.dropItem(src, amount);
			}
		}else{
			inv.moveItem(src, action);
		}
		
	}

}
