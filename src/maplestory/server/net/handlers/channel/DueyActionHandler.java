package maplestory.server.net.handlers.channel;

import database.MapleDatabase;
import database.QueryResult;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.DueyParcel;
import maplestory.inventory.item.EquipItem;
import maplestory.inventory.item.Item;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class DueyActionHandler extends MaplePacketHandler {

	private static final int QUICK_SEND_ITEM_ID = 5330000;
	
	private static final int ACTION_CLOSE = 0x07, ACTION_SEND = 0x02, ACTION_OPEN_PARCEL = 0x04, ACTION_REMOVE_PARCEL = 0x05;
	
	private static final int 
			RESPONSE_PACKAGE_SUCCESS = 0x12,
			RESPONSE_NOT_ENOUGH_MESOS = 0x0A,
			RESPONSE_NAME_DOES_NOT_EXIST = 0x0C,
			RESPONSE_SAME_ACCOUNT = 0x0D;

	
	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		if(!client.getCharacter().isDueyOpen()){
			return;
		}
		
		int action = buf.readByte();
		
		if(action == ACTION_CLOSE){
			client.getCharacter().setDueyOpen(false);
		}else if(action == ACTION_SEND){
			byte inventoryType = buf.readByte();
			short itemPos = buf.readShort();
			short amount = buf.readShort();
			
			InventoryType inv = InventoryType.getById(inventoryType);
			String message = null;
			String itemData = null;
			
			int mesos = buf.readInt();
			
			if(mesos < 0 || (inv != null && amount < 1)){
				client.closeConnection();
				return;
			}
			
			Item item = null;
			
			if(inv != null){
				item = client.getCharacter().getInventory(inv).getItem(itemPos);
				
				if(item instanceof EquipItem){
					EquipItem eq = (EquipItem) item;
					itemData = eq.getStatInfo().serialize().toString();
				}
			}
			
			int cost = mesos + 5000;
			
			String recipientName = readMapleAsciiString(buf);
			
			boolean isQuickSend = buf.readBoolean();
			
			if(isQuickSend){
				
				if(client.getCharacter().getItemQuantity(QUICK_SEND_ITEM_ID, false) > 0){
					
					message = readMapleAsciiString(buf);
					
					if(message.length() > 100){
						client.setLoginMessage("Your game connection was closed earlier due to an error when you attempted to send a quick delivery parcel.");
						client.closeConnection();
						return;
					}
					
					client.getCharacter().getInventory(InventoryType.CASH).removeItem(QUICK_SEND_ITEM_ID, 1);
					
				}else{
					client.setLoginMessage("Your game connection was closed earlier due to an error when you attempted to send a quick delivery parcel.");
					client.closeConnection();
					return;
				}
				
			}
			
			int fee = getFee(mesos);
			
			mesos -= fee;
			
			if(client.getCharacter().getMeso() < cost){
				client.sendPacket(PacketFactory.dueyResponse(RESPONSE_NOT_ENOUGH_MESOS));
			}else{
				
				int characterId = -1;
				
				try{
					characterId = getCharId(recipientName);
				}catch(SQLException e){
					client.sendPacket(PacketFactory.dueyResponse(RESPONSE_NAME_DOES_NOT_EXIST));
					client.getCharacter().sendMessage(MessageType.POPUP, "An error occured when sending your package. No items or mesos have been deducted.");
					return;
				}
				
				if(characterId < 0){
					client.sendPacket(PacketFactory.dueyResponse(RESPONSE_NAME_DOES_NOT_EXIST));
					return;
				}
				
				try{
					if(client.hasCharacterWithId(characterId)){
						client.sendPacket(PacketFactory.dueyResponse(RESPONSE_SAME_ACCOUNT));
						return;
					}
				}catch(SQLException e){
					client.sendPacket(PacketFactory.dueyResponse(RESPONSE_SAME_ACCOUNT));
					client.getCharacter().sendMessage(MessageType.POPUP, "An error occured when sending your package. No items or mesos have been deducted.");
					return;
				}
				
				/*MapleCharacter recipient = client.getWorld().getPlayerStorage().getById(characterId);
				
				if(recipient != null){
					
				}*/
				
				long expirationTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30);
				
				int itemId = (item == null) ? -1 : item.getItemId();
				
				client.getCharacter().giveMesos(-cost, false, true);
				if(item != null){
					client.getCharacter().getInventory(inv).removeItem(itemId, amount);
				}
				
				MapleDatabase.getInstance().execute("INSERT INTO `duey_packages` (`sender`, `mesos`, `expiration_time`, `item`, `recipient`, `item_data`, `message`, `item_amount`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", client.getCharacter().getName(), mesos, expirationTime, itemId, characterId, itemData, message, amount);
				
				client.sendPacket(PacketFactory.dueyResponse(RESPONSE_PACKAGE_SUCCESS));
			}
			
		}else if(action == ACTION_OPEN_PARCEL){
			
			int parcelId = buf.readInt();
			
			DueyParcel parcel = getParcel(client.getCharacter(), parcelId);
			
			if(parcel == null){
				client.setLoginMessage("Your game connection was closed earlier because of an error when taking out a duey parcel.");
				client.closeConnection();
				return;
			}
			
			Item gift = parcel.getGift();

			if(gift != null){
				if(client.getCharacter().getInventory(gift.getItemId()).hasSpace(1)){
					client.getCharacter().getInventory(gift.getItemId()).addItem(gift);
				}else{
					client.getCharacter().sendMessage(MessageType.POPUP, "Your inventory is full");
					client.sendReallowActions();
					return;
				}
			}
			
			MapleDatabase.getInstance().execute("DELETE FROM `duey_packages` WHERE `id`=?", parcelId);
			
			client.getCharacter().giveMesos(parcel.getMesos(), false, false);
			
			client.sendPacket(PacketFactory.dueyRemoveItem(false, parcelId));
			
			
			//MapleDatabase.getInstance().execute("DELETE FROM `duey_packages` WHERE `id`=?", parcelId);
			
			
		}else if(action == ACTION_REMOVE_PARCEL){
			int parcelId = buf.readInt();
			
			DueyParcel parcel = getParcel(client.getCharacter(), parcelId);
			
			if(parcel != null){
				MapleDatabase.getInstance().execute("DELETE FROM `duey_packages` WHERE `id`=?", parcelId);
				
				client.sendPacket(PacketFactory.dueyRemoveItem(true, parcelId));
				
			}else{
				client.setLoginMessage("Your game connection was closed earlier because of an error when deleting a duey parcel.");
				client.closeConnection();
			}
			
		}
		
	}

	private static DueyParcel getParcel(MapleCharacter chr, int id) throws SQLException{
		DueyParcel parcel = null;
		
		for(DueyParcel parc : chr.getDueyParcels()){
			if(parc.getParcelId() == id){
				return parc;
			}
		}
		
		return null;
	}
	
	private static int getCharId(String name) throws SQLException{
		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `id` FROM `characters` WHERE `name`=?", name);
		
		if(results.size() > 0){
			return results.get(0).get("id");
		}
		
		return -1;
	}

	private static int getFee(int mesos) {
		
		if(mesos >= 100_000_000){
			return (int) (mesos * 0.06D);
		}else if(mesos >= 25_000_000){
			return (int) (mesos * 0.05D);
		}else if(mesos >= 10_000_000){
			return (int) (mesos * 0.04D);
		}else if(mesos >= 5_000_000){
			return (int) (mesos * 0.03D);
		}else if(mesos >= 1_000_000){
			return (int) (mesos * 0.018D);
		}else if(mesos >= 100_000){
			return (int) (mesos * 0.008D);
		}
		
		return 0;
	}

}
