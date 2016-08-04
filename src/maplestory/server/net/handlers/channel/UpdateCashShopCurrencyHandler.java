package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.cashshop.CashShopWallet;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class UpdateCashShopCurrencyHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		if(client.getCharacter().isCashShopOpen()){
			client.sendPacket(PacketFactory.updateCashshopCash(CashShopWallet.getWallet(client.getCharacter())));
		}
	}

}
