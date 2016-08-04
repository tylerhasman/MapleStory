package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleLifeFactory;
import maplestory.life.MapleNPC;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class EnterMTSHandler extends MaplePacketHandler {

	//private static final int FM = 910000000;
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		MapleCharacter chr = client.getCharacter();
		
		/*if(chr.getJob() == MapleJob.BEGINNER || chr.getJob() == MapleJob.NOBLESSE || chr.getJob() == MapleJob.LEGEND){
			chr.sendMessage(MessageType.POPUP, "You must job advance first before you are able to use the Trade button.");
			client.sendReallowActions();
			return;
		}*/
		
		MapleNPC npc = MapleLifeFactory.getNPC(2080005);
		
		chr.openNpc(npc);
		
		client.sendReallowActions();
		
		/*if(client.getCharacter().getMapId() == FM){
			chr.sendMessage(MessageType.POPUP, "You are already in the FM");
			client.sendReallowActions();
		}else{
			chr.setFmReturnMap(chr.getMapId());
			chr.changeMap(client.getChannel().getMapFactory().getMap(FM));
		}*/
		
		
	}

}
