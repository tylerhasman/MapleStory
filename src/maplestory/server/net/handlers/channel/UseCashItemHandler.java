package maplestory.server.net.handlers.channel;

import java.util.Map.Entry;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.inventory.item.PetItem;
import maplestory.player.MapleCharacter;
import maplestory.player.MaplePetInstance;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.util.Hex;
import maplestory.util.StringUtil;

public class UseCashItemHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		MapleCharacter chr = client.getCharacter();
		
		buf.skipBytes(2);
		
		int itemId = buf.readInt();
		
		if(chr.getInventory(InventoryType.CASH).countById(itemId) <= 0){
			client.sendReallowActions();
			return;
		}
		
		if(itemId == 5170000){
			String name = readMapleAsciiString(buf);
			
			MaplePetInstance pet = client.getCharacter().getPets()[0];
			
			if(pet == null){
				client.sendReallowActions();
				return;
			}
			
			pet.getSource().setPetName(name);
			
			for(Entry<Integer, Item> slot : client.getCharacter().getInventory(InventoryType.CASH).getItems().entrySet()){
				
				if(slot.getValue() instanceof PetItem){
					PetItem pi = (PetItem) slot.getValue();
					if(pi.getUniqueId() == pet.getSource().getUniqueId()){
						client.getCharacter().getInventory(InventoryType.CASH).refreshItem(slot.getKey());
					}
				}
				
			}
			
			client.getCharacter().getMap().broadcastPacket(PacketFactory.changePetName(client.getCharacter(), name));
			
		}else if(itemId == 5071000){//Megaphone
			
			String text = readMapleAsciiString(buf);
			
			text = client.getCharacter().getFullName() + " : "+text;
			
			client.getWorld().broadcastPacket(PacketFactory.getServerMessagePacket(MessageType.MEGAPHONE, text, client.getChannelId(), false));
		}else if(itemId == 5076000){//Item megaphone

			String text = readMapleAsciiString(buf);
			
			boolean whisper = buf.readBoolean();
			
			text = client.getCharacter().getFullName() + " : "+text;
			
			Item item = null;
			
			int slot = 0;
			
			if(buf.readBoolean()){
				
				int inventoryType = buf.readInt();
				slot = buf.readInt();
				
				item = chr.getInventory(InventoryType.getById(inventoryType)).getItem(slot);
			}
			
			client.getWorld().broadcastPacket(PacketFactory.itemMegaphone(text, whisper, client.getChannelId(), slot, item));
		}else if(ItemType.MESSENGER.isThis(itemId)){
			
			String[] lines = new String[4];
			
			for(int i = 0; i < lines.length;i++){
				lines[i] = readMapleAsciiString(buf);
			}
			
			boolean whisper = buf.readBoolean();
			
			
			client.getWorld().broadcastPacket(PacketFactory.getAvatarMegaphone(chr, client.getChannelId(), itemId, lines, whisper));
			
			
			
		}else{
			
			byte[] remaining = new byte[buf.readableBytes()];
			buf.readBytes(remaining);
			
			client.getLogger().info("Used un-handled cash item "+itemId+" "+ItemInfoProvider.getItemName(itemId)+"\r\n"+Hex.toHex(remaining));
			
		}
		
		client.sendReallowActions();
	}

}
