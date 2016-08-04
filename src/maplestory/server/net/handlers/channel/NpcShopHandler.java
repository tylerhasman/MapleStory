package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;

public class NpcShopHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		ShopAction action = ShopAction.getAction(buf.readByte());
		
		if(client.getCharacter().getOpenShop() == null){
			return;
		}
		
		if(action == ShopAction.BUY){
			short slot = buf.readShort();
			int itemId = buf.readInt();
			short quantity = buf.readShort();
			if(quantity < 1){
				client.closeConnection();
			}else{
				client.getCharacter().getOpenShop().buy(client.getCharacter(), slot, itemId, quantity);
			}
		}else if(action == ShopAction.SELL){
			
			short slot = buf.readShort();
			int itemId = buf.readInt();
			short quantity = buf.readShort();
			
			if(quantity <= 0){
				client.closeConnection();
				return;
			}
			
			client.getCharacter().getOpenShop().sell(client.getCharacter(), slot, itemId, quantity);
			
		}else if(action == ShopAction.CLOSE){
			client.getCharacter().closeShop();
		}else if(action == ShopAction.RECHARGE){
			
			short slot = buf.readShort();
			
			client.getCharacter().getOpenShop().recharge(client.getCharacter(), slot);
			
		}
		
	}
	
	
	@AllArgsConstructor
	public static enum ShopAction {
		
		BUY(0),
		SELL(1),
		RECHARGE(2),
		CLOSE(3)
		;
		
		@Getter
		private final int id;
		
		
		public static ShopAction getAction(int id){
			for(ShopAction a : values()){
				if(a.id == id){
					return a;
				}
			}
			return null;
		}
	}

}
