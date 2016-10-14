package maplestory.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import maplestory.player.MapleCharacter;

public class PlayerStorage {

	private ReentrantReadWriteLock locks = new ReentrantReadWriteLock(true);
	private ReadLock rLock = locks.readLock();
	private WriteLock wLock = locks.writeLock();
	
	private Map<Integer, MapleCharacter> storage;
	
	public PlayerStorage() {
		storage = new HashMap<>();
	}
	
	public MapleCharacter getById(int id){
		rLock.lock();
		try{
			return storage.get(Integer.valueOf(id));
		}finally{
			rLock.unlock();
		}
	}
	
	public MapleCharacter getByName(String name){
		rLock.lock();
		try{
			for(MapleCharacter chr : storage.values()){
				if(chr.getName().equalsIgnoreCase(name)){
					return chr;
				}
			}
			
			return null;
		}finally{
			rLock.unlock();
		}
	}
	
	public MapleCharacter getByAccountId(int id) {
		rLock.lock();
		try{
			for(MapleCharacter chr : storage.values()){
				if(chr.getAccountId() == id){
					return chr;
				}
			}
			
			return null;
		}finally{
			rLock.unlock();
		}
	}
	
	public void addPlayer(MapleCharacter chr){
		wLock.lock();
		try{
			storage.put(chr.getId(), chr);
		}finally{
			wLock.unlock();
		}
	}
	
	public void removePlayer(int id){
		wLock.lock();
		try{
			storage.remove(id);
		}finally{
			wLock.unlock();
		}
	}
	
	public Collection<MapleCharacter> getAllPlayers(){
        rLock.lock();    
        try {
            return new ArrayList<>(storage.values());
        } finally {
            rLock.unlock();
        }
	}
	
}
