package maplestory.server.net.handlers.channel;

import constants.MapleBuffStat;
import constants.skills.Bishop;
import constants.skills.FPArchMage;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.map.MapleMapObject;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class TakeDamageHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		buf.readInt();
		byte damageFrom = buf.readByte();
		byte element = buf.readByte();
		int damage = buf.readInt();

		MapleCharacter chr = client.getCharacter();
		
		chr.damage(damage);
		
		if(damageFrom == 0) {//Skill
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
		
		/*int objectId = 0, monsterId = 0, pgmr = 0, direction = 0;
		int pos_x = 0, pos_y = 0, fake = 0;
		
		if(damageFrom != -3){
			
			monsterId = buf.readInt();
			oid = buf.readInt();
			
		}else{
			//Handle other types I guess?
		}*/
		
	}

}
