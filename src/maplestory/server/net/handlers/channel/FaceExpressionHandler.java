package maplestory.server.net.handlers.channel;

import constants.MapleEmote;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class FaceExpressionHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		int emoteId = buf.readInt();
		MapleEmote emote = MapleEmote.byId(emoteId);
		
		MapleCharacter chr = client.getCharacter();
		
		if(emote.isCashShop()){
			
			int emoteid = 5159992 + emoteId;

			if(chr.getInventory(emoteid).countById(emoteid) == 0){
				return;//They are using an emote that they don't own!
			}
			
		}
		
		
		chr.emote(emote);
		
	}

}
