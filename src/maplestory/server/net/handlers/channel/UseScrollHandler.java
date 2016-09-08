package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.EquipItem;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.inventory.item.ScrollItem;
import maplestory.inventory.item.ScrollResult;
import maplestory.inventory.item.ScrollStatInfo;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class UseScrollHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {

		int whatever = buf.readInt();
		
		short slot = buf.readShort();
		short dst = buf.readShort();
		
		byte ws = (byte) buf.readShort();
		
		boolean whiteScroll = false;
		boolean legendarySpirit = dst >= 0;
		
		if((ws & 2) == 2){
			whiteScroll = true;
		}
		
		Inventory takenFrom = null;
		
		EquipItem target = null;
		
		if(dst < 0){
			takenFrom =  client.getCharacter().getInventory(InventoryType.EQUIPPED);
		}else if(legendarySpirit && client.getCharacter().getSkillLevel(1003) > 0){
			takenFrom = client.getCharacter().getInventory(InventoryType.EQUIP);
		}else{
			client.sendReallowActions();
			client.getCharacter().sendMessage(MessageType.POPUP, "Error occured while scrolling!");
			return;
		}
		
		target = (EquipItem) takenFrom.getItem(dst);
		
		Inventory use = client.getCharacter().getInventory(InventoryType.USE);
		
		if(!(use.getItem(slot) instanceof ScrollItem)){
			client.getLogger().warn("Tried to use non-scroll "+use.getItem(slot)+"!");
			client.sendPacket(PacketFactory.getInventoryNoOp());
			return;
		}
		
		ScrollItem scroll = (ScrollItem) use.getItem(slot);
		
		if(target.getUpgradeSlotsAvailble() < 1 && !scroll.isA(ItemType.CLEAN_SLATE_SCROLL)){
			client.sendPacket(PacketFactory.getInventoryNoOp());
			return;
		}
		
		ScrollStatInfo scrollInfo = ItemInfoProvider.getScrollStatInfo(scroll.getItemId());
		
		if(!scrollInfo.isItemUseableOn(target.getItemId())){
			client.sendPacket(PacketFactory.getInventoryNoOp());
			return;
		}
		
		if(whiteScroll && use.countById(2340000) <= 0){
			whiteScroll = false;
		}
		
		if(!scroll.isA(ItemType.CHAOS_SCROLL) && !scroll.isA(ItemType.CLEAN_SLATE_SCROLL) && !scroll.isUseableOn(target.getItemId())){
			client.sendPacket(PacketFactory.getInventoryNoOp());
			return;
		}
		
		if(scroll.isA(ItemType.CLEAN_SLATE_SCROLL) && !(target.getLevel() + target.getUpgradeSlotsAvailble() < ItemInfoProvider.getEquipInfo(target.getItemId()).getUpgradeSlots())){
			return;
		}
		
		ScrollResult result = scroll.useScroll(target);

		use.removeItemFromSlot(slot, 1);
		
		if(result.isSuccess()){
			takenFrom.setItem(dst, result.getResult());
		}else if(result.isDestroyed()){
			takenFrom.removeItemFromSlot(dst, 1);
		}else{
			takenFrom.setItem(dst, result.getResult());
		}
		
		client.getCharacter().getMap().broadcastPacket(PacketFactory.scrollEffect(client.getCharacter(), result, legendarySpirit));
		
	}

}
