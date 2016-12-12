package maplestory.guild.bbs;

import java.util.List;

import maplestory.player.MapleCharacterSnapshot;

public interface BulletinPost {
	
	public MapleCharacterSnapshot getAuthor();
	
	public int getPostId();
	
	public long getPostTime();
	
	public String getContent();
	
	public String getSubject();
	
	public BulletinEmote getEmote();
	
	public List<BulletinReply> getReplies();
	
}
