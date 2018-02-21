package maplestory.world;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import maplestory.inventory.MapleCashInventory;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.player.MapleJob;

public class RankManager {

	private static final Comparator<MapleCharacterSnapshot> RANKER = (s1, s2) -> {
		
		if(s1.getLevel() == s2.getLevel()) {
			return Integer.compare(s2.getId(), s1.getId());//Its not fair but whatever
		}else {
			return Integer.compare(s2.getLevel(), s1.getLevel());
		}
		
	};
	
	private World world;
	
	private List<MapleCharacterSnapshot> worldRankings;
	private Map<Integer, List<MapleCharacterSnapshot>> jobRankings;
	
	private List<MapleCharacterSnapshot> lastWorldRankings;
	private Map<Integer, List<MapleCharacterSnapshot>> lastJobRankings;
	
	private ReadWriteLock lock;
	
	public RankManager(World world) {
		this.world = world;
		worldRankings = new ArrayList<>();
		jobRankings = new HashMap<>();
		lastWorldRankings = new ArrayList<>();
		lastJobRankings = new HashMap<>();
		
		for(MapleJob job : MapleJob.values()) {
			jobRankings.put(job.getId(), new ArrayList<>());
			lastJobRankings.put(job.getId(), new ArrayList<>());
		}
		
		lock = new ReentrantReadWriteLock(true);
		
	}
	
	private void updateLastRankings() {
		lastWorldRankings = new ArrayList<>(worldRankings);
		for(MapleJob job : MapleJob.values()) {
			lastJobRankings.put(job.getId(), new ArrayList<>(jobRankings.get(job.getId())));
		}
	}
	
	public void updateRankings() {
		
		lock.writeLock().lock();
		
		try {
			world.getLogger().info("Updating rankings for "+world.getName());
			List<MapleCharacterSnapshot> chrs = MapleCharacterSnapshot.getCharacters(world);
			world.getLogger().info("Retrieved "+chrs.size()+" characters for world "+world.getName());
			
			updateLastRankings();
			worldRankings.clear();
			worldRankings.addAll(chrs);
			Collections.sort(worldRankings, RANKER);
			
			for(MapleJob job : MapleJob.values()) {
				jobRankings.get(job.getId()).clear();
			}
			
			for(MapleCharacterSnapshot snap : chrs) {
				if(jobRankings.containsKey(snap.getJob())) {
					jobRankings.get(snap.getJob()).add(snap);
				}
			}
			
			for(MapleJob job : MapleJob.values()) {
				Collections.sort(jobRankings.get(job.getId()), RANKER);
			}
			
			world.getLogger().info("Finished updating rankings for world "+world.getName());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			lock.writeLock().unlock();
		}
		
	}
	
	private int indexOf(List<MapleCharacterSnapshot> snaps, MapleCharacter chr) {
		return Collections.binarySearch(snaps, chr.createSnapshot(), RANKER);
	}
	
	public int getWorldRanking(MapleCharacter chr) {
		lock.readLock().lock();
		try {
			int rank = indexOf(worldRankings, chr);
			
			if(rank >= 0) {
				return rank + 1;
			}else {
				return -1;
			}
		}finally {
			lock.readLock().unlock();
		}
	}
	
	public int getJobRanking(MapleCharacter chr) {
		lock.readLock().lock();
		try {
			if(!jobRankings.containsKey(chr.getJob().getId())) {
				return -1;
			}
			int rank = indexOf(jobRankings.get(chr.getJob().getId()), chr);
			if(rank >= 0) {
				return rank + 1;
			}else {
				return -1;
			}
		}finally {
			lock.readLock().unlock();
		}

	}
	
	public int getWorldRankingChange(MapleCharacter chr) {
		
		int last = indexOf(lastWorldRankings, chr);
		int current = indexOf(worldRankings, chr);
		
		if(last == -1 || current == -1) {
			return 0;
		}
		
		return current - last;
	}
	
	public int getJobRankingChange(MapleCharacter chr) {
		
		int last = indexOf(lastJobRankings.get(chr.getJob().getId()), chr);
		int current = indexOf(jobRankings.get(chr.getJob().getId()), chr);
		
		if(last == -1 || current == -1) {
			return 0;
		}
		
		return current - last;
	}
	
}
