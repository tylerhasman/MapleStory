package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.server.net.MaplePacketHandler;
import maplestory.util.Randomizer;

public class MonsterDamageMonsterHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		int attackerId = buf.readInt();
		buf.skipBytes(4);
		int damagedId = buf.readInt();
		
		MapleMonster monster = (MapleMonster) client.getCharacter().getMap().getObject(attackerId);
		MapleMonster damaged = (MapleMonster) client.getCharacter().getMap().getObject(damagedId);
		
		if(monster == null || damaged == null){
			return;
		}
		
		int damage = Randomizer.nextInt(((monster.getMaxHp() / 13 + monster.getStats().getPADamage() * 10)) * 2 + 500) / 10; //Beng's formula.
		
		damaged.damage(monster, damage);
		
	}

}
