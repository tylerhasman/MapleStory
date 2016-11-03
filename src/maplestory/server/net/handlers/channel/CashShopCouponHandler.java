package maplestory.server.net.handlers.channel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import constants.MessageType;
import database.MapleDatabase;
import database.QueryResult;
import io.netty.buffer.ByteBuf;
import maplestory.cashshop.CashShopWallet;
import maplestory.cashshop.CashShopWallet.CashShopCurrency;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class CashShopCouponHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.skipBytes(2);
		
		String code = readMapleAsciiString(buf);
		CashShopWallet wallet = CashShopWallet.getWallet(client);
		try {
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT * FROM `cashshop_codes` WHERE `code`=?", code);
			
			if(results.size() == 0){
				client.getCharacter().sendMessage(MessageType.POPUP, "Invalid coupon!");
			}else{
				
				System.out.println(client.getId());
				
				QueryResult result = results.get(0);
				
				int id = result.get("id");
				int nxCash = result.get("nx_cash");
				int maplePoints = result.get("maple_points");
				int nxPrepaid = result.get("nx_prepaid");
				int item = result.get("item");
				int itemAmount = result.get("item_amount");
				int mesos = result.get("mesos");
				boolean used = ((int)result.get("used") == 1);
				
				if(used){
					client.getCharacter().sendMessage(MessageType.POPUP, "That coupon has already been redeemed!");
				}else{
					MapleDatabase.getInstance().execute("UPDATE `cashshop_codes` SET `used`=? WHERE `id`=?", 1, id);
					StringBuffer message = new StringBuffer("You have been credited the following:\r\n");
					
					if(nxCash > 0 || maplePoints > 0 || nxPrepaid > 0){
						
						wallet.giveCash(CashShopCurrency.NX_CASH, nxCash);
						wallet.giveCash(CashShopCurrency.MAPLE_POINTS, maplePoints);
						wallet.giveCash(CashShopCurrency.PREPAID, nxPrepaid);
						
						wallet.commitChanges();
						
						client.sendPacket(PacketFactory.updateCashshopCash(wallet));
						
						if(nxCash > 0){
							message.append("\r\n"+nxCash+" Nx Cash");
						}
						if(maplePoints > 0){
							message.append("\r\n"+maplePoints+" Maple Points");
						}
						if(nxPrepaid > 0){
							message.append("\r\n"+maplePoints+" Nx Prepaid\r\n");
						}
						
						
					}
					
					if(itemAmount > 0 || mesos > 0){
						List<CashItem> cashItems = new ArrayList<>();
						List<Item> normalItems = new ArrayList<>();
						if(itemAmount > 0){
							Item freeItem = ItemFactory.getItem(item, itemAmount);
							
							if(freeItem instanceof CashItem){
								cashItems.add((CashItem) freeItem);
							}else{
								normalItems.add(freeItem);
							}	
							client.getCharacter().getInventory(freeItem.getItemId()).addItem(freeItem);
							
							message.append(InventoryType.getByItemId(freeItem.getItemId()).name()+": "+ItemInfoProvider.getItemName(freeItem.getItemId())+" x "+freeItem.getAmount()+"\r\n");
						}
						
						client.getCharacter().giveMesos(mesos, false, false);
						
						if(mesos > 0){
							message.append(mesos+" mesos\r\n");
						}
						
						client.getCharacter().sendMessage(MessageType.POPUP, message.toString());
						//client.sendPacket(PacketFactory.showCouponRedeemedItem(item));
						//client.sendPacket(PacketFactory.couponShowRedeemedItem(client, cashItems, normalItems, maplePoints, mesos));
					}
					
					if(!message.toString().isEmpty())
						client.getCharacter().sendMessage(MessageType.POPUP, message.toString());
					
				}
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			client.getCharacter().sendMessage(MessageType.POPUP, "An error occured when processing your coupon code, please try again later.");
		}finally{
			client.sendPacket(PacketFactory.updateCashshopCash(wallet));
		}
		
	}

}
