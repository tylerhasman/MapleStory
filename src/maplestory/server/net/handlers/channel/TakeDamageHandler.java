package maplestory.server.net.handlers.channel;

import constants.MapleBuffStat;
import constants.skills.Bishop;
import constants.skills.FPArchMage;
import constants.MapleBuffStat;
import constants.MapleElement;
import constants.skills.Bishop;
import constants.skills.FPArchMage;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.map.MapleMapObject;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.server.net.PacketFactory;
import maplestory.util.Hex;


public class TakeDamageHandler extends MaplePacketHandler {

	private static final byte MELEE_ATTACK = -1;
	private static final byte MAGIC_ATTACK = 0;
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		buf.readInt();
		
		byte damageType = buf.readByte();
		byte element = buf.readByte();
		
		
		int damage = buf.readInt();

		MapleCharacter chr = client.getCharacter();
		
		if(damageType == 0) {//Skill
			int attackerMonsterId = buf.readInt();
			int attackerObjectId = buf.readInt();

			MapleMonster attacker = (MapleMonster) chr.getMap().getObject(attackerObjectId);
			System.out.println(chr.hasBuff(MapleBuffStat.MANA_REFLECTION));
			if(chr.hasBuff(MapleBuffStat.MANA_REFLECTION)) {
				float percent = chr.getBuffStatSource(MapleBuffStat.MANA_REFLECTION).getX();
				percent /= 100F;
				
				int returnedDamage = (int) (damage * percent);
				
				if(returnedDamage > attacker.getMaxHp() / 5) {
					returnedDamage = attacker.getMaxHp() / 5;
				}
				
				attacker.damage(chr, returnedDamage);
				int sourceId = chr.getBuffStatSource(MapleBuffStat.MANA_REFLECTION).getSourceId();
				client.sendPacket(PacketFactory.showOwnBuffEffect(sourceId, 5));
				
				chr.getMap().broadcastPacket(PacketFactory.showBuffEffect(chr.getObjectId(), sourceId, 5), chr.getId());
				chr.getMap().broadcastPacket(PacketFactory.getMonsterDamagePacket(attacker.getObjectId(), returnedDamage));
				
				
				
			}
			
		}
		
		client.getCharacter().damage(damage);
		
		if(damageType != -3) {//Has attacker
			int attackerMonsterId = buf.readInt();
			int attackerObjectId = buf.readInt();

			byte direction = buf.readByte();
			
			chr.getMap().broadcastPacket(PacketFactory.damagePlayer(chr.getObjectId(), damageType, damage, attackerMonsterId, direction), chr.getId());
			
			MapleMonster attacker = (MapleMonster) chr.getMap().getObject(attackerObjectId);
			
			if(chr.hasBuff(MapleBuffStat.MANA_REFLECTION) && damageType == MAGIC_ATTACK) {
				doManaReflection(chr, attacker, damage);
			}
			
			if(chr.hasBuff(MapleBuffStat.POWERGUARD)) {
				doPowerGuard(chr, attacker, damage);
			}
			
		}
		
	}
	
	private void doPowerGuard(MapleCharacter chr, MapleMonster attacker, int damage) {
		float percent = chr.getBuffStatSource(MapleBuffStat.POWERGUARD).getX();
		percent /= 100F;
		
		int returnedDamage = (int) (damage * percent);
		
		if(returnedDamage > attacker.getMaxHp() / 10) {
			returnedDamage = attacker.getMaxHp() / 10;
		}
		
		attacker.damage(chr, returnedDamage);
		
		chr.getMap().broadcastPacket(PacketFactory.getMonsterDamagePacket(attacker.getObjectId(), returnedDamage));
	}

	private void doManaReflection(MapleCharacter chr, MapleMonster attacker, int damage) {
		float percent = chr.getBuffStatSource(MapleBuffStat.MANA_REFLECTION).getX();
		percent /= 100F;
		
		int returnedDamage = (int) (damage * percent);
		
		if(returnedDamage > attacker.getMaxHp() / 5) {
			returnedDamage = attacker.getMaxHp() / 5;
		}
		
		attacker.damage(chr, returnedDamage);
		int sourceId = chr.getBuffStatSource(MapleBuffStat.MANA_REFLECTION).getSourceId();
		chr.getClient().sendPacket(PacketFactory.showOwnBuffEffect(sourceId, 5));
		
		chr.getMap().broadcastPacket(PacketFactory.showBuffEffect(chr.getObjectId(), sourceId, 5), chr.getId());
		chr.getMap().broadcastPacket(PacketFactory.getMonsterDamagePacket(attacker.getObjectId(), returnedDamage));
	}

}
