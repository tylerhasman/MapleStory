package maplestory.server.net.handlers.login;

import java.net.InetAddress;

import constants.ServerConstants;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;

public class RegisterPicHandler extends MaplePacketHandler {

	@SneakyThrows
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.readByte();
		int charId = buf.readInt();
		String macs = readMapleAsciiString(buf);
		
		readMapleAsciiString(buf);
		
		String pic = readMapleAsciiString(buf);
		
		if(client.isPicCreated()){
			client.closeConnection();
		}else{
			client.registerPic(pic);
			InetAddress ip = InetAddress.getByName(ServerConstants.CHANNEL_SERVER_IP);
			int port = client.getChannel().getPort();
			client.sendChannelAddress(ip, port, charId);
		}

	}

}
