package maplestory.server.net.handlers.channel;

import java.util.List;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.BoxItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.BoxItem.Reward;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class UseBoxItemHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		short slot = buf.readShort();
		
		int itemId = buf.readInt();
		
		Inventory inventory = client.getCharacter().getInventory(itemId);
		
		Item item = inventory.getItem(slot);
		
		if(item == null || item.getItemId() != itemId){
			client.sendReallowActions();
			client.getCharacter().sendMessage(MessageType.POPUP, "Error using item.");
			return;
		}
		
		if(!checkInventoryRoom(client.getCharacter())){
			client.sendReallowActions();
			client.getCharacter().openSimpleTextNpc("Please ensure you have one free slot in your EQUIP, USE, SETUP, ETC and CASH inventories.");
			return;
		}
		
		if(item instanceof BoxItem){
			BoxItem box = (BoxItem) item;
			
			List<Reward> rewards = box.getRewards();
			
			for(Reward reward : rewards){
				
				Item rewardItem = reward.getItem();
				
				Inventory rewardInventory = client.getCharacter().getInventory(rewardItem.getItemId());
				
				if(!rewardInventory.hasSpace(rewardItem.getItemId(), rewardItem.getAmount())){
					continue;
				}
				
				if(reward.shouldGive()){
					rewardInventory.addItem(rewardItem);
					reward.broadcastWorldMessage(client.getCharacter());
					break;
				}
				
			}
		}else{
			client.getCharacter().sendMessage(MessageType.POPUP, "Failed to open that item. Please report this incident");
		}

		client.sendReallowActions();
		
	}
	
	private boolean checkInventoryRoom(MapleCharacter chr){
		
		InventoryType[] types = new InventoryType[] {
				
				InventoryType.CASH,
				InventoryType.EQUIP,
				InventoryType.ETC,
				InventoryType.SETUP,
				InventoryType.USE
				
		};
		
		for(InventoryType type : types){
			if(chr.getInventory(type).getFreeSlot() == -1){
				return false;
			}
		}
		
		return true;
		
	}

}
