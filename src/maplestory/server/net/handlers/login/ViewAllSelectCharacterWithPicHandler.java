package maplestory.server.net.handlers.login;

import io.netty.buffer.ByteBuf;
import maplestory.channel.MapleChannel;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.server.MapleServer;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class ViewAllSelectCharacterWithPicHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		String pic = readMapleAsciiString(buf);
		int charId = buf.readInt();
		int worldId = buf.readInt();
		
		MapleCharacterSnapshot snapshot = MapleCharacterSnapshot.createDatabaseSnapshot(charId);
		
		if(snapshot.getWorld() != worldId){
			client.getLogger().warn("Client tried to connect to world "+worldId+" with character "+charId+" which exists in world "+snapshot.getWorld());
			client.closeConnection();
			return;
		}
		
		if(snapshot.getAccount() != client.getId()){
			client.getLogger().warn("Client tried to use a character it doesn't own.");
			client.closeConnection();
			return;
		}
		
		MapleChannel channel = MapleServer.getWorld(worldId).findAvailableChannel();
		
		String macs = readMapleAsciiString(buf);
		
		if(client.checkPic(pic)){
			channel.initialConnection(client, charId);
		}else{
			client.sendPacket(PacketFactory.getWrongPic());
		}
	}

}
