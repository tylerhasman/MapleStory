package maplestory.server.net.handlers.channel;

import java.sql.SQLException;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemType;
import maplestory.inventory.storage.MapleStorageBox;
import maplestory.inventory.storage.MapleStorageBox.StoragePacketType;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.util.Hex;

public class StorageHandler extends MaplePacketHandler {

	public static final int CLOSE = 8, WITHDRAW_ITEM = 4, DEPOSIT_ITEM = 5, ARRANGE = 6, MESO_OP = 7;
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) throws SQLException {
		
		MapleCharacter chr = client.getCharacter();
		
		byte mode = buf.readByte();
		
		MapleStorageBox storage = chr.getOpenStorageBox();
		
		if(storage == null){
			return;
		}
		
		if(mode == CLOSE){
			storage.commitChanges(client.getId());
			client.getLogger().info("Committed storage to database");
			chr.setOpenStorageBox(null);
		}else if(mode == WITHDRAW_ITEM){
			InventoryType type = InventoryType.getById(buf.readByte());
			int slot = buf.readByte();
			
			if(slot < 0 || slot > storage.getSize()){
				return;
			}
			
			Item item = storage.getItem(type, slot);
			
			if(item == null){
				return;
			}
			
			
			Inventory inv = chr.getInventory(item.getItemId());
			if(inv.isFull()){
				client.sendPacket(PacketFactory.storageError(StoragePacketType.ERR_PLAYER_INVENTORY_FULL));
				return;
			}
			
			int cost = storage.getWithdrawItemCost();
			
			if(chr.getMeso() >= cost){
				chr.giveMesos(-cost, false, false);
				storage.removeItem(type, slot);
				
				inv.addItem(item);
				client.sendPacket(PacketFactory.storageRemoveItem(inv.getType(), storage));
			}else{
				client.sendPacket(PacketFactory.storageError(StoragePacketType.ERR_NOT_ENOUGH_MESOS));
			}
		}else if(mode == ARRANGE){
			client.sendPacket(PacketFactory.storageUpdateMeso(storage));
		}else if(mode == DEPOSIT_ITEM) {
			
			int slot = buf.readShort();
			int itemId = buf.readInt();
			int amount = buf.readShort();
			
			InventoryType type = InventoryType.getByItemId(itemId);
			Inventory inv = chr.getInventory(type);
			
			if(slot < 1 || slot > inv.getSize()){
				return;
			}
			
			if(amount < 1 || chr.getItemQuantity(itemId, false) < amount){
				return;
			}
			
			if(storage.isFull()){
				client.sendPacket(PacketFactory.storageError(StoragePacketType.ERR_STORAGE_FULL));
				return;
			}
			
			int cost = storage.getDepositItemCost();
			
			if(chr.getMeso() >= cost){
				Item item = inv.getItem(slot);
				
				if(item != null & (item.getItemId() == itemId && (item.getAmount() >= amount || item.isA(ItemType.RECHARGABLE)))){
					item = item.copy();
					if(item.isA(ItemType.RECHARGABLE)){
						amount = item.getAmount();
					}
					
					chr.giveMesos(-cost, false, false);
					inv.removeItemFromSlot(slot, amount);
					item.setAmount(amount);
					storage.addItem(item);
					client.sendPacket(PacketFactory.storageAddItem(type, storage));
				}
			}else{
				client.sendPacket(PacketFactory.storageError(StoragePacketType.ERR_NOT_ENOUGH_MESOS));
			}

		}else if(mode == MESO_OP){
			int meso = buf.readInt();
			
			if(meso < 0){
				if(chr.getMeso() >= -meso){
					chr.giveMesos(meso, false, false);
					storage.addMesos(-meso);
					
				}
			}else{
				if(storage.getMesos() >= meso){
					storage.addMesos(-meso);
					chr.giveMesos(meso, false, false);
				}
			}
			
			client.sendPacket(PacketFactory.storageUpdateMeso(storage));
		}else{
			client.getLogger().warn("Unhandled storage operation "+mode+" > "+Hex.toHex(buf.readBytes(buf.readableBytes()).array()));
		}
		
	}

}
