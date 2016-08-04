package maplestory.server.net.handlers.login;

import constants.LoginStatus;
import constants.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class LoginPasswordHandler extends MaplePacketHandler {

	@SneakyThrows
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		String username = readMapleAsciiString(buf);
		String password = readMapleAsciiString(buf);
		
		int result = client.login(username, password);
		
		if(result == 0){
			if(client.getLoginStatus() == LoginStatus.OFFLINE){
				if(!client.getLoginMessage().isEmpty()){
					client.sendPacket(PacketFactory.getServerMessagePacket(MessageType.POPUP, client.getLoginMessage(), 1, false));
					client.setLoginMessage("");	
				}
				client.sendPacket(PacketFactory.getAuthSuccess(client));
				client.setLoggedInStatus(LoginStatus.LOGGED_IN);
			}else{
				client.sendPacket(PacketFactory.getLoginFailed(7));
			}
		}else{
			client.sendPacket(PacketFactory.getLoginFailed(result));
		}
	
		
	}

}
