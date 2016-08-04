package maplestory.client;

import java.util.ArrayList;
import java.util.List;

import maplestory.player.MapleCharacter;

public class MapleMessenger {
 
	private List<MapleCharacter> players;
	
	public MapleMessenger(MapleCharacter player) {
		players = new ArrayList<>(3);
		players.add(player);
	}
	
}
