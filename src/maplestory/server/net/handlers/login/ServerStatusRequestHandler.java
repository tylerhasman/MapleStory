package maplestory.server.net.handlers.login;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.MapleServer;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.world.World;

public class ServerStatusRequestHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		byte worldId = (byte) buf.readShort();
		
		World world = MapleServer.getWorld(worldId);
		
		float percent = (float) world.getPlayerCount() / (float) world.getMaxPlayers();
		
		byte status;
		
		if(percent < 0.8){
			status = 0;
		}else if(percent >= 0.8 && percent < 1){
			status = 1;
		}else{
			status = 2;
		}
		
		client.sendPacket(PacketFactory.getServerStatus(status));
		
	}

}
