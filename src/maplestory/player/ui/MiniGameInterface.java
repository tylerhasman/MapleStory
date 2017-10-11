package maplestory.player.ui;

import maplestory.player.MapleCharacter;

public interface MiniGameInterface extends UserInterface {

	public void ready(MapleCharacter chr);
	
	public void unready(MapleCharacter chr);
	
	public void startGame(MapleCharacter chr);
	
	public void giveUp(MapleCharacter chr);
	
	public void requestTie(MapleCharacter chr);
	
	public void answerTie(MapleCharacter chr);
	
	public void skipTurn(MapleCharacter chr);
	
}
