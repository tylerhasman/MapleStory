package maplestory.server.net.handlers.channel;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.guild.MapleGuild;
import maplestory.guild.MapleGuildRankLevel;
import maplestory.guild.bbs.BulletinEmote;
import maplestory.guild.bbs.BulletinPost;
import maplestory.guild.bbs.BulletinReply;
import maplestory.guild.bbs.GuildBulletin;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.util.Hex;

public class GuildBBSHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		byte operation = buf.readByte();
	
		MapleGuild guild = client.getCharacter().getGuild();
		
		if(guild == null){
			client.sendReallowActions();
			return;
		}
		
		GuildBulletin bulletin = guild.getBulletin();
		
		if(operation == 0){
			
			boolean edit = buf.readBoolean();
			int postId = -1;
			if(edit){
				postId = buf.readInt();
			}
			boolean notice = buf.readBoolean();
			String title = readMapleAsciiString(buf);
			String text = readMapleAsciiString(buf);
			
			int icon = buf.readInt();
			
			BulletinEmote emote = BulletinEmote.getById(icon);

			if(emote.isPremium()){
				if(client.getCharacter().getItemQuantity(5290000 + icon - 0x64, false) == 0){
					emote = BulletinEmote.SMILE;
				}
			}
			
			
			if(edit){
				bulletin.editPost(postId, title, text, emote, client.getCharacter());
			}else if(!notice){
				bulletin.addPost(title, text, emote, client.getCharacter());
			}else{
				bulletin.setNotice(title, text, emote, client.getCharacter());
			}
			
		}else if(operation == 1){
			
			int postId = buf.readInt();
			
			bulletin.deletePost(postId);
			
		}else if(operation == 2){//Open BBS
			@SuppressWarnings(value="unused")
			int page = buf.readInt();
			
			client.getCharacter().sendGuildBulletin(bulletin);
		}else if(operation == 3){
			
			int postId = buf.readInt();
			
			BulletinPost post = bulletin.getPost(postId);
			
			if(postId == 0){
				post = bulletin.getNotice();
			}
			
			if(post != null){
				client.sendPacket(PacketFactory.guildBBSThread(post));
			}
		}else if(operation == 4){
			
			int postId = buf.readInt();
			
			String str = readMapleAsciiString(buf);
			
			BulletinPost post = bulletin.getPost(postId);
			
			if(postId == 0){
				post = bulletin.getNotice();
			}
			
			post.addReply(client.getCharacter(), str);
			
			if(post != null){
				client.sendPacket(PacketFactory.guildBBSThread(post));
			}
		}else if(operation == 5){
			
			int postId = buf.readInt();
			int replyId = buf.readInt();
			
			BulletinPost post = bulletin.getPost(postId);
			
			BulletinReply reply = post.findReply(replyId);
			
			if(post != null){
				if(reply != null){
					MapleGuildRankLevel level = guild.getRankLevel(client.getCharacter());
					if(reply.getAuthor() == client.getCharacter().getId() || level == MapleGuildRankLevel.JR_MASTER || level == MapleGuildRankLevel.MASTER){
						post.removeReply(replyId);
						client.sendPacket(PacketFactory.guildBBSThread(post));
					}else{
						client.getCharacter().sendMessage(MessageType.POPUP, "Only a Jr. Master or Master may delete that post");
					}
				}
			}
			
			
		}else{
			client.getLogger().warn("GuildBBSHandler Unhandled op code "+operation);
		}
		
	}

}
