package maplestory.server.net.handlers.login;

import java.sql.SQLException;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class DeleteCharHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		String pic = readMapleAsciiString(buf);
        int cid = buf.readInt();
        if (client.checkPic(pic)) {
        	try {
				if(client.deleteCharacter(cid)){
					client.sendPacket(PacketFactory.getDeleteCharacterResponse(cid, 0));
				}else{
					client.sendPacket(PacketFactory.getDeleteCharacterResponse(cid, 0x14));//Failed for some reason... cheating?
				}
			} catch (SQLException e) {
				e.printStackTrace();
				client.closeConnection();
			}
        } else {
            client.sendPacket(PacketFactory.getDeleteCharacterResponse(cid, 0x14));
        }
	}

}
