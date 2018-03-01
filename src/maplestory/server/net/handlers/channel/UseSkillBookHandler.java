package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.SkillBook;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;
import maplestory.util.Randomizer;

public class UseSkillBookHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		if(!client.getCharacter().isAlive()){
			client.sendReallowActions();
			return;
		}
		
		buf.skipBytes(4);
		short slot = buf.readShort();
		int itemId = buf.readInt();
		MapleCharacter chr = client.getCharacter();
		
		Item toUse = chr.getInventory(InventoryType.USE).getItem(slot);
        
		System.out.println(toUse.getClass().getSimpleName());
		
		if(toUse instanceof SkillBook){
			if(toUse.getItemId() != itemId){
				return;
			}
			
			SkillBook sb = (SkillBook) toUse;

			boolean useable = false;
			boolean success = false;
			boolean newSkill = false;
			int maxlevel = sb.getMasterLevel();
			int skillId = sb.getSkill(chr.getJob());
			Skill skill = SkillFactory.getSkill(skillId);
			
			if(skill == null){
				chr.sendMessage(MessageType.POPUP, "You are unable to use that skill book at this time.");
				client.sendReallowActions();
				return;
			}
			
			if(skillId == 0){
				useable = false;
			}else if((chr.getSkillLevel(skill)) >= sb.getRequiredSkillLevel()){
				useable = true;
				if(Randomizer.nextInt(100) + 1 < sb.getSuccessRate()){
					success = true;
					
					if(chr.getSkillLevel(skill) == 0){
						newSkill = true;
					}
					
					chr.changeSkillLevel(skill, chr.getSkillLevel(skill), Math.max(maxlevel, chr.getMasterLevel(skill.getId())));
				}else{
					success = false;
				}
				chr.getInventory(InventoryType.USE).removeItemFromSlot(slot, 1);
			}else{
				client.getCharacter().sendMessage(MessageType.POPUP, "You do not meet the requirements for that mastery book.");
			}
			
			client.sendPacket(PacketFactory.skillBookSuccess(chr, skill, maxlevel, newSkill, useable, success));
			
		}
		
	}

}
