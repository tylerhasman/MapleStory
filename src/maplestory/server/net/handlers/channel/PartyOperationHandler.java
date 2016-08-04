package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.party.MapleParty;
import maplestory.party.PartyOperationType;
import maplestory.party.MapleParty.PartyEntry;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.world.World;

public class PartyOperationHandler extends MaplePacketHandler {

	private static final int 
			CREATE = 1,
			LEAVE = 2,
			JOIN = 3,
			INVITE = 4,
			EXPEL = 5, 
			CHANGE_LEADER = 6;
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int operation = buf.readByte();
		MapleCharacter chr = client.getCharacter();
		World world = chr.getClient().getWorld();
		MapleParty party = chr.getParty();
		
		if(operation == CREATE){
			chr.createParty();
		}else if(operation == LEAVE){
			if(party != null){
				chr.leaveParty();
			}
		}else if(operation == JOIN){
			int partyid = buf.readInt();
			
			MapleParty targetParty = world.getParty(partyid);
			
			if(targetParty == null){
				chr.sendMessage(MessageType.POPUP, "An issue has occured joing party "+partyid);
			}else{
				if(chr.isInvitedToParty(targetParty)){
					chr.joinParty(targetParty);
				}else{
					chr.sendMessage(MessageType.POPUP, "You aren't invited to that party!");
				}
			}
		}else if(operation == INVITE){
			String targetPlayer = readMapleAsciiString(buf);
			MapleCharacter target = world.getPlayerStorage().getByName(targetPlayer);
			
			if(target != null){
				
				if(target.getParty() != null){
					client.sendPacket(PacketFactory.partyStatus(PartyOperationType.ALREADY_IN_PARTY));
				}else{
					
					if(party == null || !party.isFull()){
						if(party == null){
							chr.createParty();
							party = chr.getParty();
						}
						target.inviteToParty(chr);//MapleParty should handle this
					}else{
						chr.sendMessage(MessageType.PINK_TEXT, "Could not invite player because your party is full.");
						//client.sendPacket(PacketFactory.partyStatus(PartyOperationType.FULL));
					}	
				}
				
			}else{
				//client.sendPacket(PacketFactory.partyStatus(PartyOperationType.NOT_FOUND_IN_CHANNEL));
				chr.sendMessage(MessageType.POPUP, "Could not find player "+targetPlayer+".");
			}
		}else if(operation == EXPEL){
			int cid = buf.readInt();
			
			if(party.isLeader(chr)){
				if(party.isMember(cid)){
					PartyEntry entry = party.getEntry(cid);
					party.expel(entry);
				}else{
					chr.sendMessage(MessageType.POPUP, "They aren't in your party");
				}
			}else{
				chr.sendMessage(MessageType.POPUP, "Only the party leader may expel players");
			}
		}else if(operation == CHANGE_LEADER){
			int cid = buf.readInt();
			
			if(party.isMember(cid)){
				
				if(party.isLeader(chr)){
					
					party.changeLeader(party.getEntry(cid));
					
				}else{
					chr.sendMessage(MessageType.POPUP, "You must be the leader of the party to do that");
				}
				
			}else{
				chr.sendMessage(MessageType.POPUP, "They aren't in your party!");
			}
		}
		
	}

}
