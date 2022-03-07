package maplestory.guild;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import constants.MessageType;
import database.BatchedScript;
import database.ExecuteResult;
import database.MapleDatabase;
import database.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.channel.MapleChannel;
import maplestory.channel.MapleSocketChannel;
import maplestory.guild.bbs.BulletinPost;
import maplestory.guild.bbs.BulletinReply;
import maplestory.guild.bbs.GuildBulletin;
import maplestory.guild.bbs.MapleGuildBulletin;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.server.MapleServer;
import maplestory.server.net.PacketFactory;
import maplestory.world.World;

public class MapleGuild {

	public final static int CREATE_GUILD_COST = 1500000;
	public final static int CHANGE_EMBLEM_COST = 5000000;
	
	
	@Getter
	private final int guildId;
	@Getter
	private String name;
	@Getter
	private MapleGuildEmblem emblem;
	
	private List<GuildEntry> members;
	
	private Map<MapleGuildRankLevel, MapleGuildRank> guildRanks;
	private Map<Integer, MapleGuildRankLevel> memberRanks;
	
	@Getter
	private int capacity;
	
	@Getter
	private String notice;
	
	@Getter
	private int guildPoints;
	
	private List<Integer> invitations;
	
	@Getter
	private final long creationTime;
	
	@Getter
	private final int worldId;
	
	private GuildBulletin bulletin;
	
	MapleGuild(int id, int worldId, String name, MapleGuildEmblem emblem, long creationTime, int capacity, String notice) {
		this.guildId = id;
		members = new ArrayList<>();
		guildRanks = new HashMap<>();
		memberRanks = new HashMap<>();
		this.capacity = capacity;
		this.notice = notice;
		this.name = name;
		this.emblem = emblem;
		this.creationTime = creationTime;
		this.worldId = worldId;
		invitations = new ArrayList<>();
	}
	
	public GuildBulletin getBulletin() {
		return bulletin;
	}
	
	public World getWorld(){
		return MapleServer.getWorld(worldId);
	}
	
	public void increaseCapacity(){
		throw new RuntimeException("Not yet implemented");
	}
	
	public void changeNotice(String notice){
		this.notice = notice;
		broadcastPacket(PacketFactory.guildUpdateNotice(this));
	}

	public void changeEmblem(MapleGuildEmblem emblem) {
		this.emblem = emblem;
		broadcastPacket(PacketFactory.guildUpdateEmblem(this));
		forEachOnlineMember(mc -> mc.respawnPlayerForOthers(), -1);
		saveGuildData();//We will save this one because it costs lots of mesos to do it
	}
	
	public MapleGuildRank getRank(MapleGuildRankLevel level){
		return guildRanks.get(level);
	}
	
	public void changeRankTitles(Map<MapleGuildRankLevel, String> names){
		for(MapleGuildRankLevel level : names.keySet()){
			guildRanks.get(level).setName(names.get(level));
		}
		broadcastPacket(PacketFactory.guildUpdateRankTitle(this));
	}

	public void changeRank(GuildEntry target, MapleGuildRankLevel level) {
		if(isMember(target)){
			memberRanks.put(target.getSnapshot().getId(), level);
			broadcastPacket(PacketFactory.guildChangeRank(target, level));
		}else{
			throw new IllegalArgumentException("Target isn't in this guild!");
		}
	}
	
	public void updateAllMembers(){
		for(GuildEntry entry : getMembers()){
			updateMember(entry);
		}
	}
	
	public void updateGuild(){
		forEachOnlineMember(chr -> {
			chr.getClient().sendPacket(PacketFactory.guildUpdateInfo(chr, this));
		}, -1);
	}
	
	public void updateMember(GuildEntry entry){
		broadcastPacket(PacketFactory.guildUpdateMember(entry));
	}

	public void invitePlayer(MapleCharacter inviter, String name){
		if(isMember(inviter)){
			
			MapleChannel channel = inviter.getClient().getChannel();
			
			MapleCharacter chr = channel.getPlayerByName(name);
			
			if(chr == null){
				inviter.getClient().sendPacket(PacketFactory.guildInviteError(MapleGuildInviteResponse.NOT_IN_CHANNEL));
			}else if(chr.getGuild() != null){
				inviter.getClient().sendPacket(PacketFactory.guildInviteError(MapleGuildInviteResponse.ALREADY_IN_GUILD));
			}else{
				invitations.add(chr.getId());
				chr.getClient().sendPacket(PacketFactory.guildInvite(this, inviter));
			}
			
		}

	}
	
	public void broadcastPacket(byte[] packet){
		broadcastPacket(packet, -1);
	}
	
	public void broadcastPacket(byte[] packet, int except){
		forEachOnlineMember(mc -> mc.getClient().sendPacket(packet), except);
	}
	
	protected void forEachOnlineMember(Consumer<MapleCharacter> func, int except){
		for(GuildEntry entry : getMembers()){
			if(entry.getSnapshot().getId() == except){
				continue;
			}
			
			if(entry.getSnapshot().isOnline()){
				func.accept(entry.getSnapshot().getLiveCharacter().get());
			}
		}
	}
	
	public List<GuildEntry> getMembers(){
		return Collections.unmodifiableList(members);
	}

	public boolean isMember(MapleCharacter chr) {
		return getEntry(chr) != null;
	}
	
	public boolean isMember(GuildEntry entry){
		return getEntry(entry.getSnapshot().getId()) != null;
	}
	
	public GuildEntry getEntry(MapleCharacter chr){
		return getEntry(chr.getId());
	}
	
	public GuildEntry getEntry(int chrId) {
		for(GuildEntry entry : getMembers()){
			if(entry.getSnapshot().getId() == chrId){
				return entry;
			}
		}
		return null;
	}

	
	public MapleGuildRank getRank(GuildEntry member){
		return getRank(getRankLevel(member));
	}

	public MapleGuildRankLevel getRankLevel(GuildEntry member) {
		if(member.getGuildId() != guildId){
			throw new IllegalArgumentException("Guild entry doesn't match guild. Expected "+guildId+" got "+member.getGuildId());
		}
		return memberRanks.get(member.getSnapshot().getId());
	}
	
	public MapleGuildRankLevel getRankLevel(MapleCharacter chr){
		return getRankLevel(getEntry(chr));
	}

	public void addMember(MapleCharacter chr, MapleGuildRankLevel level) {
		if(isMember(chr)){
			throw new RuntimeException(chr.getName()+" is already in the guild "+getName());
		}
		members.add(new GuildEntry(guildId, chr));
		memberRanks.put(chr.getId(), level);
		forEachOnlineMember(mc -> mc.getClient().sendPacket(PacketFactory.guildUpdateInfo(mc, this)), chr.getId());
		chr.joinGuild(this);
		saveEntries();
	}
	
	public void removeMember(MapleCharacter chr){
		GuildEntry entry = getEntry(chr);
		if(entry != null){
			removeMember(entry, false);
		}
	}
	
	private void removeMember(GuildEntry entry, boolean expelled) {
		boolean removed = members.removeIf(ge -> ge.getSnapshot().getId() == entry.getSnapshot().getId());
		if(removed){
			memberRanks.remove(entry.getSnapshot().getId());
			broadcastPacket(PacketFactory.guildMemberLeft(entry, expelled));
			if(entry.isOnline()){
				entry.getSnapshot().getLiveCharacter().get().leaveGuild();
			}
			saveEntries();
		}
	}

	public boolean isInvited(MapleCharacter chr) {
		return invitations.contains(chr.getId());
	}
	
	public void disbandGuild(){
		for(GuildEntry entry : new ArrayList<>(getMembers())){
			removeMember(entry, false);
			forEachOnlineMember(mc -> mc.sendMessage(MessageType.POPUP, "Your guild has disbanded"), -1);
		}
		try {
			MapleDatabase.getInstance().execute("DELETE FROM `guild_entries` WHERE `guild`=?", guildId);
			MapleDatabase.getInstance().execute("DELETE FROM `guilds` WHERE `id`=?", guildId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		getWorld().unregisterGuild(this);
	}

	protected void saveEntries(){
		
		if(members.size() == 0){
			return;
		}
		
		try {
			MapleDatabase.getInstance().execute("DELETE FROM `guild_entries` WHERE `guild`=?", guildId);
			
			String script = "INSERT INTO `guild_entries` (`character_id`,`guild`,`rank`) VALUES ";
			
			Object[] args = new Object[members.size() * 3];
			
			int i = 0;
			
			for(GuildEntry entry : members){
				
				int chrId = entry.getSnapshot().getId();
				int rank = getRankLevel(entry).getId();
				
				args[i++] = chrId;
				args[i++] = guildId;
				args[i++] = rank;
				
				script += "(?, ?, ?),";
				
			}
			
			script = script.substring(0, script.length() - 1);
			script += ";";
			
			MapleDatabase.getInstance().execute(script, args);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void saveGuildData(){
		
		int bg = emblem.getBackground();
		int bgColor = emblem.getBackgroundColor();
		int logo = emblem.getLogo();
		int logoColor = emblem.getLogoColor();
		
		String script = "UPDATE `guilds` SET `notice`=?,`capacity`=?, `emblem_background`=?,`emblem_background_color`=?,`emblem_logo`=?,`emblem_logo_color`=?,`points`=?, ";
		
		String[] rankNames = new String[MapleGuildRankLevel.values().length];
		
		for(int i = 0; i < MapleGuildRankLevel.getPacketOrder().length;i++){
			MapleGuildRankLevel level = MapleGuildRankLevel.getPacketOrder()[i];
			rankNames[i] = getRank(level).getName();
			script += "`"+level.getDatabaseName()+"`=?,";
		}
		
		script = script.substring(0, script.length()-1);
		script += " WHERE `id`=?";
		
		try {
			MapleDatabase.getInstance().execute(script, notice, capacity, bg, bgColor, logo, logoColor, guildPoints, 
					rankNames[0],
					rankNames[1],
					rankNames[2],
					rankNames[3],
					rankNames[4],
					getGuildId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void saveGuild(){
		saveGuildData();
		saveEntries();
		saveBulletin();
		
		
	}

	private void saveBulletin() {
		
		try {
			MapleDatabase.getInstance().execute("DELETE FROM `guild_bbs` WHERE `guild`=?", guildId);
			MapleDatabase.getInstance().execute("DELETE FROM `guild_bbs_replies` WHERE `guild`=?", guildId);
			
			String scriptNotice = "INSERT INTO `guild_bbs` (`guild`,`post_id`,`title`,`content`,`poster`,`post_time`,`emote`,`notice`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			
			BatchedScript script = new BatchedScript("INSERT INTO `guild_bbs` (`guild`,`post_id`,`title`,`content`,`poster`,`post_time`,`emote`) VALUES (?, ?, ?, ?, ?, ?, ?)");
			BatchedScript replyScript = new BatchedScript("INSERT INTO `guild_bbs_replies` (`guild`, `post`, `author`, `post_time`, `content`) VALUES (?, ?, ?, ?, ?)");
			
			for(BulletinPost post : bulletin.getPosts()){
				
				script.addBatch(guildId, post.getPostId(), post.getSubject(), post.getContent(), post.getAuthor().getId(), post.getPostTime(), post.getEmote().getId());
				for(BulletinReply reply : post.getReplies()){
					
					replyScript.addBatch(guildId, post.getPostId(), reply.getAuthor(), reply.getPostTime(), reply.getContent());
					
				}
				
			}
			
			MapleDatabase.getInstance().execute(script, false);
			
			if(bulletin.getNotice() != null){
				BulletinPost notice = bulletin.getNotice();
				MapleDatabase.getInstance().execute(scriptNotice, guildId, 0, notice.getSubject(), notice.getContent(), notice.getAuthor().getId(), notice.getPostTime(), notice.getEmote().getId(), true);
				for(BulletinReply reply : notice.getReplies()){
					
					replyScript.addBatch(guildId, notice.getPostId(), reply.getAuthor(), reply.getPostTime(), reply.getContent());
					
				}
			}
			

			MapleDatabase.getInstance().execute(replyScript, false);
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public void expel(GuildEntry targetEntry) {
		removeMember(targetEntry, true);
		targetEntry.getSnapshot().getLiveCharacter().ifPresent(chr -> chr.sendNote(getName(), "You have been expelled from "+getName(), 0));
	}
	
	protected void loadEntries(){
		members.clear();
		memberRanks.clear();
		
		try {
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `character_id`,`rank` FROM `guild_entries` WHERE `guild`=?", guildId);
		
			for(QueryResult result : results){
				
				int chrId = result.get("character_id");
				int rank = result.get("rank");
				
				memberRanks.put(chrId, MapleGuildRankLevel.getById(rank));
				
				MapleCharacterSnapshot snapshot = MapleCharacterSnapshot.createDatabaseSnapshot(chrId);
				
				Optional<MapleCharacter> chr = snapshot.getLiveCharacter();
				
				if(chr.isPresent()){
					members.add(new GuildEntry(guildId, chr.get()));
				}else{
					members.add(new GuildEntry(guildId, snapshot));
				}
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static MapleGuild loadFromDatabase(int guildId) throws GuildNotFoundException{
		
		MapleGuild guild = null;
		
		try {
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT * FROM `guilds` WHERE `id`=?", guildId);
			
			if(results.size() > 0){
				
				QueryResult result = results.get(0);
				
				String name = result.get("name");
				String notice = result.get("notice");
				int capacity = result.get("capacity");
				MapleGuildEmblem emblem = new MapleGuildEmblem(result);
				long creationTime = result.get("creation_time");
				int points = result.get("points");
				int world = result.get("world");
				Map<MapleGuildRankLevel, MapleGuildRank> guildRanks = new HashMap<>();
				
				for(MapleGuildRankLevel level : MapleGuildRankLevel.getPacketOrder()){
					String rankName = result.get(level.getDatabaseName());
					guildRanks.put(level, new MapleGuildRank(rankName));
				}
				
				guild = new MapleGuild(guildId, world, name, emblem, creationTime, capacity, notice);
				guild.guildRanks = guildRanks;
				guild.guildPoints = points;
				
				guild.bulletin = MapleGuildBulletin.loadBulletin(guild);
				
				guild.loadEntries();
				
			}else{
				throw new GuildNotFoundException(guildId);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		
		return guild;
	}
	
	public static MapleGuild createGuild(String name, World world) {
		
		MapleGuild guild = null;
		
		long creationTime = System.currentTimeMillis();
		
		String notice = "Welcome to "+name+"!";
		
		try {
			ExecuteResult result = MapleDatabase.getInstance().executeWithKeys("INSERT INTO `guilds` (`name`,`notice`,`creation_time`,`world`) VALUES (?, ?, ?,?)", true, name, notice, creationTime, world.getId());
			
			int guildId = result.getGeneratedKeys().get(0);
			guild = loadFromDatabase(guildId);
			guild.getWorld().registerGuild(guild);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (GuildNotFoundException e) {
			e.printStackTrace();
		}
		
		return guild;
	}

	@AllArgsConstructor
	public static enum MapleGuildInviteResponse {
		NOT_IN_CHANNEL(0x2A),
		ALREADY_IN_GUILD(0x28);
		
		private final int code;
		
		public byte getCode() {
			return (byte) code;
		}
	}
	
}
