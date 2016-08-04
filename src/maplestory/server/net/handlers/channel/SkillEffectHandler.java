package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;

public class SkillEffectHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int skillId = buf.readInt();
		int level = buf.readByte();
		byte flags = buf.readByte();
		int speed = buf.readByte();
		byte direction = buf.readByte();
		
		Skill skill = SkillFactory.getSkill(skillId);
		
		if(skill.hasEffect()){
			client.getCharacter().getMap().broadcastPacket(PacketFactory.skillEffect(client.getCharacter(), skillId, level, flags, speed, direction), client.getCharacter().getId());
		}else{
			client.getLogger().warn(client.getCharacter().getName()+" got here using "+skillId+" named "+SkillFactory.getSkillName(skillId));
		}
	}

}
