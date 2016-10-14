package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.client.MapleMessenger;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.util.Hex;

public class MessengerHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		byte mode = buf.readByte();
		
		MapleCharacter chr = client.getCharacter();
		MapleMessenger messenger = chr.getMessenger();
		
		if(mode == 0x00){
			if(messenger != null){
				chr.sendMessage(MessageType.POPUP, "You are already in a chat room.");
				return;
			}
			int id = buf.readInt();
			if(id == 0){
				chr.openMessenger();
			}else{
				MapleMessenger joining = client.getWorld().getMessenger(id);
				
				if(joining != null){
					
					if(joining.isInvited(chr)){
						
						if(joining.isFull()){
							chr.sendMessage(MessageType.POPUP, "That chat room is full!");
							return;
						}
						
						chr.joinMessenger(joining);
						
					}else{
						chr.sendMessage(MessageType.POPUP, "You are not invited to that chat room.");
					}
					
				}else{
					chr.sendMessage(MessageType.POPUP, "The chat room you are trying to join no longer exists.");
				}
			}
			
		}else if(mode == 0x02){
			chr.closeMessenger();
		}else if(mode == 0x03){
			String invitedName = readMapleAsciiString(buf);
			
			MapleCharacter invited = client.getWorld().getPlayerStorage().getByName(invitedName);
			
			if(invited != null){
				
				if(invited.getMessenger() != null){
					client.sendPacket(PacketFactory.messengerNote(invited.getName(), 5, 1));
				
				}else{
					messenger.invite(invited, chr);
					chr.getClient().sendPacket(PacketFactory.messengerNote(invited.getName(), 4, 1));
				}
				
			}else{
				client.sendPacket(PacketFactory.messengerNote(invitedName, 4, 0));
			}
			
		}else if(mode == 0x05){
			String inviterName = readMapleAsciiString(buf);
			
			MapleCharacter inviter = client.getWorld().getPlayerStorage().getByName(inviterName);
			
			if(inviter != null){
				if(inviter.getMessenger() != null){
					inviter.getClient().sendPacket(PacketFactory.messengerNote(chr.getName(), 5, 0));
				}
			}
		}else if(mode == 0x06){
			String msg = readMapleAsciiString(buf);
			messenger.broadcastMessage(chr, msg);
		}
		
		
	}

}
