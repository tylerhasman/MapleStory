package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleNPC;
import maplestory.map.MapleMapObject;
import maplestory.server.net.MaplePacketHandler;
import maplestory.shop.MapleShop;

public class NpcTalkHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		if(client.getCharacter().getActiveNpc() != null){
			client.sendReallowActions();
			return;
		}
		
		int oid  = buf.readInt();
		
		MapleMapObject obj = client.getCharacter().getMap().getObject(oid);
		
		if(obj instanceof MapleNPC){
			
			MapleNPC npc = (MapleNPC) obj;
			
			if(MapleShop.getShopId(npc.getId()) >= 0){
				MapleShop shop = MapleShop.getShop(MapleShop.getShopId(npc.getId()));
				
				client.getCharacter().openShop(shop, npc.getId());
			}else{
				client.getCharacter().openNpc(npc);
			}
			
		}
		
	}

}
