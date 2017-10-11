package maplestory.client;

import java.util.ArrayList;
import java.util.List;

import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.server.net.PacketFactory;

public class MapleMessenger {
 
	private static final int CAPACITY = 3;
	
	private static int nextId = 0;
	
	private List<MapleCharacterSnapshot> players;
	private final int uniqueId;
	private List<MessengerInvitation> invites;
	
	public MapleMessenger(MapleCharacter player) {
		players = new ArrayList<>(3);
		players.add(player.createSnapshot());
		uniqueId = nextId++;
		invites = new ArrayList<>();
	}
	
	public int getUniqueId() {
		return uniqueId;
	}
	
	public int numPlayers(){
		return players.size();
	}
	
	public void invite(MapleCharacter target, MapleCharacter source){
		
		if(isInChat(target)){
			return;
		}
		
		invites.add(new MessengerInvitation(target, source));
		target.getClient().sendPacket(PacketFactory.messengerInvite(source.getName(), getUniqueId()));
		
	}
	
	public boolean isInvited(MapleCharacter target){
		for(MessengerInvitation invite : invites){
			if(invite.getInvited().getId() == target.getId()){
				return true;
			}
		}
		return false;
	}
	
	public void broadcastMessage(String msg){
		broadcastMessage(null, msg);
	}
	
	public void broadcastMessage(MapleCharacter source, String msg){
		for(MapleCharacterSnapshot snapshot : players){
			if(source != null && snapshot.getId() == source.getId()){
				continue;
			}
			snapshot.getLiveCharacter().ifPresent(chr -> chr.getClient().sendPacket(PacketFactory.messengerChat(msg)));
			
		}
	}
	
	public boolean isFull(){
		return players.size() >= CAPACITY;
	}
	
	public void addPlayer(MapleCharacter chr){
		if(isFull()){
			throw new IllegalStateException("Capacity is met! This messenger cannot accept players");
		}
		if(isInChat(chr)){
			throw new IllegalArgumentException("Cannot add "+chr.getName()+" to messenger, they are already in it.");
		}
		
		players.add(chr.createSnapshot());
		
		invites.removeIf(invite -> invite.getInvited().getId() == chr.getId());
		
		chr.getClient().sendPacket(PacketFactory.messengerJoin(position(chr)));
		
		for(MapleCharacterSnapshot snapshot : players){
			if(snapshot.getId() == chr.getId()){
				continue;
			}
			
			snapshot.ifOnline(other -> other.getClient().sendPacket(PacketFactory.messengerAddPlayer(chr.getName(), chr, position(chr), chr.getClient().getChannelId()+1)));
		}
		
		for(MapleCharacterSnapshot snapshot : players){
			if(snapshot.getId() == chr.getId()){
				continue;
			}
			snapshot.getLiveCharacter().ifPresent(other -> {
				chr.getClient().sendPacket(PacketFactory.messengerAddPlayer(snapshot.getName(), other, position(other), snapshot.getChannel()+1));
			});
		}
		
	}
	
	public void removePlayer(MapleCharacter chr){
		if(!isInChat(chr)){
			throw new IllegalArgumentException("Cannot remove "+chr.getName()+" from messenger, they aren't a participant.");
		}
		
		int position = position(chr);
		
		players.removeIf(snapshot -> snapshot.getId() == chr.getId());
		
		for(MapleCharacterSnapshot snapshot : players){
			snapshot.ifOnline(other -> other.getClient().sendPacket(PacketFactory.messengerRemovePlayer(position)));
		}
		
	}
	
	private int position(MapleCharacter chr){
		if(!isInChat(chr)){
			throw new IllegalArgumentException("Player not in messenger");
		}
		for(int i = 0; i < players.size();i++){
			if(players.get(i) != null){
				if(players.get(i).getId() == chr.getId()){
					return i;
				}
			}
		}
		return -1;
	}
	
	private boolean isInChat(MapleCharacter chr){
		for(MapleCharacterSnapshot snapshot : players){
			if(snapshot.getId() == chr.getId()){
				return true;
			}
		}
		return false;
	}
	
	static class MessengerInvitation {
		
		private MapleCharacterSnapshot invited, inviter;
		
		public MessengerInvitation(MapleCharacter invited, MapleCharacter inviter) {
			this.invited = invited.createSnapshot();
			this.inviter = inviter.createSnapshot();
		}
		
		public MapleCharacterSnapshot getInvited() {
			return invited;
		}
		
		public MapleCharacterSnapshot getInviter() {
			return inviter;
		}
		
	}

	public void updatePlayer(MapleCharacter chr) {
		if(!isInChat(chr)){
			throw new IllegalArgumentException("Cannot update "+chr.getName()+", they aren't a participant.");
		}
		
		int position = position(chr);
		
		players.set(position, chr.createSnapshot());
		
		for(MapleCharacterSnapshot snapshot : players){
			if(snapshot.getId() == chr.getId()){
				continue;
			}
			
			if(snapshot.isOnline()){
				snapshot.getLiveCharacter().get().getClient().sendPacket(PacketFactory.messengerUpdatePlayer(chr, position, chr.getClient().getChannelId()+1));
			}
		}
		
	}
	
}
