package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.map.MapleMapObject;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class MonsterBombHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		int oid = buf.readInt();
		MapleMapObject obj = client.getCharacter().getMap().getObject(oid);
	
		if(obj instanceof MapleMonster){
			MapleMonster monster = (MapleMonster) obj;
			if(monster.getId() == 8500003 || monster.getId() == 8500004){
				monster.getMap().broadcastPacket(PacketFactory.killMonster(oid, 4));
				client.getCharacter().getMap().removeObject(oid);
			}
		}
	}

}
