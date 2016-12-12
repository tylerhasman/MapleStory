package maplestory.guild.bbs;

import java.util.Collection;
import java.util.List;

import maplestory.guild.MapleGuild;
import maplestory.player.MapleCharacter;

public interface GuildBulletin {

	public MapleGuild getOwner();
	
	public Collection<BulletinPost> getPosts();

	public BulletinPost getNotice();

	public BulletinPost getPost(int postId);

	public void deletePost(int postId);

	public void addPost(String title, String text, BulletinEmote emote, MapleCharacter poster);

	public void setNotice(String title, String text, BulletinEmote emote, MapleCharacter character);

	public void editPost(int postId, String title, String text, int icon, MapleCharacter character);
	
	
}
