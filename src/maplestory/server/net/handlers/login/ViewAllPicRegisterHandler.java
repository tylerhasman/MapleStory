package maplestory.server.net.handlers.login;

import io.netty.buffer.ByteBuf;
import maplestory.channel.MapleChannel;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.server.MapleServer;
import maplestory.server.net.MaplePacketHandler;

public class ViewAllPicRegisterHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		buf.skipBytes(1);
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
		
		String mac = readMapleAsciiString(buf);
		
		readMapleAsciiString(buf);
		String pic = readMapleAsciiString(buf);
		boolean allowLogin = false;
		if(!client.isPicCreated()){
			client.registerPic(pic);
			allowLogin = true;
		}else{
			client.getLogger().warn("Client tried to create a PIC when it has already registered one");
			allowLogin = client.checkPic(pic);
		}
		
		if(allowLogin){
			channel.initialConnection(client, charId);
		}else{
			client.closeConnection();
		}
	}

}
