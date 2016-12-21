package maplestory.inventory.item;

import java.util.List;

import constants.MessageType;
import lombok.AllArgsConstructor;
import maplestory.player.MapleCharacter;
import maplestory.util.Randomizer;
import maplestory.world.World;

/**
 * An item the player can use to get rewards.
 *
 */
public interface BoxItem extends Item {
	
	public List<Reward> getRewards();
	
	@AllArgsConstructor
	public static class Reward {
		
		private int itemId, period;
		private int totalProbibility;
		private int probiblity, amount;
		@SuppressWarnings("unused")//TODO: Use effect
		private String effect, worldMsg;
		
		public Item getItem(){
			
			Item item = null;
			
			if(period != -1){
				item = ItemFactory.getItem(itemId, amount, null, period * 60 * 60 * 10);
			}else{
				item = ItemFactory.getItem(itemId, amount);
			}
			
			return item;
		}
		
		private String createWorldMsg(String characterName){
			return worldMsg.replaceAll("/name", characterName).replaceAll("/item", ItemInfoProvider.getItemName(itemId));
		}
		
		public void broadcastWorldMessage(MapleCharacter character){
			if(worldMsg == null){
				return;
			}
			World world = character.getWorld();
			world.broadcastMessage(MessageType.LIGHT_BLUE_TEXT, createWorldMsg(character.getName()));
		}
		
		public boolean shouldGive(){
			return Randomizer.nextInt(totalProbibility) < probiblity;
		}
		
	}
	
}
