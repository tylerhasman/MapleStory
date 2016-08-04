package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleSummon;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.skill.SkillFactory;

public class DamageSummonHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int skillid = buf.readInt();
		int unkByte = buf.readByte();
		int damage = buf.readInt();
		int monsterIdFrom = buf.readInt();
		
		if(SkillFactory.getSkill(skillid) != null){
			
			MapleCharacter chr = client.getCharacter();
			MapleSummon summon = chr.getSummon(skillid);
			
			if(summon != null){
				summon.damage(damage, unkByte, monsterIdFrom);
			}
			
		}
	}

}
