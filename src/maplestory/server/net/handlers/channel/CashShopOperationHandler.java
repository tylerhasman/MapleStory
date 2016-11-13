package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.cashshop.CashShopInventory;
import maplestory.cashshop.CashShopItemData;
import maplestory.cashshop.CashShopPackage;
import maplestory.cashshop.CashShopWallet;
import maplestory.cashshop.CashShopWallet.CashShopCurrency;
import maplestory.client.MapleClient;
import maplestory.inventory.Inventory;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class CashShopOperationHandler extends MaplePacketHandler {

	@AllArgsConstructor
	private static enum OperationType {
		
		/**
		 * Code: 0x03
		 */
		BUY_ITEM(0x03),
		/**
		 * Code: 0x1E
		 */
		BUY_PACKAGE(0x1E),
		/**
		 * Code: 0x04
		 */
		SEND_GIFT(0x04),
		/**
		 * Code: 0x05
		 */
		MODIFY_WISHLIST(0x05),
		/**
		 * Code: 0x06
		 */
		INCREASE_INVENTORY_SIZE(0x06),
		/**
		 * Code: 0x07
		 */
		INCREASE_STORAGE_SIZE(0x07),
		/**
		 * Code: 0x08
		 */
		INCREASE_CHARACTER_SLOTS(0x08),
		/**
		 * Code: 0x0D
		 */
		REMOVE_FROM_CASH_INVENTORY(0x0D),
		/**
		 * Code: 0x0E
		 */
		ADD_TO_CASH_INVENTORY(0x0E),
		/**
		 * Code: 0x01D
		 */
		CRUSH_RING(0x01D),
		/**
		 * Code: 0x20
		 */
		BUY_QUEST_ITEM(0x20),
		/**
		 * Code: 0x23
		 */
		FRIENDSHIP_RING(0x23),
		
		;
		
		@Getter
		private final int code;
		
		public static OperationType getById(int id){
			for(OperationType type : values()){
				if(type.code == id){
					return type;
				}
			}
			return null;
		}
		
	}
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		MapleCharacter chr = client.getCharacter();
		
		OperationType action = OperationType.getById(buf.readByte());
		
		CashShopWallet wallet = CashShopWallet.getWallet(client);
		
		CashShopInventory cashInventory = CashShopInventory.getCashInventory(client.getId());
		
		if(action == OperationType.BUY_ITEM || action == OperationType.BUY_PACKAGE){
			buf.skipBytes(1);
			CashShopCurrency currency = CashShopCurrency.getById(buf.readInt());
			int boughtId = buf.readInt();
			
			CashShopItemData itemData = ItemInfoProvider.getCashShopItemData(boughtId);
			
			if(itemData == null || !itemData.isOnSale() || wallet.getCash(currency) < itemData.getPrice()){
				return;
			}
			
			if(currency == null){
				return;
			}
			
			if(action == OperationType.BUY_ITEM){

				Item item = itemData.createItem();
				
				if(!(item instanceof CashItem)){
					client.getLogger().warn("Tried to buy cash item but not a CashItem. SN: "+itemData.getCashEntryId());
					return;
				}
				
				cashInventory.addItem(item);
				
				client.sendPacket(PacketFactory.cashShopItemBought((CashItem)item, client.getId()));
			}else if(action == OperationType.BUY_PACKAGE){
				CashShopPackage cashPackage = ItemInfoProvider.getCashShopPackage(itemData.getItemId());
				
				if(cashPackage == null){
					client.getCharacter().sendMessage(MessageType.POPUP, "An error occured, no funds have been deducted.");
					client.sendReallowActions();
					return;
				}
				
				for(CashShopItemData data : cashPackage.getItems()){
					cashInventory.addItem(data.createItem());
				}
				
				client.sendPacket(PacketFactory.cashShopPackageBought(cashPackage, client.getId()));
				
				
			}

			wallet.spendCash(currency, itemData.getPrice());
			client.sendPacket(PacketFactory.updateCashshopCash(wallet));
		}else if(action == OperationType.SEND_GIFT){
			
			int birthday = buf.readInt();
			
		}else if(action == OperationType.REMOVE_FROM_CASH_INVENTORY){
			
			int itemId = buf.readInt();
			
			Item item = cashInventory.findById(itemId);
			
			if(item == null){
				chr.sendMessage(MessageType.POPUP, "Error occured, try again later");
				client.sendPacket(PacketFactory.updateCashshopCash(wallet));
				return;
			}
			
			Inventory cashInv = chr.getInventory(item.getItemId());
			
			if(cashInv.isFull()){
				chr.sendMessage(MessageType.POPUP, "Your cash inventory is full!");
				return;
			}
			
			int slot = cashInv.getFreeSlot();
			
			cashInv.addItem(item);
			cashInventory.removeItem(item);
			
			client.sendPacket(PacketFactory.cashShopTakeItem(item, slot));
			
		}else{
			client.getLogger().warn("Unhandled cash shop operation "+action);
			client.sendPacket(PacketFactory.updateCashshopCash(wallet));
		}
		
	}

}
