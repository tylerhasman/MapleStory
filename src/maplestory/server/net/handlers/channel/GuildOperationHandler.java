package maplestory.server.net.handlers.channel;

import java.util.HashMap;
import java.util.Map;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.guild.GuildEntry;
import maplestory.guild.MapleGuild;
import maplestory.guild.MapleGuild.MapleGuildInviteResponse;
import maplestory.guild.MapleGuildEmblem;
import maplestory.guild.MapleGuildRankLevel;
import maplestory.map.MapleReactor;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleServer;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.world.World;

public class GuildOperationHandler extends MaplePacketHandler {

	@AllArgsConstructor
	static enum GuildOp {
		UNKNOWN(0x00),
		CREATE(0x02),
		INVITE(0x05),
		JOIN(0x06),
		QUIT(0x07),
		EXPEL(0x08),
		CHANGE_RANK_NAMES(0x0D),
		CHANGE_RANK(0x0E),
		CHANGE_EMBLEM(0x0F),
		CHANGE_NOTICE(0x10),
		;
		
		private static final Map<Integer, GuildOp> byId = new HashMap<>();
		
		static{
			for(GuildOp op : values()){
				byId.put(op.id, op);
			}
		}
		
		private final int id;
	}

	private static boolean isGuildNameTaken(World world, String guildName) {
		
		for(MapleGuild guild : world.getGuilds()){
			if(guild.getName().equalsIgnoreCase(guildName)){
				return true;
			}
		}
		
		return false;
	}

	
    private static boolean isGuildNameAcceptable(String name) {
        if (name.length() < 3 || name.length() > 12) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLowerCase(name.charAt(i)) && !Character.isUpperCase(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		MapleCharacter chr = client.getCharacter();
		GuildOp op = GuildOp.byId.get((int)buf.readByte());
		MapleGuild guild = chr.getGuild();
		
		if(op == GuildOp.UNKNOWN){
			client.getLogger().warn("Unknown guild op recieved...");
		}else if(op == GuildOp.CREATE){
			if(guild != null){
				chr.sendMessage(MessageType.POPUP, "You are already in a guild!");
				return;
			}
			if(chr.getMeso() < MapleGuild.CREATE_GUILD_COST){
				chr.sendMessage(MessageType.POPUP, "You do not have enough mesos to create a guild");
				return;
			}
			
			String guildName = readMapleAsciiString(buf);
			
			if(!isGuildNameAcceptable(guildName)){
				chr.sendMessage(MessageType.POPUP, "The guild name you have chosen is not allowed.");
				return;
			}
			
			if(isGuildNameTaken(chr.getClient().getWorld(), guildName)){
				chr.sendMessage(MessageType.POPUP, "The guild name "+guildName+" is unavailable.");
				return;
			}
			
			MapleGuild newGuild = MapleGuild.createGuild(guildName, chr.getWorld());
		
			if(newGuild == null){
				chr.sendMessage(MessageType.POPUP, "Failed to create guild, unknown error.");
			}else{
				chr.giveMesos(-MapleGuild.CREATE_GUILD_COST);
				newGuild.addMember(chr, MapleGuildRankLevel.MASTER);
				chr.sendMessage(MessageType.POPUP, "You have successfully created the guild "+guildName);
				chr.respawnPlayerForOthers();
			}
			
		}else if(op == GuildOp.CHANGE_NOTICE){
			String notice = readMapleAsciiString(buf);
			
			if(guild != null){
				GuildEntry entry = guild.getEntry(chr);
				if(guild.getRankLevel(entry) == MapleGuildRankLevel.MASTER || guild.getRankLevel(entry) == MapleGuildRankLevel.JR_MASTER){
					guild.changeNotice(notice);
				}else{
					chr.sendMessage(MessageType.POPUP, "You may not change the guild notice.");
				}
			}
		}else if(op == GuildOp.CHANGE_RANK_NAMES){
			if(guild != null){
				GuildEntry entry = guild.getEntry(chr);
				if(guild.getRankLevel(entry) == MapleGuildRankLevel.MASTER || guild.getRankLevel(entry) == MapleGuildRankLevel.JR_MASTER){
					Map<MapleGuildRankLevel, String> names = new HashMap<>();
					
					for(int i = 0; i < MapleGuildRankLevel.values().length;i++){
						String name = readMapleAsciiString(buf);
						MapleGuildRankLevel level = MapleGuildRankLevel.getPacketOrder()[i];
						names.put(level, name);
					}
					
					guild.changeRankTitles(names);
					
					
				}else{
					chr.sendMessage(MessageType.POPUP, "You may not change the guild rank names.");
				}
			}
		}else if(op == GuildOp.CHANGE_EMBLEM){
			if(guild != null){
				GuildEntry entry = guild.getEntry(chr);
				if(guild.getRankLevel(entry) == MapleGuildRankLevel.MASTER || guild.getRankLevel(entry) == MapleGuildRankLevel.JR_MASTER){
					if(chr.getMeso() < MapleGuild.CHANGE_EMBLEM_COST){
						chr.sendMessage(MessageType.POPUP, "You do not have enough mesos to change the guild emblem.");
					}else{
						short bg = buf.readShort();
						byte bgColor = buf.readByte();
						short logo = buf.readShort();
						byte logoColor = buf.readByte();
						
						guild.changeEmblem(new MapleGuildEmblem(bg, bgColor, logo, logoColor));
						chr.giveMesos(-MapleGuild.CHANGE_EMBLEM_COST);
						
					}
				}else{
					chr.sendMessage(MessageType.POPUP, "You may not change the guild emblem.");
				}
			}
		}else if(op == GuildOp.INVITE){
			if(guild != null){
				GuildEntry entry = guild.getEntry(chr);
				if(guild.getRankLevel(entry) == MapleGuildRankLevel.MASTER || guild.getRankLevel(entry) == MapleGuildRankLevel.JR_MASTER){
					
					String targetName = readMapleAsciiString(buf);

					guild.invitePlayer(chr, targetName);
					
				}else{
					chr.sendMessage(MessageType.POPUP, "You may not invite players to your guild.");
				}
			}
		}else if(op == GuildOp.JOIN){
			int guildId = buf.readInt();
			buf.skipBytes(4);
			MapleGuild targetGuild = client.getWorld().getGuild(guildId);
			if(chr.getGuild() != null){
				chr.sendMessage(MessageType.POPUP, "You are already in a guild!");
				return;
			}
			if(targetGuild == null){
				chr.sendMessage(MessageType.POPUP, "Could not find guild.");
			}else{
				if(targetGuild.isInvited(chr)){
					targetGuild.addMember(chr, MapleGuildRankLevel.MEMBER_1);
					chr.respawnPlayerForOthers();
				}else{
					chr.sendMessage(MessageType.POPUP, "You aren't invited to that guild.");
				}
			}
		}else if(op == GuildOp.CHANGE_RANK){
			
			int chrId = buf.readInt();
			byte rank = buf.readByte();
			
			if(guild != null){
				GuildEntry entry = guild.getEntry(chr);
				if(guild.getRankLevel(entry) == MapleGuildRankLevel.MASTER || guild.getRankLevel(entry) == MapleGuildRankLevel.JR_MASTER){
					
					if(rank > 5 || rank <= 1){//Master or something unknown...
						return;
					}
					GuildEntry target = guild.getEntry(chrId);
					guild.changeRank(target, MapleGuildRankLevel.getById(-rank + 5));
					
				}else{
					chr.sendMessage(MessageType.POPUP, "You may not change player rank.");
				}
			}
		}else if(op == GuildOp.QUIT){
			if(guild != null){
				guild.removeMember(chr);
				chr.respawnPlayerForOthers();
			}
		}else if(op == GuildOp.EXPEL){
			
			int targetId = buf.readInt();
			//String targetName = readMapleAsciiString(buf);
			//We don't use the name :P
			
			if(targetId == chr.getId()){
				chr.sendMessage(MessageType.POPUP, "You can't expel yourself.");
			}else{
				if(guild != null){
					GuildEntry entry = guild.getEntry(chr);
					if(guild.getRankLevel(entry) == MapleGuildRankLevel.MASTER || guild.getRankLevel(entry) == MapleGuildRankLevel.JR_MASTER){
						GuildEntry targetEntry = guild.getEntry(targetId);
						if(targetEntry != null){
							guild.expel(targetEntry);
						}
					}else{
						chr.sendMessage(MessageType.POPUP, "You may not expel players.");
					}
				}
			}
		}else{
			client.getLogger().warn("Unhandled operation "+op);
		}
		
	}

}
