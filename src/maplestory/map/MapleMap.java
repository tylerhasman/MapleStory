package maplestory.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import constants.ItemLetterFont;
import constants.MessageType;
import constants.ServerConstants;
import tools.TimerManager;
import tools.TimerManager.MapleTask;
import lombok.Getter;
import lombok.Setter;
import maplestory.channel.MapleChannel;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.life.MapleLifeFactory;
import maplestory.life.MapleMonster;
import maplestory.life.MapleNPC;
import maplestory.life.MapleSummon;
import maplestory.map.MapleMapItem.DropType;
import maplestory.player.MapleCharacter;
import maplestory.player.MaplePetInstance;
import maplestory.script.MapScriptManager;
import maplestory.script.MapleScript;
import maplestory.script.MapleScriptInstance;
import maplestory.server.MapleServer;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MonsterStatusEffect;

public class MapleMap {

	private ReentrantLock charactersLock = new ReentrantLock(true);
	private Map<Integer, MapleMapObject> objects;
	private List<MapleCharacter> characters;
	private List<MaplePortal> portals;
	
	@Getter
	private final int mapId;
	
	private final int world, channel;
	
	@Getter @Setter
	private MapleFootholdTree footholds;
	
	@Getter @Setter
	private String mapName, streetName;
	
	private AtomicInteger nextObjectId;
	
	@Getter
	private ReentrantLock objectLock = new ReentrantLock(true);
	
	private int returnMap;
	
	@Getter @Setter
	private int mobInterval;
	
	@Getter
	private List<SpawnPoint> monsterSpawnPoints;
	
	@Getter @Setter
	private int fieldLimit;
	
	@Getter @Setter
	private boolean town;
	
	private long lastPlayerEnter;
	
	@Getter
	private MapleClock clock;
	
	private Map<String, Object> metadata;
	
	private AtomicBoolean monsterSpawnsEnabled;
	
	private List<MapleTask> mapTasks;
	
	private MapleScriptInstance scriptInstance;
	private boolean scriptDisabled;
	
	public MapleMap(int mapid, int world, int channel, int returnMap, float monsterRate) {
		this.mapId = mapid;
		this.world = world;
		this.channel = channel;
		this.returnMap = returnMap;
		objects = new HashMap<>();
		characters = new ArrayList<>();
		portals = new ArrayList<>();
		nextObjectId = new AtomicInteger(0);
		monsterSpawnPoints = new ArrayList<>();
		monsterSpawnsEnabled = new AtomicBoolean(true);
		mapTasks = new ArrayList<>();
		mapTasks.add(TimerManager.scheduleRepeatingTask(new Runnable() {
			
			@Override
			public void run() {
				doMonsterSpawns();
			}
		}, 0, 100, TimeUnit.MILLISECONDS));
		
		mapTasks.add(TimerManager.scheduleRepeatingTask(new Runnable() {
			
			@Override
			public void run() {
				for(MapleMapObject obj : getObjects()){
					if(obj instanceof MapleReactor){
						MapleReactor reactor = (MapleReactor) obj;
						reactor.checkStageRequirements();
					}
				}
			}
			
		}, 0, 5000, TimeUnit.MILLISECONDS));
		
		
		lastPlayerEnter = 0;
		scriptDisabled = false;
	}
	
	public MapleReactor getReactorByName(String name){
		for(MapleMapObject obj : getObjects()){
			if(obj instanceof MapleReactor){
				MapleReactor r = (MapleReactor) obj;
				
				if(r.getReactorData().getName().equals(name)){
					return r;
				}
			}
		}
		return null;
	}
	
	public void setMetadata(String key, Object value){
		if (metadata == null) {
			metadata = new ConcurrentHashMap<>();
		}

		metadata.put(key, value);
	}
	
	public Object getMetadata(String key){
		return getMetadata(key, null);
	}
	
	public Object getMetadata(String key, Object def){
		if (metadata == null) {
			return def;
		}
		
		return metadata.getOrDefault(key, def);
	}
	
	public void broadcastMessage(MessageType type, String msg){
		for(MapleCharacter chr : getPlayers()){
			chr.sendMessage(type, msg);
		}
	}
	
	public boolean hasClock(){
		return clock != null;
	}
	
	public void createClock(int seconds, Runnable endTask){
		clock = new MapleClock(seconds, TimerManager.schedule(endTask, seconds, TimeUnit.SECONDS));
		for(MapleCharacter player : getPlayers()){
			player.getClient().sendPacket(PacketFactory.createClock(seconds));
		}
	}
	
	public void createClock(int seconds, String scriptFunctionName, Object... args){
		createClock(seconds, () -> executeMapScript(null, scriptFunctionName, args));
	}
	
	public void deleteClock(){
		if(hasClock()){
			clock.destroy();
			clock = null;
			for(MapleCharacter chr : getPlayers()){
				chr.getClient().sendPacket(PacketFactory.removeClock());
			}
		}
	}
	
	private int countLoadedNeighborsWithPlayers(){
		int sum = 0;
		for(MaplePortal portal : getPortals()){
			if(portal.getTarget() == null || portal.getTargetMapId() == getMapId()){
				continue;
			}
			if(MapleServer.getChannel(world, channel).getMapFactory().isMapLoaded(portal.getTargetMapId())){
				if(MapleServer.getChannel(world, channel).getMapFactory().getMap(portal.getTargetMapId()).getPlayers().size() > 0)
					sum++;
			}
		}
		return sum;
	}
	
	public boolean isReadyToUnload(){
		if(getPlayers().size() > 0){
			return false;
		}
		
		return countLoadedNeighborsWithPlayers() <= ServerConstants.MAP_NEIGHBOR_UNLOAD_THRESHHOLD && (System.currentTimeMillis() - lastPlayerEnter) > ServerConstants.EMPTY_MAP_UNLOAD_TIME;
	}
	
	public void setMonsterSpawnsEnabled(boolean flag) {
		monsterSpawnsEnabled.set(flag);
	}
	
	private void doMonsterSpawns(){
		if(monsterSpawnsEnabled.get()){
			for(SpawnPoint sp : monsterSpawnPoints){
				if(sp.shouldSpawn()){
					sp.spawnMonster(this, false);
				}
			}
		}
	}
	
	public void unload(){
		if(getPlayers().size() > 0){
			throw new IllegalArgumentException("Cannot unload a map with players in it");
		}
		getChannel().getLogger().debug("Unloading "+getMapId()+" ("+getMapName()+")");
		charactersLock.lock();
		objectLock.lock();
		try{
			monsterSpawnPoints.clear();
			objects.clear();
			footholds.clear();
			footholds = null;
			portals.clear();
			if(mapTasks != null){
				for(MapleTask task : mapTasks){
					task.cancel(false);
				}
				mapTasks.clear();
				mapTasks = null;
			}
		}finally{
			charactersLock.unlock();
			objectLock.unlock();
		}
	}
	
	private int getNextObjectId(){
		objectLock.lock();
		try{
			if(nextObjectId.getAndIncrement() >= 50000){
				nextObjectId.set(100);
			}
			
			return nextObjectId.get();
		}finally{
			objectLock.unlock();
		}

	}
	
	public List<MapleCharacter> getPlayers(){
		charactersLock.lock();
		try{
			return characters;
		}finally{
			charactersLock.unlock();
		}
	}
	
	public MapleMap getReturnMap(){
		return getChannel().getMapFactory().getMap(returnMap);
	}
	
	public List<MapleMonster> getMonsters(){
		objectLock.lock();
		try{
			List<MapleMonster> monsters = new ArrayList<>();
			
			for(MapleMapObject obj : getObjects()){
				if(obj instanceof MapleMonster){
					monsters.add((MapleMonster) obj);
				}
			}
			
			return monsters;
			
		}finally{
			objectLock.unlock();
		}
	}
	
	public void spawnDisappearingItemDrop(MapleMapObject source, Item item, Point pos){
		Point drop = calcDropPosition(pos);
		
		MapleMapItem dr = new MapleMapItem(item, -1, drop, DropType.FFA, this, source);
		
		broadcastPacket(PacketFactory.getDropItemPacket(dr, drop, source.getPosition(), 3));
	}
	
	public MapleChannel getChannel() {
		return MapleServer.getChannel(world, channel);
	}

	public void dropItem(Item item, Point position, MapleMapObject source){
		dropItem(item, 0, position, null, source);
	}
	
	public void dropItem(Item item, Point position, MapleCharacter owner, MapleMapObject source){
		dropItem(item, 0, position, owner, source);
	}
	
	public void dropItem(int itemId, int amount, Point position, MapleMapObject source){
		dropItem(ItemFactory.getItem(itemId, amount), position, source);
	}
	
	public void dropMesos(int amount, Point position, MapleCharacter owner, MapleMapObject source){
		dropItem(null, amount, position, owner, source);
	}
	
	public void dropMesos(int amount, Point position, MapleMapObject source){
		dropMesos(amount, position, null, source);
	}
	
	private void dropItem(Item item, int mesoAmount, Point position, MapleCharacter owner, MapleMapObject source){
		
		DropType type = null;
		int ownerId = -1;
		
		if(owner != null){
			type = DropType.OWNER_ONLY;
			ownerId = owner.getId();
		}else{
			type = DropType.FFA;
		}
		
		MapleMapItem drop = null;
		
		if(mesoAmount <= 0){
			drop = MapleMapItem.getItemDrop(item, position, this, ownerId, type, source);
		}else{
			drop = MapleMapItem.getMesoDrop(mesoAmount, position, this, ownerId, type, source);
		}

		addMapObject(drop, false);
		
		drop.broadcastDropPacket();
	}
	
    public Point calcDropPosition(Point initial) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 50));
        if (ret == null) {
            return initial;
        }
        return ret;
    }
    
    public void dropText(String text, ItemLetterFont font, int x, int y, boolean center){
    	
    	int x2 = x;
    	
    	if(center){
    		int totalWidth = font.calculateWidth(text);
    		x2 -= totalWidth / 2;
    	}
    	
    	for(char c : text.toCharArray()){
    		if(c == ' '){
    			x2 += 30;
    		}
    		if(!font.isCharacterSupported(c)){
    			continue;
    		}
    		int itemId = font.getCharacter(c);
    		int width = font.getWidth(c);
    		
    		MapleMapItem item = MapleMapItem.getItemDrop(ItemFactory.getItem(itemId, 1), new Point(x2, y), this, -1, DropType.OWNER_ONLY, null);
    		
    		addMapObject(item, false);
    		item.broadcastDropPacket();
    		x2 += width;
    	}
    	
    }
	
	public void addPlayer(MapleCharacter mapleCharacter) {
		charactersLock.lock();
		try{
			characters.add(mapleCharacter);
			lastPlayerEnter = System.currentTimeMillis();
			mapleCharacter.setMapId(getMapId());
			for(int i = 0; i < mapleCharacter.getPets().length;i++){
				MaplePetInstance inst = mapleCharacter.getPets()[i];
				
				if(inst == null){
					continue;
				}
				
				inst.setPosition(mapleCharacter.getPosition());
				inst.setFoothold(mapleCharacter.getFh());
				
				mapleCharacter.getClient().sendPacket(PacketFactory.spawnPet(mapleCharacter, inst, i));
			}
			for(MapleCharacter other : characters){
				if(other.getId() == mapleCharacter.getId())
					continue;
				mapleCharacter.sendSpawnData(other.getClient());
				other.sendSpawnData(mapleCharacter.getClient());
			}
			for(MapleMapObject obj : objects.values()){
				obj.sendSpawnData(mapleCharacter.getClient());
			}
			if(getPlayers().size() == 1){
				for(MapleMonster monster : getMonsters()){
					monster.findNewController();
				}
			}
			
			executeMapScript(mapleCharacter, "onPlayerEnter", mapleCharacter);
			
			if(hasClock()){
				mapleCharacter.getClient().sendPacket(PacketFactory.createClock(clock.getSecondsLeft()));
			}
		}finally{
			charactersLock.unlock();
		}
	}
	
	public void executeMapScript(MapleCharacter mapleCharacter, String funcName, Object... args){
		if(scriptDisabled){
			return;
		}
		if(scriptInstance == null){
			MapleScript script = new MapleScript("scripts/map/"+getMapId()+".js");
			if(!script.isDisabled()){
				SimpleBindings bindings = new SimpleBindings();
				bindings.put("msm", new MapScriptManager(this, mapleCharacter));
				try {
					scriptInstance = script.execute(bindings);
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
			}else{
				if(ServerConstants.CACHE_SCRIPTS)
					scriptDisabled = true;
				return;
			}
		}
		
		try {
			scriptInstance.function(funcName, args);
		} catch (NoSuchMethodException e) {
		} catch (ScriptException e) {
			e.printStackTrace();
		}

	}
    
    public MapleTask scheduleRepeatingScriptTask(String functionName, int delay, int interval, Object... args){
    	
    	MapleTask sf = TimerManager.scheduleRepeatingTask(new Runnable() {
			
			@Override
			public void run() {
				executeMapScript(null, functionName, args);
			}
			
		}, delay, interval, TimeUnit.MILLISECONDS);
    	
    	if (mapTasks == null) {
			mapTasks = new ArrayList<>();	
		}
    	
    	mapTasks.add(sf);
    	
    	return sf;
    }
	
	public void removePlayer(MapleCharacter character){

		charactersLock.lock();
		try{
			boolean worked = characters.removeIf(c -> c.getId() == character.getId());
			
			if(worked){
				for(MapleCharacter other : characters){
					character.sendDestroyData(other.getClient());
				}
				executeMapScript(character, "onPlayerExit", character);
			}
			lastPlayerEnter = System.currentTimeMillis();
			
		}finally{
			charactersLock.unlock();
		}
		
		//TODO: Maps should unload if there is no one in them? Maybe...
		
		objectLock.lock();
		try{
			
			for(MapleMonster monster : getMonsters()){
				if(monster.getController() == null){
					continue;
				}
				if(monster.getController().getId() == character.getId()){
					monster.findNewController();
				}
			}
			
		}finally{
			objectLock.unlock();
		}
	}

	public void broadcastPacket(byte[] packet, int exclude) {
		for(MapleCharacter chr : getPlayers()){
			if(chr.getId() == exclude){
				continue;
			}
			
			chr.getClient().sendPacket(packet);
		}
	}

	public void addPortal(MaplePortal makePortal) {
		portals.add(makePortal);
	}

	public MaplePortal getPortal(String target) {
		
		for(MaplePortal portal : portals){
			if(portal.getName().equals(target)){
				return portal;
			}
		}
		
		return null;
	}

	public MaplePortal getFallbackPortal() {
		return portals.get(0);
	}

	public void addMapObject(MapleMapObject obj, boolean sendSpawn) {
		objectLock.lock();
		try{
			obj.setObjectId(getNextObjectId());
			if(sendSpawn){
				for(MapleCharacter chr : getPlayers()){
					obj.sendSpawnData(chr.getClient());
				}	
			}
			objects.put(obj.getObjectId(), obj);
		}finally{
			objectLock.unlock();
		}
	}

	public Collection<MapleMapObject> getObjects() {
		objectLock.lock();
		try{
			return new ArrayList<>(objects.values());
		}finally{
			objectLock.unlock();
		}
	}
	
    private Point calcPointBelow(Point initial) {
        MapleFoothold fh = footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    }
    
    public int getFoothold(Point point){
    	MapleFoothold fh = footholds.findBelow(point);
    	
    	return fh.getId();
    }

    /**
     * it's threadsafe, gtfo :D
     *
     * @param monster
     * @param mobTime
     */
    public void addMonsterSpawn(MapleMonster monster, int mobTime, int team) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        
        SpawnPoint sp = new SpawnPoint(newpos, mobTime, monster.getId(), monster.getF(), monster.getFh(), mobInterval);
        
        monsterSpawnPoints.add(sp);
        
        if(sp.shouldSpawn()){
        	sp.spawnMonster(this, false);
        }
        
        //SpawnPoint sp = new SpawnPoint(monster, newpos, !monster.isMobile(), mobTime, mobInterval, team);
       // monsterSpawn.add(sp);
        /*if (sp.shouldSpawn() || mobTime == -1) {// -1 does not respawn and should not either but force ONE spawn
            spawnMonster(sp.getMonster());
        }*/
        //monster.setPosition(newpos);
       // spawnMonster(monster);
    }

    public MapleMonster spawnMonster(int id, int x, int y){
    	MapleMonster monster = MapleLifeFactory.getMonster(id);
    	
    	MapleFoothold fh = footholds.findBelow(new Point(x, y));
    	
    	if(fh != null){
    		y = (fh.getY1() + fh.getY2()) / 2;//Average
    	}
    	
    	monster.setPosition(new Point(x, y));
    	
    	spawnMonster(monster);
    	
    	return monster;
    }
    
	public void spawnMonster(MapleMonster monster) {
		objectLock.lock();
		try{
			Point position = new Point(monster.getPosition());
			monster.setMap(this);
			monster.setObjectId(getNextObjectId());
			monster.getPosition().y = calcPointBelow(position).y;
			monster.spawn();
			objects.put(monster.getObjectId(), monster);
		}finally{
			objectLock.unlock();
		}
	}

	public MapleMapObject getObject(int objectid) {
		return objects.get(objectid);
	}

	public void removeObject(int objectId) {
		objectLock.lock();
		try{
			MapleMapObject obj = objects.remove(objectId);
			if(obj != null){
				for(MapleCharacter chr : getPlayers()){
					obj.sendDestroyData(chr.getClient());
				}
			}
		}finally{
			objectLock.unlock();
		}

	}

	public void broadcastPacket(MapleCharacter mapleCharacter, byte[] packet, Point position) {
		for(MapleCharacter chr : getPlayers()){
			if(!chr.equals(mapleCharacter)){
				chr.getClient().sendPacket(packet);
			}
		}
	}

	public void broadcastPacket(byte[] packet) {
		broadcastPacket(packet, -1);
	}

	public int getReturnMapId() {
		return returnMap;
	}

	public void spawnSummon(MapleSummon tosummon) {
		addMapObject(tosummon, true);
		tosummon.spawn();
	}
	
	public static class SpawnPoint {
		
		@Getter
		private Point location;
		@Getter
		private int monsterId;
		
		private int f, fh;
		
		private int mobTime;
		
		private long deathTime;
		
		private int interval;
		
		public SpawnPoint(Point location, int mobTime, int monsterId, int f, int fh, int interval) {
			this.location = location;
			this.mobTime = mobTime;
			this.monsterId = monsterId;
			this.f = f;
			this.fh = fh;
			deathTime = 0;
			this.interval = interval;
		}
		
		private void monsterDie(int animation){
			deathTime = System.currentTimeMillis() - animation;
		}
		
		public boolean shouldSpawn(){
			return nextSpawnTime() >= mobTime;
		}
		
		public long nextSpawnTime(){
			return System.currentTimeMillis() - deathTime - interval;
		}
		
		public MapleMonster spawnMonster(MapleMap map, boolean force){
			if(force || shouldSpawn()){
				MapleMonster monster = createMonster();
				
				deathTime = Long.MAX_VALUE;
				
				monster.addDeathListener((animation) -> monsterDie(animation));
				
				map.spawnMonster(monster);
				
				return monster;
			}
			
			return null;
		}
		
		private MapleMonster createMonster(){
			MapleMonster monster = MapleLifeFactory.getMonster(monsterId);
			
			monster.setPosition(location);
			monster.setF(f);
			monster.setFh(fh);
			
			return monster;
		}
		
	}

	public List<MapleMapObject> getMapObjectsInBox(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<>();
        objectLock.lock();
        try {
            for (MapleMapObject l : objects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
            return ret;
        } finally {
            objectLock.unlock();
        }
    }
	
	public List<MaplePortal> getPortals() {
		return portals;
	}

	public void spawnDoor(MapleMagicDoor door) {
		door.setHomeMap(this);
		addMapObject(door, true);
	}

	public void spawnMist(MapleMist mist, int duration, boolean poison, boolean recovery) {
		addMapObject(mist, true);
		
		MapleTask poisonSchedule;
		
		if(poison){
			Runnable task = new Runnable() {
				
				@Override
				public void run() {
					List<MapleMapObject> affectedMonsters = getMapObjectsInBox(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
					
					for(MapleMapObject mo : affectedMonsters){
						if(mist.getEffect().makeChanceResult()){
							MapleMonster monster = (MapleMonster) mo;
							
							MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(mist.getSourceSkill(), mist.getOwner().getSkillLevel(mist.getSourceSkill()));
							
							monster.applyStatusEffect(mist.getOwner(), monsterStatusEffect, duration);
						}
					}
				}
			};
			poisonSchedule = TimerManager.scheduleRepeatingTask(task, 2000, 2500, TimeUnit.MILLISECONDS);
		}else{
			poisonSchedule = null;
		}
		
		TimerManager.schedule(new Runnable() {
			
			@Override
			public void run() {
				removeObject(mist.getObjectId());
				if(poisonSchedule != null){
					poisonSchedule.cancel(false);
				}
			}
		}, duration);
		
		mapTasks.add(poisonSchedule);
		
	}

	public MaplePortal getClosestPortal(Point targetPosition) {
		
		MaplePortal closest = null;
		double distance = 0D;
		
		for(MaplePortal portal : getPortals()){
			if(closest == null){
				closest = portal;
				distance = distance(portal.getPosition().x, portal.getPosition().y, targetPosition.x, targetPosition.y);
				continue;
			}
			
			double otherDistance = distance(portal.getPosition().x, portal.getPosition().y, targetPosition.x, targetPosition.y);
			
			if(otherDistance < distance){
				closest = portal;
				distance = otherDistance;
			}
			
		}
		
		return closest;
	}
	
	public static double distance(double x1, double y1, double x2, double y2){
    	double distanceX = Math.pow(x1 - x2, 2);
    	double distanceY = Math.pow(y1 - y2, 2);
    	
    	return Math.sqrt(distanceX + distanceY);
    }

	public MapleCharacter getPlayerById(int oid) {
		for(MapleCharacter chr : getPlayers()){
			if(chr.getObjectId() == oid){
				return chr;
			}
		}
		
		return null;
	}

	public int countObjectsOfType(MapleMapObjectType monster) {
		return (int) objects.values().stream().filter(obj -> obj.getType() == monster).count();
	}

	public List<MapleMapObject> getObjectsWithinRange(Point position, double distance) {
		return getObjects().stream().filter(obj -> obj.distance(position.x, position.y) <= distance).collect(Collectors.toList());
	}

	public boolean containsNPC(int npc) {
		for(MapleMapObject obj : getObjects()){
			if(obj instanceof MapleNPC){
				if(((MapleNPC)obj).getId() == npc){
					return true;
				}
			}
		}
		return false;
	}

	public void spawnReactor(MapleReactor newReactor) {
		addMapObject(newReactor, true);
	}

	public MaplePortal getPortal(int pid) {
		for(MaplePortal p : portals){
			if(p.getId() == pid){
				return p;
			}
		}
		return null;
	}
	
}
