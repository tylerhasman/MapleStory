package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleLifeFactory;
import maplestory.player.MapleJob;
import maplestory.script.MapleScript;
import maplestory.server.net.MaplePacketHandler;

public class ClickGuideHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		MapleScript guide = new MapleScript("scripts/npc/guide.js", "scripts/npc/fallback.js");
		
		int npcId = client.getCharacter().getJob() == MapleJob.NOBLESSE ? 1101008 : 1202000;
		
		client.getCharacter().openNpc(guide, MapleLifeFactory.getNPC(npcId));
	}

}
