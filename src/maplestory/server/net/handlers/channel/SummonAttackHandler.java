package maplestory.server.net.handlers.channel;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.life.MapleSummon;
import maplestory.map.MapleMapObject;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;

public class SummonAttackHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		int oid = buf.readInt();
		
		MapleCharacter chr = client.getCharacter();
		
		if(!chr.isAlive()){
			return;
		}
		
		MapleSummon summon = chr.getSummonByObjectId(oid);
		
		if(summon == null){
			return;
		}
		
		Skill summonSkill = SkillFactory.getSkill(summon.getSkill());
		MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
		
		buf.skipBytes(4);
		
		List<SummonAttack> attacks = new ArrayList<>();
		
		byte direction = buf.readByte();
		int numAttacks = buf.readByte();
		
		buf.skipBytes(8);
		
		for(int i = 0; i < numAttacks;i++){
			int moid = buf.readInt();
			buf.skipBytes(18);
			int damage = buf.readInt();
			attacks.add(new SummonAttack(moid, damage));
		}
		
		chr.getMap().broadcastPacket(PacketFactory.summonAttack(chr.getId(), summon.getSkill(), direction, attacks), chr.getId());
		
		for(SummonAttack attack : attacks){
			
			int damage = attack.getDamage();
			
			MapleMapObject obj = chr.getMap().getObject(attack.getMonsterOid());
			
			if(obj instanceof MapleMonster){
				MapleMonster target = (MapleMonster) obj;
				
				if(damage > 0 && summonEffect.getMonsterStati().size() > 0){
					
					if(summonEffect.makeChanceResult()){
						//Apply special effects like poison here
					}
					
				}
				
				target.damage(chr, damage);
			}
			
		}
	}
	
	@AllArgsConstructor
	@Data
	public static class SummonAttack {
		
		private int monsterOid;
		private int damage;
		
		
	}

}
