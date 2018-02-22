package maplestory.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import maplestory.server.MapleServer;

public class BuddyList {

	private List<BuddyListEntry> entries;
	
	private List<Integer> requests;//Requests from other players
	
	private int capacity;
	
	public BuddyList(int capacity) {
		entries = new ArrayList<>();
		requests = new ArrayList<>();
		this.capacity = capacity;
	}
	
	public void broadcastPacket(byte[] data) {
		for(BuddyListEntry entry : entries) {
			entry.snapshot.getLiveCharacter().ifPresent(chr -> chr.getClient().sendPacket(data));
		}
	}
	
	public MapleCharacterSnapshot nextRequest() {
		if(requests.isEmpty()) {
			return null;
		}
		return MapleCharacterSnapshot.createDatabaseSnapshot(requests.remove(0));
	}
	
	public void addRequest(MapleCharacter chr) {
		if(hasBuddyRequestFrom(chr.getId())) {
			return;
		}
		requests.add(chr.getId());
	}
	
	public boolean hasBuddyRequestFrom(int cid) {
		return requests.contains(cid);
	}
	
	public void deleteRequest(int id) {
		requests.remove((Object) id);//We need to do this or it mistakes it for the index
	}
	
	public void addBuddy(int buddyId, String group) {
		
		MapleCharacterSnapshot snapshot = MapleCharacterSnapshot.createDatabaseSnapshot(buddyId);
		
		if(snapshot.getId() == -1) {
			return;//Character must have been deleted
		}
		
		if(isBuddy(snapshot.getName())) {
			return;//Already buddies
		}
		
		deleteRequest(buddyId);
		entries.add(new BuddyListEntry(snapshot, group));
	}

	public int getCapacity() {
		return capacity;
	}
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	private BuddyListEntry getEntry(String name) {
		for(BuddyListEntry entry : entries) {
			if(entry.snapshot.getName().equalsIgnoreCase(name)) {
				return entry;
			}
		}
		return null;
	}
	
	public String getGroup(String name) {
		BuddyListEntry entry = getEntry(name);
		
		return entry != null ? entry.group : null;
	}
	
	public boolean isBuddy(String name) {
		return getEntry(name) != null;
	}
	
	public void changeGroup(String name, String group) {
		BuddyListEntry entry = getEntry(name);
		
		if(entry != null) {
			entry.group = group;
		}
	}
	
	public List<BuddyListEntry> getEntries() {
		return Collections.unmodifiableList(entries);
	}
	
	public static class BuddyListEntry {
		
		private MapleCharacterSnapshot snapshot;
		private String group;
		private boolean visible;
		
		public BuddyListEntry(MapleCharacterSnapshot snapshot, String group) {
			this.snapshot = snapshot;
			this.group = group;
			visible = true;
		}
		
		public boolean isVisible() {
			return visible;
		}
		
		public MapleCharacterSnapshot getSnapshot() {
			MapleCharacter online = MapleServer.getWorld(snapshot.getWorld()).getPlayerStorage().getById(snapshot.getId());
			if(online != null) {
				snapshot = online.createSnapshot();
			}
			
			return snapshot;
		}
		
		public String getGroup() {
			return group;
		}
		
	}

	public boolean isFull() {
		return entries.size() >= capacity;
	}

	
	
}
