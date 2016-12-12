package maplestory.guild.bbs;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import database.MapleDatabase;
import database.QueryResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import maplestory.guild.MapleGuild;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.server.MapleServer;

public class MapleGuildBulletin implements GuildBulletin {

	private int nextPost;
	
	private int owner, world;
	
	private Map<Integer, BulletinPost> posts;
	
	private BulletinPost notice;
	
	public MapleGuildBulletin() {
		nextPost = 0;
	}
	
	@Override
	public MapleGuild getOwner() {
		return MapleServer.getWorld(world).getGuild(owner);
	}

	@Override
	public Collection<BulletinPost> getPosts() {
		return Collections.unmodifiableCollection(posts.values());
	}

	@Override
	public BulletinPost getNotice() {
		return notice;
	}
	
	@Override
	public void deletePost(int postId) {
		if(posts.remove(postId) == null){
			if(notice != null && notice.getPostId() == postId){
				notice = null;
			}
		}
	}
	
	@Override
	public void editPost(int postId, String title, String text, int icon, MapleCharacter character) {
		BulletinPost old = getPost(postId);
		
		if(old != null){
			
			MapleBulletinPost newPost = new MapleBulletinPost(character.getId(), old.getPostId(), old.getPostTime(), text, title);
			
			posts.put(old.getPostId(), newPost);
			
		}
		
	}
	
	@Override
	public BulletinPost getPost(int postId) {
		if(notice != null && postId == notice.getPostId()){
			return notice;
		}
		return posts.get(postId);
	}
	
	@Override
	public void addPost(String title, String text, BulletinEmote emote, MapleCharacter poster) {
		MapleBulletinPost post = new MapleBulletinPost(poster.getId(), nextPost++, System.currentTimeMillis(), text, title);
		
		posts.put(post.postId, post);
	}
	
	@Override
	public void setNotice(String title, String text, BulletinEmote emote, MapleCharacter poster) {
		notice = new MapleBulletinPost(poster.getId(), nextPost++, System.currentTimeMillis(), text, title);
	}
	
	@AllArgsConstructor(access=AccessLevel.PROTECTED)
	static class MapleBulletinPost implements BulletinPost {

		private int characterId;
		
		private int postId;
		
		private long postTime;
		
		private String content, subject;
		
		@Override
		public MapleCharacterSnapshot getAuthor() {
			return MapleCharacterSnapshot.createDatabaseSnapshot(characterId);
		}

		@Override
		public int getPostId() {
			return postId;
		}

		@Override
		public long getPostTime() {
			return postTime;
		}

		@Override
		public String getContent() {
			return content;
		}

		@Override
		public String getSubject() {
			return subject;
		}

		@Override
		public BulletinEmote getEmote() {
			return BulletinEmote.SMILE;
		}

		@Override
		public List<BulletinReply> getReplies() {
			return Collections.emptyList();
		}
		
	}
	
	public static MapleGuildBulletin loadBulletin(MapleGuild guild) throws SQLException{
		
		MapleGuildBulletin bulletin = new MapleGuildBulletin();
		
		bulletin.owner = guild.getGuildId();
		bulletin.world = guild.getWorld().getId();
		
		bulletin.posts = new HashMap<>();
		
		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `post_id`,`title`,`content`,`poster`,`post_time`,`notice` FROM `guild_bbs` WHERE `guild`=?", guild.getGuildId());
		
		for(QueryResult result : results){
			
			int postId = result.get("post_id");
			String title = result.get("title");
			String content = result.get("content");
			int poster = result.get("poster");
			long post_time = result.get("post_time");
			boolean notice = result.get("notice");
			
			MapleBulletinPost post = new MapleBulletinPost(poster, postId, post_time, content, title);
			
			if(notice){
				bulletin.notice = post;
			}else{
				bulletin.posts.put(postId, post);
			}
			
		}
		
		
		return bulletin;
	}

}
