package maplestory.server.net.handlers.login;

import java.sql.SQLException;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class CheckCharNameHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		String name = readMapleAsciiString(buf);
		
		try {
			boolean inUse = MapleCharacter.checkNameTaken(name);
			client.sendPacket(PacketFactory.getNameCheckResponse(name, inUse));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
