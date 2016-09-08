package maplestory.server.net.handlers.channel;

import tools.TimerManager;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.map.MapleMapObject;
import maplestory.map.MapleReactor;
import maplestory.server.net.MaplePacketHandler;

public class DamageReactorHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		int oid = buf.readInt();
		int charPos = buf.readInt();
		short stance = buf.readShort();
		
		int attackType = buf.readInt();
		
		int skillId = buf.readInt();
		
		MapleMapObject obj = client.getCharacter().getMap().getObject(oid);
		
		if(!(obj instanceof MapleReactor)){
			return;
		}
		
		MapleReactor reactor = (MapleReactor) obj;
		
		reactor.hitReactor(client.getCharacter(), stance);
		
	}

}
