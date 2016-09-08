package maplestory.party;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.server.MapleServer;
import maplestory.server.net.PacketFactory;

public class MapleParty {

	private static final AtomicInteger nextId = new AtomicInteger();

	public static final int MAX_SIZE = 6;
	
	private List<PartyEntry> players;
	private int leader;
	@Getter
	private int partyId;
	
	private int worldId;
	
	@Getter
	private boolean disbanded;
	
	MapleParty(MapleCharacter leader) {
		partyId = nextId.incrementAndGet();
		disbanded = false;
		players = new ArrayList<>(MAX_SIZE);
		players.add(new PartyEntry(leader, partyId));
		this.leader = leader.getId();
		worldId = leader.getWorldId();
	}
	
	public double getExpBonus(MapleCharacter chr){
		PartyEntry entry = getEntry(chr);
		
		double bonus = 0D;
		
		if(entry != null){
			
			int applicable = 0;
			
			for(PartyEntry member : getMembers()){
				if(member.getSnapshot().getId() != chr.getId()){
					if(member.getSnapshot().getMapId() == chr.getMapId()){
						if(member.getSnapshot().isOnline()){
							applicable++;
						}
					}
				}
			}
			
			if(applicable > 0){
				bonus += 0.05D;
			}
			
			bonus += applicable * 0.05D;
		}
		
		return bonus;
	}
	
	public void broadcastPacket(byte[] packet, int exclude){
		
		for(PartyEntry entry : getMembers()){
			if(entry.getSnapshot().getId() == exclude){
				continue;
			}
			if(entry.getSnapshot().isOnline()){
				entry.getSnapshot().getLiveCharacter().getClient().sendPacket(packet);
			}
		}
		
	}
	
	public int size(){
		return getMembers().size();
	}
	
	public List<PartyEntry> getMembers(){
		synchronized (players) {
			return Collections.unmodifiableList(new ArrayList<>(players));
		}
	}
	
	public PartyEntry getLeader(){
		for(PartyEntry ent : getMembers()){
			if(ent.getSnapshot().getId() == leader){
				return ent;
			}
		}
		throw new IllegalStateException("Party has no leader!");
	}
	
	public void addPlayer(MapleCharacter chr){
		synchronized (players) {
			players.add(new PartyEntry(chr, partyId));
			broadcastPacket(PacketFactory.updatePartyMemberHp(chr), chr.getId());
			for(PartyEntry member : getMembers()){
				if(member.getSnapshot().isOnline()){
					MapleCharacter mem = member.getSnapshot().getLiveCharacter();
					
					mem.getClient().sendPacket(PacketFactory.partyUpdate(chr.getClient().getChannelId(), this, PartyOperationType.JOIN, chr.createSnapshot()));
					
					if(member.getSnapshot().getId() != chr.getId()){
						chr.getClient().sendPacket(PacketFactory.updatePartyMemberHp(member.getSnapshot().getLiveCharacter()));
						member.getSnapshot().getLiveCharacter().getClient().sendPacket(PacketFactory.updatePartyMemberHp(chr));
					}
				}
			}
			
		}
	}
	
	public void leave(MapleCharacter leaving) {
		synchronized (players) {
			PartyEntry entry = getEntry(leaving);
			
			if(entry == null){
				return;
			}
			
			if(isLeader(leaving)) {
				disband();
			}else{
				sendRemovePacket(entry, PartyOperationType.LEAVE);
				players.removeIf(ent -> ent.equals(entry));
				updateParty();
			}
			
			leaving.getMap().executeMapScript(leaving, "onPlayerLeaveParty", leaving, this);
		}
		
	}
	
	public void disband(){
		synchronized (players) {
			disbanded = true;
			sendRemovePacket(getLeader(), PartyOperationType.DISBAND);
			for(PartyEntry entry : getMembers()){
				players.remove(entry);
				MapleCharacter chr = entry.getSnapshot().getLiveCharacter();
				if(chr != null){
					chr.leaveParty();
				}
			}
			
			MapleServer.getWorld(worldId).unregisterParty(this);	
		}
	}
	
	public void expel(PartyEntry entry){
		if(entry.getPartyId() != partyId){
			return;
		}
		
		sendRemovePacket(entry, PartyOperationType.EXPEL);
		players.remove(entry);
		
		MapleCharacter chr = entry.getSnapshot().getLiveCharacter();
		
		if(chr != null){
			chr.leaveParty();
		}
		updateParty();
	}
	
	private void sendRemovePacket(PartyEntry removed, PartyOperationType reason){
		for(PartyEntry entry : getMembers()){
			MapleCharacterSnapshot snap = entry.getSnapshot();
			MapleCharacter member = snap.getLiveCharacter();
			
			if(member != null){
				member.getClient().sendPacket(PacketFactory.partyUpdate(removed.getSnapshot().getChannel(), this, reason, removed.getSnapshot()));
			}
		}
	}

	
	public boolean isLeader(MapleCharacter chr){
		return chr.getId() == leader;
	}
	
	public boolean isMember(MapleCharacter chr){
		return getEntry(chr) != null;
	}
	
	public boolean isMember(int cid){
		return getEntry(cid) != null;
	}
	
	public PartyEntry getEntry(MapleCharacter chr){
		return getEntry(chr.getId());
	}
	
	public PartyEntry getEntry(int cid){
		for(PartyEntry entry : getMembers()){
			if(entry.snapshot.getId() == cid){
				return entry;
			}
		}
		return null;
	}
	
	public void updateMember(PartyEntry member){
		if(member.getPartyId() != partyId){
			return;
		}
		member.updateSnapshot();
		MapleCharacterSnapshot snap = member.getSnapshot();
		
		if(leader == member.getSnapshot().getId()){
			if(!member.getSnapshot().isOnline()){
				findNewLeader();
			}
		}
		
		for(PartyEntry entry : getMembers()){
			MapleCharacter chr = entry.getSnapshot().getLiveCharacter();
			
			if(chr == null){
				continue;
			}
			
			chr.getClient().sendPacket(PacketFactory.partyUpdate(entry.getSnapshot().getChannel(), this, PartyOperationType.SILENT_UPDATE, snap));
		}
	}
	
	private void findNewLeader(){
		for(PartyEntry entry : getMembers()){
			if(entry.getSnapshot().isOnline()){
				changeLeader(entry);
				break;
			}
		}
	}
	
	public void updateParty(){
		for(PartyEntry entry : getMembers()){
			updateMember(entry);
		}
	}
	
	public static MapleParty createParty(MapleCharacter leader){
		MapleParty party = new MapleParty(leader);
		
		leader.getClient().getWorld().registerParty(party);
		
		return party;
	}

	public void changeLeader(PartyEntry to) {
		leader = to.getSnapshot().getId();
		for(PartyEntry entry : getMembers()){
			MapleCharacter chr = entry.getSnapshot().getLiveCharacter();
			
			if(chr != null){
				chr.getClient().sendPacket(PacketFactory.partyUpdate(to.getSnapshot().getChannel(), this, PartyOperationType.CHANGE_LEADER, to.getSnapshot()));
			}
		}
	}
	
	public boolean isFull() {
		return size() >= MAX_SIZE;
	}
	
	public void updateMember(MapleCharacter character) {
		updateMember(getEntry(character));
	}
	
	public static class PartyEntry {
		
		private WeakReference<MapleCharacter> player;
		private MapleCharacterSnapshot snapshot;
		private final int partyId;
		
		public PartyEntry(MapleCharacter chr, int partyId) {
			setPlayer(chr);
			this.partyId = partyId;
		}
		
		public int getPartyId() {
			return partyId;
		}
		
		public void setPlayer(MapleCharacter chr){
			player = new WeakReference<MapleCharacter>(chr);
			snapshot = chr.createSnapshot();
		}
		
		@Override
		public String toString() {
			return "PartyEntry("+snapshot.getName()+")";
		}
		
		private void updateSnapshot(){
			MapleCharacter chr = player.get();
			if(chr != null){
				snapshot = chr.createSnapshot();
			}
		}
		
		public MapleCharacterSnapshot getSnapshot() {
			return snapshot;
		}
		
		public void updatePlayerReference(MapleCharacter chr) {
			if(player.get() == null || player.get().getId() == chr.getId()){
				player = new WeakReference<MapleCharacter>(chr);
			}else{
				throw new IllegalArgumentException("Cannot update player that isn't the right player. Expected "+snapshot.getId()+" got "+chr.getId());
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof PartyEntry){
				PartyEntry other = (PartyEntry) obj;
				if(other.getSnapshot().getId() == getSnapshot().getId() && getPartyId() == other.partyId){
					return true;
				}
			}
			return super.equals(obj);
		}
		
	}
	
}
