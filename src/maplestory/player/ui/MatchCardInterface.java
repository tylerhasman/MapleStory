package maplestory.player.ui;

import maplestory.player.MapleCharacter;

public interface MatchCardInterface extends MiniGameInterface {

	public void selectCard(int turn, int slot, MapleCharacter chr);
	
}
