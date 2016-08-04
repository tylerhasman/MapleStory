package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.server.net.MaplePacketHandler;

public class AutoAggroHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int oid = buf.readInt();
		
		MapleMonster monster = null;
		
		if(client.getCharacter().getMap().getObject(oid) instanceof MapleMonster){
			monster = (MapleMonster) client.getCharacter().getMap().getObject(oid);
			
			if(monster.getController() != null){
				if(!monster.hasAggro()){
					monster.setAggro(true);
					monster.getController().uncontrolMonster(monster);
					client.getCharacter().controlMonster(monster);					
				}
			}
		}
	}

}
