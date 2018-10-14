package maplestory.guild.bbs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import database.MapleDatabase;
import database.QueryResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
	public void editPost(int postId, String title, String text, BulletinEmote emote, MapleCharacter character) {
		BulletinPost old = getPost(postId);
		
		if(old != null){
			
			MapleBulletinPost newPost = new MapleBulletinPost(character.getId(), old.getPostId(), old.getPostTime(), text, title, emote, null);
			
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
		MapleBulletinPost post = new MapleBulletinPost(poster.getId(), nextPost++, System.currentTimeMillis(), text, title, emote, null);
		
		posts.put(post.postId, post);
	}
	
	@Override
	public void setNotice(String title, String text, BulletinEmote emote, MapleCharacter poster) {
		notice = new MapleBulletinPost(poster.getId(), nextPost++, System.currentTimeMillis(), text, title, emote, null);
	}
	
	@AllArgsConstructor(access=AccessLevel.PROTECTED)
	static class MapleBulletinReply implements BulletinReply {

		@Getter
		private int replyId, author;
		
		@Getter
		private long postTime;
		
		@Getter
		private String content;
		
	}
	
	@AllArgsConstructor(access=AccessLevel.PROTECTED)
	static class MapleBulletinPost implements BulletinPost {

		private int characterId;
		
		private int postId;
		
		private long postTime;
		
		private String content, subject;

		private BulletinEmote emote;
		
		private List<BulletinReply> replies;
		
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
			return emote;
		}

		@Override
		public List<BulletinReply> getReplies() {
			if(replies == null)
				return Collections.emptyList();
			return replies;
		}

		@Override
		public void addReply(MapleCharacter author, String message) {
			if(replies == null){
				replies = new ArrayList<>();
			}
			replies.add(new MapleBulletinReply(replies.size(), author.getId(), System.currentTimeMillis(), message));
		}
		
		@Override
		public void removeReply(int replyId) {
			if(replies == null){
				return;
			}
			replies.removeIf(reply -> reply.getReplyId() == replyId);
		}
		
		@Override
		public BulletinReply findReply(int replyId) {
			if(replies == null){
				return null;
			}
			
			for(BulletinReply reply : replies){
				if(reply.getReplyId() == replyId)
					return reply;
			}
			
			return null;
		}
		
	}
	
	public static MapleGuildBulletin loadBulletin(MapleGuild guild) throws SQLException{
		
		MapleGuildBulletin bulletin = new MapleGuildBulletin();
		
		bulletin.owner = guild.getGuildId();
		bulletin.world = guild.getWorldId();
		
		bulletin.posts = new HashMap<>();
		
		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `post_id`,`title`,`content`,`poster`,`post_time`,`notice`,`emote` FROM `guild_bbs` WHERE `guild`=?", guild.getGuildId());
		
		for(QueryResult result : results){
			
			int postId = result.get("post_id");
			String title = result.get("title");
			String content = result.get("content");
			int poster = result.get("poster");
			long post_time = result.get("post_time");
			int notice = result.get("notice");
			int emote = result.get("emote");
			
			List<QueryResult> replyResults = MapleDatabase.getInstance().query("SELECT  `author`, `post_time`, `content` FROM `guild_bbs_replies` WHERE `post`=?", postId);
			
			List<BulletinReply> replies = null;
			
			if(replyResults.size() > 0){
				replies = new ArrayList<>();
				
				for(QueryResult replyResult : replyResults){
					
					int author = replyResult.get("author");
					long postTime = replyResult.get("post_time");
					String replyContent = replyResult.get("content");
					
					replies.add(new MapleBulletinReply(replies.size(), author, postTime, replyContent));
				}
			}
			
			MapleBulletinPost post = new MapleBulletinPost(poster, postId, post_time, content, title, BulletinEmote.getById(emote), replies);
			
			
			if(notice == 1){
				bulletin.notice = post;
			}else{
				bulletin.posts.put(postId, post);
			}
			
		}
		
		
		return bulletin;
	}

}
