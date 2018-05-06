package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.server.net.MaplePacketHandler;

public class CancelItemEffectHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int itemId = -buf.readInt();
		
		if(ItemInfoProvider.noCancelMouse(itemId)){
			return;
		}
		
		client.getCharacter().cancelEffect(ItemInfoProvider.getItemEffect(itemId));
	}

}
