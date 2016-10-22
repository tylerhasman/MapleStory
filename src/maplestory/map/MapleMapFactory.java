
package maplestory.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import maplestory.life.MapleLifeFactory;
import maplestory.life.MapleMonster;
import maplestory.map.MapleReactor.ReactorData;
import maplestory.server.MapleServer;
import maplestory.server.MapleStory;
import maplestory.util.StringUtil;
import me.tyler.mdf.Node;

public class MapleMapFactory {

    private Map<Integer, MapleMap> maps;
    private int channel, world;
    private ReentrantLock mapLock = new ReentrantLock(true);
    
    public MapleMapFactory(int world, int channel) {
        this.world = world;
        this.channel = channel;
        maps = Collections.synchronizedMap(new HashMap<>());
    }
    
    private MapleMap loadMap(int mapid){
    	
    	Node source = MapleStory.getDataFile("Map.mdf").getRootNode();
    	
    	MapleMap map = null;
    	String mapName = getMapId(mapid);
		Node mapData = source.getChild(mapName);
		if (mapData == null)
			return null;
		String link = mapData.readString("info/link", "");
		if (!link.equals("")) { // nexon made hundreds of dojo maps so
								// to reduce the size they added links.
			mapName = getMapId(Integer.parseInt(link));
			mapData = source.getChild(mapName);
		}
		float monsterRate = mapData.readFloat("info/mobRate");

		map = new MapleMap(mapid, world, channel, mapData.readInt("info/returnMap"), monsterRate);
		maps.put(mapid, map);
		// map.setOnFirstUserEnter(NodeTool.getString(mapData.getChild("info/onFirstUserEnter"),
		// String.valueOf(mapid)));
		 map.setGlobalScriptOnUserEnter(mapData.readString("info/onUserEnter", ""));
		// String.valueOf(mapid)));
		map.setFieldLimit(mapData.readInt("info/fieldLimit"));
		map.setMobInterval(mapData.readInt("info/createMoveInterval", 5000));
		PortalFactory portalFactory = new PortalFactory();
		for (Node portal : mapData.getChild("portal")) {
			map.addPortal(portalFactory.makePortal(portal.readInt("pt"), portal));
		}
		/*
		 * Node timeMob = mapData.getChild("info/timeMob");
		 * if (timeMob != null) {
		 * map.timeMob(NodeTool.getInt(timeMob
		 * .getChild("id")),
		 * NodeTool.getString(timeMob.getChild("message")));
		 * }
		 */

		List<MapleFoothold> allFootholds = new LinkedList<>();
		Point lBound = new Point();
		Point uBound = new Point();
		for (Node footRoot : mapData.getChild("foothold")) {
			for (Node footCat : footRoot) {
				for (Node footHold : footCat) {
					int x1 = footHold.readInt("x1");
					int y1 = footHold.readInt("y1");
					int x2 = footHold.readInt("x2");
					int y2 = footHold.readInt("y2");
					MapleFoothold fh = new MapleFoothold(new Point(x1, y1), new Point(x2, y2), Integer.parseInt(footHold.getName()));
					fh.setPrev(footHold.readInt("prev"));
					fh.setNext(footHold.readInt("next"));
					if (fh.getX1() < lBound.x) {
						lBound.x = fh.getX1();
					}
					if (fh.getX2() > uBound.x) {
						uBound.x = fh.getX2();
					}
					if (fh.getY1() < lBound.y) {
						lBound.y = fh.getY1();
					}
					if (fh.getY2() > uBound.y) {
						uBound.y = fh.getY2();
					}
					allFootholds.add(fh);
				}
			}
		}
		MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
		for (MapleFoothold fh : allFootholds) {
			fTree.insert(fh);
		}
		map.setFootholds(fTree);

		if(mapData.hasChild("life")){
			for (Node life : mapData.getChild("life")) {
				String id = life.readString("id");
				String type = life.readString("type");
				if (id.equals("9001105")) {
					id = "9001108";// soz
				}
				AbstractLoadedMapleLife myLife = loadLife(life, id, type);
				if (myLife instanceof MapleMonster) {
					MapleMonster monster = (MapleMonster) myLife;
					int mobTime = life.readInt("mobTime", 0);
					int team = life.readInt("team", -1);
					monster.setMap(map);
					if (mobTime == -1) { // does not respawn, force spawn
											// once
						map.spawnMonster(monster);
					} else {
						map.addMonsterSpawn(monster, mobTime, team);
					}
				} else {
					map.addMapObject(myLife, false);
				}
			}
		}
	
		if (mapData.getChild("reactor") != null) {
			for (Node reactor : mapData.getChild("reactor")) {
				String id = reactor.readString("id");
				if (id != null) {
					MapleReactor newReactor = loadReactor(map, reactor, id);
					map.spawnReactor(newReactor);
				}
			}
		}
		
		Node nameData = getNameData();
		 
		try {
			map.setMapName(getMapName(mapid));
			map.setStreetName(nameData.readString(getMapStringName(mapid)+"/streetName", ""));
		} catch (Exception e) {
			map.setMapName("");
			map.setStreetName("");
		}

		map.setTown(mapData.getChild("town") != null);

		/*
		 * map.setClock(mapData.getChild("clock") != null);
		 * map.setEverlast(mapData.getChild("everlast") != null);
		 * 
		 * map.setHPDec(NodeTool.getIntConvert("decHP", mapData,
		 * 0));
		 * map.setHPDecProtect(NodeTool.getIntConvert("protectItem"
		 * , mapData, 0));
		 * map.setForcedReturnMap(NodeTool.getInt(mapData
		 * .getChild("info/forcedReturn"), 999999999));
		 * map.setBoat(mapData.getChild("shipObj") != null);
		 * map.setTimeLimit(NodeTool.getIntConvert("timeLimit",
		 * mapData.getChild("info"), -1));
		 * map.setFieldType(NodeTool
		 * .getIntConvert("info/fieldType", mapData, 0));
		 * map.setMobCapacity
		 * (NodeTool.getIntConvert("fixedMobCapacity",
		 * mapData.getChild("info"), 500));//Is there a map that
		 * contains more than 500 mobs?
		 */
		 return map;
	}
    
	private static Node getNameData() {
		return MapleStory.getDataFile("String.mdf").getRootNode().readNode("Map.img");
	}

	public MapleMap getMap(int mapid) {
		mapLock.lock();
		try {
			if (!isMapLoaded(mapid)) {
				MapleServer.getChannel(world, channel).getLogger().debug("Loading map " + mapid+" ("+getMapName(mapid)+")");
			}
			MapleMap map = maps.get(mapid);
			
			if (map == null) {
				map = loadMap(mapid);
			}
			
			return map;
		} finally {
			mapLock.unlock();
		}

	}

    public boolean isMapLoaded(int mapId) {
    	mapLock.lock();
    	try{
    		  return maps.containsKey(mapId);
    	}finally{
    		mapLock.unlock();
    	}
    }
    
    public List<Integer> findMap(String query){
    	
    	query = query.toLowerCase();
    	
    	List<Integer> maps = new ArrayList<>();
    	
    	for(Node child : getNameData().getChildren()){
    		for(Node map : child){
    			int mapId = Integer.valueOf(map.getName());
    			String name = map.readString("mapName");
    			if(name == null){
    				continue;
    			}
    			
    			if(name.toLowerCase().contains(query)){
    				maps.add(mapId);
    			}
    		}
    	}
    	
    	return maps;
    	
    }
    
    public String getMapName(int id){
    	Node data = getNameData().getChild(getMapStringName(id));
    	if(data == null){
    		return String.valueOf(id);
    	}
    	return data.readString("mapName");
    }
    
    private AbstractLoadedMapleLife loadLife(Node life, String id, String type) {
        AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(Integer.parseInt(id), type);

        myLife.setCy(life.readInt("cy"));
        if(life.hasChild("f")){
            myLife.setF(life.readInt("f"));	
        }
        myLife.setFh(life.readInt("fh"));
        myLife.setRx0(life.readInt("rx0"));
        myLife.setRx1(life.readInt("rx1"));
        int x = life.readInt("x");
        int y = life.readInt("y");
        myLife.setPosition(new Point(x, y));
        int hide = life.readInt("hide", 0);
        if (hide == 1) {
            myLife.setHide(true);
        }
        return myLife;
    }

    private MapleReactor loadReactor(MapleMap map, Node reactor, String id) {
    	
    	int delay = reactor.readInt("reactorTime") * 1000;
    	String name = reactor.readString("name", "");
    	
        MapleReactor myReactor = new MapleReactor(Integer.parseInt(id), new ReactorData(delay, name), map);
        int x = reactor.readInt("x");
        int y = reactor.readInt("y");
        myReactor.setPosition(new Point(x, y));

        return myReactor;
    }

    private String getMapId(int mapid) {
        String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
        StringBuilder builder = new StringBuilder("Map/Map");
        int area = mapid / 100000000;
        builder.append(area);
        builder.append("/");
        builder.append(mapName);
        builder.append(".img");
        mapName = builder.toString();
        return mapName;
    }

    private static String getMapStringName(int mapid) {
        StringBuilder builder = new StringBuilder();
        if (mapid < 100000000) {
            builder.append("maple");
        } else if (mapid >= 100000000 && mapid < 200000000) {
            builder.append("victoria");
        } else if (mapid >= 200000000 && mapid < 300000000) {
            builder.append("ossyria");
        } else if (mapid >= 540000000 && mapid < 541010110) {
            builder.append("singapore");
        } else if (mapid >= 600000000 && mapid < 620000000) {
            builder.append("MasteriaGL");
        } else if (mapid >= 670000000 && mapid < 682000000) {
            builder.append("weddingGL");
        } else if (mapid >= 682000000 && mapid < 683000000) {
            builder.append("HalloweenGL");
        } else if (mapid >= 800000000 && mapid < 900000000) {
            builder.append("jp");
        } else {
            builder.append("etc");
        }
        builder.append("/").append(mapid);
        return builder.toString();
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public void setWorld(int world) {
        this.channel = world;
    }

    public Collection<MapleMap> getLoadedMaps(){
    	mapLock.lock();
    	try{
    		return new ArrayList<>(maps.values());
    	}finally{
    		mapLock.unlock();
    	}
    }
    
    public void unloadDeadMaps(){
    	mapLock.lock();
    	try{
    		//MapleServer.getChannel(world, channel).getLogger().debug("Loaded Maps: "+getLoadedMaps().size());
    		
    		for(MapleMap map : getLoadedMaps()){
    			if(map == null){
    				continue;
    			}
    			if(map.isReadyToUnload()){
    				unloadMap(map.getMapId());
    			}
    		}
    		
    	}finally{
    		mapLock.unlock();
    	}
    }

	public void unloadMap(int mapId) {
		mapLock.lock();
		try{
			
			if(isMapLoaded(mapId)){
				MapleMap map = maps.remove(mapId);
				
				if(map == null){
					MapleServer.getChannel(world, channel).getLogger().warn("Map not removed properly! "+mapId);
					return;
				}
				
				map.unload();
			}	
		}finally{
			mapLock.unlock();
		}
	}

}
