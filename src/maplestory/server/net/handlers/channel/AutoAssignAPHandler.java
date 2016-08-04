package maplestory.server.net.handlers.channel;

import constants.MapleStat;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;

public class AutoAssignAPHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		MapleCharacter chr = client.getCharacter();
		
		buf.skipBytes(8);
		
		if(chr.getRemainingAp() <= 0){
			return;
		}
		
		int total = 0;
		int extras = 0;
		for(int i = 0; i < 2;i++){
			int type = buf.readInt();
			int amount = buf.readInt();
			if(amount < 0 || amount > chr.getRemainingAp()){
				return;
			}
			total += amount;
			extras += chr.addStat(MapleStat.getByValue(type), amount);
		}
		
		int remainingAp = chr.getRemainingAp() - total + extras;
		
		chr.setRemainingAp(remainingAp);
		client.sendReallowActions();
		
	}

}
