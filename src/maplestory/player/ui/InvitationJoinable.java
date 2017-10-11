package maplestory.player.ui;

import maplestory.player.MapleCharacter;

public interface InvitationJoinable {
	
	public void invitePlayer(MapleCharacter chr);
	
	public boolean isInvited(MapleCharacter chr);
	
}
