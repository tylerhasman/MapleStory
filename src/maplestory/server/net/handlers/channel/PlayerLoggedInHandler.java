package maplestory.server.net.handlers.channel;

import java.sql.SQLException;

import constants.LoginStatus;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.map.MaplePortal;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class PlayerLoggedInHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int cid = buf.readInt();
		
		MapleCharacter chr = client.getWorld().getPlayerStorage().getById(cid);
		
		if(chr == null){
			try {
				chr = MapleCharacter.loadFromDb(cid, client);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			chr.newClient(client);
		}
		
		if(chr == null){
			client.closeConnection();
			return;
		}
		
		client.setCharacter(chr);
		client.setId(chr.getAccountId());
		client.setLoggedInStatus(LoginStatus.IN_GAME);
		
		client.getCharacter().getDamageNumberGenerator().newSeeds();
		
		client.sendPacket(PacketFactory.getCharInfo(chr));
	
		chr.sendKeybindings();
		
		MaplePortal portal = chr.getInitialSpawnpoint();
		
		if(portal != null){
			chr.setPosition(portal.getPosition());
		}
        
		chr.getClient().getWorld().getPlayerStorage().addPlayer(chr);
		
		chr.startCooldownTimers();
		
		if(chr.getMap() == null){
			chr.setMapId(100000000);//Fallback to henesys
		}
		
		chr.getMap().addPlayer(chr);
		
		if(chr.getGuild() != null){
			chr.getGuild().updateGuild();
		}
		
		if(chr.getParty() != null){
			chr.getParty().updateMember(chr);
		}
		
		chr.updateNotes();
		
		chr.updateBuddyList();
		
	}

}
