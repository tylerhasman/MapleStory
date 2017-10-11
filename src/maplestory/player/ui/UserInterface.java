package maplestory.player.ui;

import java.util.Collection;

import maplestory.player.MapleCharacter;

public interface UserInterface {

	public void addPlayer(MapleCharacter chr);
	
	public void chat(String msg, MapleCharacter source);
	
	public void chat(String msg);
	
	public void removePlayer(MapleCharacter chr);
	
	public Collection<MapleCharacter> getPlayers();
	
}
