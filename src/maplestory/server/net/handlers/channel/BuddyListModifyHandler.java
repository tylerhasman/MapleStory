package maplestory.server.net.handlers.channel;

import java.sql.SQLException;
import java.util.List;

import constants.MessageType;
import database.MapleDatabase;
import database.QueryResult;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.BuddyList;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.util.Hex;

public class BuddyListModifyHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		
		byte op = buf.readByte();
		
		BuddyList buddies = client.getCharacter().getBuddyList();
		
		if(op == 0x1) {//Add buddy
			
			String name = readMapleAsciiString(buf);
			String group = readMapleAsciiString(buf);
			
			if(buddies.isBuddy(name)) {
				if(buddies.getGroup(name).equals(group)) {
					client.getCharacter().sendMessage(MessageType.POPUP, "\""+name+"\" is already your buddy");	
				}else {
					buddies.changeGroup(name, group);
					client.getCharacter().updateBuddyList();
				}
			}else if(buddies.isFull()) {
				client.getCharacter().sendMessage(MessageType.POPUP, "Your buddy list is already full");
			}else {
				
				MapleCharacterSnapshot potentialFriend = null;
				
				MapleCharacter target = client.getCharacter().getWorld().getPlayerStorage().getByName(name);
				
				if(target != null) {
					potentialFriend = target.createSnapshot();
				}else{
					potentialFriend = MapleCharacterSnapshot.createDatabaseSnapshot(name);
				}
				
				if(potentialFriend.getId() == -1) {//Not found
					client.getCharacter().sendMessage(MessageType.POPUP, "A player with the name \""+name+"\" could not be found");
				}else {
					
					if(target != null) {
						target.sendBuddyRequest(client.getCharacter());
					}
					
					client.getCharacter().sendMessage(MessageType.POPUP, "Buddy request sent to \""+name+"\"");
					addBuddyRequestIfNotExist(potentialFriend, client.getCharacter());
				}
				
			}
			
		}else if(op == 0x02){//Accept
			
			int cid = buf.readInt();
			
			if(buddies.hasBuddyRequestFrom(cid)) {
				
				buddies.deleteRequest(cid);
				
				buddies.addBuddy(cid, "Default Group");
				
				MapleCharacter thatPerson = client.getWorld().getPlayerStorage().getById(cid);
				
				if(thatPerson != null) {
					thatPerson.getBuddyList().addBuddy(client.getCharacter().getId(), "Default Group");
					thatPerson.updateBuddyList();
				}
				
				client.getCharacter().updateBuddyList();
				
				MapleDatabase.getInstance().execute("UPDATE `buddy_requests` SET `accepted`=true WHERE `from`=? AND `to`=?", cid, client.getCharacter().getId());
				
				MapleCharacterSnapshot next = client.getCharacter().getBuddyList().nextRequest();
				
				if(next != null) {
					client.sendPacket(PacketFactory.buddyListRequest(next.getId(), client.getCharacter().getId(), next.getName(), "Default Group"));
				}
				
			}else {
				client.getCharacter().sendMessage(MessageType.POPUP, "That request has expired");
				client.getCharacter().updateBuddyList();
				
			}
		}else if(op == 0x02){//Decline
			
			int cid = buf.readInt();
			
			MapleDatabase.getInstance().execute("DELETE FROM `buddy_requests` WHERE `from`=? AND `to`=?", cid, client.getCharacter().getId());
			
			MapleCharacterSnapshot next = client.getCharacter().getBuddyList().nextRequest();
			
			if(next != null) {
				client.sendPacket(PacketFactory.buddyListRequest(next.getId(), client.getCharacter().getId(), next.getName(), "Default Group"));
			}
			
		}else {
			byte[] remaining = new byte[buf.readableBytes()];
			buf.readBytes(remaining);
			
			System.out.println("BUDDY "+op+" : "+Hex.toHex(remaining));
		}
		
	}
	
	private static void addBuddyRequestIfNotExist(MapleCharacterSnapshot receiver, MapleCharacter sender) {
		
		try {
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT * FROM `buddy_requests` WHERE `from`=? AND `to`=?", sender.getId(), receiver.getId());
		
			if(results.size() == 0) {
				
				MapleDatabase.getInstance().execute("INSERT INTO `buddy_requests` (`from`, `to`) VALUES (?, ?)", sender.getId(), receiver.getId());
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
