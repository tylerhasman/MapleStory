package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.map.MaplePortal;
import maplestory.server.net.MaplePacketHandler;

public class ChangeMapHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		if(buf.readableBytes() == 0){
			if(client.getCharacter().isCashShopOpen()){
				client.getCharacter().closeCashshop();
			}
		}else{
			
			buf.readByte();
			
			int target = buf.readInt();
			String startwp = readMapleAsciiString(buf);
			buf.readByte();
			
			if(target != -1 && !client.getCharacter().isAlive()){
				
				client.getCharacter().reviveAtClosestTown();
				
				/*
				 MapleNPC npc = MapleLifeFactory.getNPC(1061011);
				
				NpcConversationManager cm = new NpcConversationManager(client, npc);
				
				
				 MapleScript script = new MapleScript("scripts/npc/death_npc.js", "scripts/npc/error.js");
				
				if(script.isUsingFallback()){
					client.getCharacter().reviveAtClosestTown();
				}else{
					try {
						Bindings bindings = new SimpleBindings();
						bindings.put("cm", cm);
						
						client.setActiveNpc(script.execute(bindings));
						client.setActiveNpcConversation(cm);
						
						client.getActiveNpc().startNpc();
						
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (ScriptException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}*/
				
			}else{
				MaplePortal portal = client.getCharacter().getMap().getPortal(startwp);
				
				if(portal != null){
					portal.enterPortal(client);
				}else{
					client.sendReallowActions();
				}	
			}
			
		}
		
	}

}
