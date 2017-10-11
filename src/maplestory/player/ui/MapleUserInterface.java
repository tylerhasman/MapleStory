package maplestory.player.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;

public abstract class MapleUserInterface implements UserInterface {

	private List<MapleCharacterSnapshot> characters;
	
	private final int capacity;
	
	protected MapleUserInterface(int capacity) {
		this.capacity = capacity;
		characters = new ArrayList<>();
	}

	protected String getDefaultChatSource(){
		return "[Server]";
	}
	
	@Override
	public void addPlayer(MapleCharacter chr) {
		if(characters.size() >= capacity){
			throw new InterfaceFullException(this);
		}
		characters.add(chr.createSnapshot());
	}

	@Override
	public void chat(String msg, MapleCharacter source) {
		for(MapleCharacterSnapshot snapshot : characters){
			snapshot.ifOnline(chr -> sendChatPacket(chr, source.getName(), msg));
		}
	}
	
	@Override
	public void chat(String msg){
		for(MapleCharacterSnapshot snapshot : characters){
			snapshot.ifOnline(chr -> sendChatPacket(chr, getDefaultChatSource(), msg));
		}
	}

	@Override
	public void removePlayer(MapleCharacter chr) {
		characters.removeIf(snapshot -> snapshot.getId() == chr.getId());
	}

	@Override
	public Collection<MapleCharacter> getPlayers() {
		List<MapleCharacter> online = new ArrayList<>();
		
		for(MapleCharacterSnapshot snapshot : characters){
			MapleCharacter chr = snapshot.getLiveCharacter();
			if(chr != null){
				online.add(chr);
			}
		}
		
		return online;
	}
	
	protected abstract void sendChatPacket(MapleCharacter to, String source, String msg);
	
	public static class InterfaceFullException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -630589026701642659L;
		
		private MapleUserInterface ui;
		
		InterfaceFullException(MapleUserInterface ui) {
			this.ui = ui;
		}
		
		@Override
		public String getMessage() {
			return "The "+ui.getClass().getName()+" is at capacity of "+ui.capacity;
		}
		
	}

}
