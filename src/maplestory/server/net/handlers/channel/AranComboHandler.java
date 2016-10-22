package maplestory.server.net.handlers.channel;

import constants.GameConstants;
import constants.skills.Aran;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class AranComboHandler extends MaplePacketHandler {

	private long lastCombo;
	
	public AranComboHandler() {
		lastCombo = -1;
	}
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		MapleCharacter chr = client.getCharacter();
		
		int level = chr.getSkillLevel(Aran.COMBO_ABILITY);
		
		if(GameConstants.isAran(chr.getJob().getId())){
			
			long currentTime = System.currentTimeMillis();
			int combo = chr.getCombo();
			
			if(currentTime - lastCombo > 3000 && combo > 0){
				combo = 0;
			}
			
			combo++;
			
			chr.setCombo(combo);
			lastCombo = currentTime;
		}
		
		
		
	}

}
