package maplestory.server.net.handlers.login;

import constants.LoginStatus;
import constants.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleServer;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.world.World;

public class LoginPasswordHandler extends MaplePacketHandler {

	@SneakyThrows
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		try{
			String username = readMapleAsciiString(buf);
			String password = readMapleAsciiString(buf);
			
			int result = client.login(username, password);
			
			if(result == 0){
				if(client.getLoginStatus() == LoginStatus.IN_GAME){
					
					boolean noCharsFound = true;
					
					for(World world : MapleServer.getWorlds()){
						MapleCharacter chr = world.getPlayerStorage().getByAccountId(client.getId());
						if(chr == null)
							continue;
						noCharsFound = false;
						if(chr.getClient() == null){
							client.setLoggedInStatus(LoginStatus.OFFLINE);
						}else if(!chr.getClient().getConnection().isOpen()){
							client.setLoggedInStatus(LoginStatus.OFFLINE);
						}
					}
					
					if(noCharsFound){
						client.setLoggedInStatus(LoginStatus.OFFLINE);
					}
					
				}
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
		}catch(Exception e){
			e.printStackTrace();
			client.sendPacket(PacketFactory.getLoginFailed(7));
		}
		

	
		
	}

}
