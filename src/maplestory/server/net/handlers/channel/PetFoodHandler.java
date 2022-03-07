package maplestory.server.net.handlers.channel;

import constants.ExpTable;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemType;
import maplestory.player.MapleCharacter;
import maplestory.player.MaplePetInstance;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.util.Randomizer;

public class PetFoodHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		MapleCharacter chr = client.getCharacter();
		
		if(chr.numPetsSpawned() == 0){
			client.sendReallowActions();
			return;
		}
		
		MaplePetInstance hungriest = null;
		
		for(MaplePetInstance instance : chr.getPets()){
			if(instance != null){
				if(hungriest == null)
					hungriest = instance;
				else if(instance.getSource().getFullness() < hungriest.getSource().getFullness())
					hungriest = instance;
			}
		}
		
		buf.skipBytes(4);
		
		short slot = buf.readShort();
		int itemId = buf.readInt();
		
		Item petFood = chr.getInventory(InventoryType.USE).getItem(slot);
		
		if(petFood == null || petFood.getItemId() != itemId || !petFood.isA(ItemType.PET_FOOD)){
			client.sendReallowActions();
			
			client.getLogger().warn("Player tried to feed their pet a non-petfood item "+petFood+" "+slot);
			
			return;
		}

		int index = chr.getPetSlot(hungriest.getSource().getUniqueId());
		
		boolean gainCloseness = Randomizer.nextInt(100) > 50;
		
		int fullness = hungriest.getSource().getFullness();
		
		if(fullness < 100){
			int closeness = hungriest.getSource().getCloseness();
			
			if(closeness >= 30000)
				gainCloseness = false;
			
			
			fullness = Math.min(100, fullness + 30);
			
			hungriest.getSource().setFullness(fullness);
			
			
			if(gainCloseness){
				closeness++;
				hungriest.getSource().setCloseness(closeness);
				
				if(closeness >= ExpTable.getClosenessNeededForLevel(hungriest.getSource().getPetLevel())){
					hungriest.getSource().setPetLevel(hungriest.getSource().getPetLevel() + 1);
					
					client.sendPacket(PacketFactory.showPetLevelUp(chr, index, true));
					chr.getMap().broadcastPacket(PacketFactory.showPetLevelUp(chr, index, false), chr.getId());
				}
				
			}
			chr.getMap().broadcastPacket(PacketFactory.petCommandResponse(chr.getId(), index, 1, true));
			
		}else{
			/*int closeness = hungriest.getSource().getCloseness();
			
			if(closeness <= 0)
				gainCloseness = false;*/
			chr.getMap().broadcastPacket(PacketFactory.petCommandResponse(chr.getId(), index, 0, false));
			
		}
		
		
		chr.getInventory(InventoryType.USE).removeItemFromSlot(slot, 1);
		
		int petSlot = chr.getInventory(InventoryType.CASH).findByCashId(hungriest.getSource().getUniqueId());
		chr.getInventory(InventoryType.CASH).setItem(petSlot, hungriest.getSource());
	}

}
