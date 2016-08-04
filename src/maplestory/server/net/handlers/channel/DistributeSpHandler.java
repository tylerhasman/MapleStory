package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;

public class DistributeSpHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {

		buf.readInt();
		int skillId = buf.readInt();
		MapleCharacter chr = client.getCharacter();
		
		int sp = chr.getRemainingSp();
		
		boolean isBeginnerSkill = false;
		
		if (skillId % 10000000 > 999 && skillId % 10000000 < 1003) {
            int total = 0;
            for (int i = 0; i < 3; i++) {
                total += chr.getSkillLevel(chr.getJob().getId() * 10000000 + 1000 + i);
            }
            sp = Math.min((chr.getLevel() - 1), 6) - total;
            isBeginnerSkill = true;
        }

		client.sendReallowActions();
		Skill skill = SkillFactory.getSkill(skillId);
		
		if(skill == null){
			return;
		}
		
		int curLevel = chr.getSkillLevel(skillId);
		if ((sp > 0 && curLevel + 1 <= (skill.isFourthJob() ? chr.getMasterLevel(skillId) : skill.getMaxLevel()))) {
            if (!isBeginnerSkill) {
                chr.setRemainingSp(chr.getRemainingSp() - 1);
            }
            chr.changeSkillLevel(skill, curLevel + 1, chr.getMasterLevel(skillId)/*, chr.getSkillExpiration(skill)*/);
        }
	}

}
