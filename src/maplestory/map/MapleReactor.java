package maplestory.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import tools.TimerManager;
import lombok.Data;
import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.script.MapleScript;
import maplestory.script.MapleScriptInstance;
import maplestory.script.ReactorScriptManager;
import maplestory.server.MapleStory;
import maplestory.server.net.PacketFactory;
import maplestory.util.StringUtil;
import me.tyler.mdf.Node;

public class MapleReactor extends AbstractMapleMapObject {
	
	@Getter
	private int id;
	
	@Getter
	private ReactorData reactorData;
	
	private WeakReference<MapleMap> map;
	
	private long nextAllowedHit;
	
	public MapleReactor(int id, ReactorData reactorData, MapleMap map) {
		this.reactorData = reactorData;
		this.id = id;
		this.reactorData.stages = loadReactorStages(id);
		this.reactorData.action = getAction(id);
		this.map = new WeakReference<MapleMap>(map);
		nextAllowedHit = 0;
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
		client.sendPacket(PacketFactory.spawnReactor(this));
	}
	
	@Override
	public void sendDestroyData(MapleClient client) {
		client.sendPacket(PacketFactory.destroyReactor(this));
	}
	
	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.REACTOR;
	}

	public void hitReactor(){
		hitReactor(null, 0);
	}
	
	public void changeState(int state){
		reactorData.state = state;
		getMap().broadcastPacket(PacketFactory.triggerReactor(this, 0));
	}
	
	public void hitReactor(MapleCharacter chr, int stance){
		if(reactorData.isDestroyed()){
			return;
		}
		if(System.currentTimeMillis() - nextAllowedHit < 0){
			return;//Hitting it too fast
		}
		
		if(reactorData.stages.size() > 0){
			nextAllowedHit = System.currentTimeMillis() + reactorData.getDelay();
			
			boolean stateIncresed = reactorData.nextState();
			boolean finalStage = reactorData.isFinalStage();
			
			if(finalStage){
				TimerManager.schedule(() -> destroy(chr), Math.max(reactorData.getDelay(), reactorData.getNonZeroDelay()));
				reactorData.setDestroyed(true);
			}else if(stateIncresed){
				getMap().broadcastPacket(PacketFactory.triggerReactor(this, stance));
			}
			
		}else{
			TimerManager.schedule(() -> destroy(chr), 500);
			reactorData.setDestroyed(true);
		}
	}
	
	public void destroy(){
		destroy(null);
	}
	
	public void destroy(MapleCharacter chr){

		if(getMap() != null){
			getMap().removeObject(getObjectId());
			
			if(reactorData.getRespawnDelay() >= 0){
				TimerManager.schedule(new Runnable() {
					
					@Override
					public void run() {
						MapleMap map = getMap();
						if(map != null){
							reactorData.reset();
							map.spawnReactor(MapleReactor.this);
						}
					}
					
				}, reactorData.getRespawnDelay());
			}
		}
		
		
		MapleScript script = new MapleScript("scripts/reactor/"+reactorData.action+".js", "scripts/reactor/missing_script.js");
		
		SimpleBindings bindings = new SimpleBindings();
		bindings.put("rm", new ReactorScriptManager(chr, this));
		
		try {
			MapleScriptInstance inst = script.execute(bindings);
			
			inst.reactorDestroy();
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
	}
	
	private Rectangle getBoundingBox(){
		ReactorStateStage stage = reactorData.getCurrentStage();
		
		if(stage.rightBound == null || stage.leftBound == null){
			return null;
		}
		
		Rectangle rect = new Rectangle(getPosition());
		rect.x = rect.x + stage.leftBound.x;
		rect.y = rect.y + stage.leftBound.y;
		
		rect.width = stage.rightBound.x - stage.leftBound.x;
		rect.height = stage.rightBound.y - stage.leftBound.y;

		return rect;
	}
	
	public void checkStageRequirements() {
		ReactorStateStage stage = reactorData.getCurrentStage();
		if (stage.itemId != 0) {
			if (getBoundingBox() != null) {

				for (MapleMapObject obj : getMap().getObjects()) {
					if (obj instanceof MapleMapItem) {
						MapleMapItem item = (MapleMapItem) obj;
						if (!item.isMesoDrop()) {
							if (item.getItemId() == stage.itemId) {
								if (getBoundingBox().contains(item.getPosition())) {
									boolean nextStage = reactorData.nextState();
									
									if (reactorData.isFinalStage()){
										item.destroy();
										getMap().executeMapScript(null, "onReactorActivated", this);
										//destroy();
									}
									
									if(nextStage){
										getMap().broadcastPacket(PacketFactory.triggerReactor(this, 0));
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public MapleMap getMap(){
		return map.get();
	}
	
	@Data
	public static class ReactorData {
		
		private final int respawnDelay;
		private final String name;
		private int state;
		private Map<Integer, ReactorStateStage> stages;
		private String action;
		private boolean destroyed;
		
		public ReactorData(int respawnDelay, String name) {
			this.respawnDelay = respawnDelay;
			this.name = name;
			state = 0;
			destroyed = false;
		}
		
		public int getNonZeroDelay() {
			for(int i : stages.keySet()){
				if(getDelay(i) > 0){
					return getDelay(i);
				}
			}
			return 0;
		}

		public int getDelay(int i) {
			return stages.get(i).totalDelay;
		}

		public ReactorStateStage getCurrentStage(){
			return stages.get(state);
		}
		
		public ReactorStateStage getNextStage(){
			int next = getCurrentStage().nextStage;
			
			if(next < 0){
				return null;
			}
			
			return stages.get(next);
		}
		
		public int getDelay() {
			return getDelay(state);
		}

		public void reset() {
			state = 0;
			destroyed = false;
		}

		/**
		 * Progress the data to the next state
		 * @return true if the state was progressed, false if there are no more states left (destroyed)
		 */
		public boolean nextState(){
			
			if(isFinalStage()){
				return false;
			}
			
			state = getCurrentStage().nextStage;
			
			return true;
		}
		
		public boolean isFinalStage(){
			return getNextStage() == null;
		}
		
	}
	
	public static class ReactorStateStage {
		
		@Getter
		private int nextStage;
		
		@Getter
		private ReactorStageType type;
		
		@Getter
		private int totalDelay;
		
		@Getter
		private Point leftBound, rightBound;

		@Getter
		public int itemId;
		
		public boolean isFinalStage(){
			return nextStage == -1;
		}
		
		@Override
		public String toString() {
			return "{"+nextStage+", "+type+"}";
		}
		
	}
	
	public static enum ReactorStageType {
		
		HIT,
		WAIT_FOR_ITEM,
		UNKNOWN;
		
	}
	
	private static Node getReactorRootNode(int reactorId){
		String id = String.valueOf(reactorId);
		
		id = StringUtil.getLeftPaddedStr(id, '0', 7);
		
		Node data = MapleStory.getDataFile("Reactor.mdf").getRootNode().readNode(id+"img");
		
		return data;	
	}
	
	public static String getAction(int reactorId){
		
		Node data = getReactorRootNode(reactorId);
		
		Node action = data.readNode("action");
		if(action != null){
			return action.stringValue();
		}
		
		return null;
	}
	
	public static Map<Integer, ReactorStateStage> loadReactorStages(int reactorId){
		
		Map<Integer, ReactorStateStage> stages = new HashMap<>();
		
		Node data = getReactorRootNode(reactorId);
		
		String id;
		
		if(data.readNode("info") != null){
			Node info = data.readNode("info");
			
			if(info.readNode("link") != null){
				data = getReactorRootNode(info.readInt("link"));
			}
		}
		
		Node stage = null;
		int i = 0;
		
		while((stage = data.readNode(String.valueOf(i))) != null){
			
			ReactorStateStage newStage = new ReactorStateStage();
			Node event = stage.readNode("event");
			
			if(event != null){
				
				for(Node eventData : event){
					
					newStage.nextStage = eventData.readInt("state", -1);
					
					int type = eventData.readInt("type", -1);
					
					if(type == 0){
						newStage.type = ReactorStageType.HIT;
					}else if(type == 100){
						newStage.type = ReactorStageType.WAIT_FOR_ITEM;
						
						int itemId = eventData.readInt("0");
						Point leftBound = eventData.readVector("lt").toPoint();
						Point rightBound = eventData.readVector("rb").toPoint();
						
						newStage.leftBound = leftBound;
						newStage.rightBound = rightBound;
						newStage.itemId = itemId;
						
					}else{
						newStage.type = ReactorStageType.UNKNOWN;
					}
					
				}
				
			}else{
				newStage.nextStage = -1;
			}
			
			Node hit = stage.readNode("hit");
			
			if(hit != null){
				Collection<Node> delayChildren = hit.getChildren();
				
				newStage.totalDelay = 0;
				
				for(Node delayChild : delayChildren){
					newStage.totalDelay += delayChild.readInt("delay", 0);
				}
			}
			
			stages.put(i, newStage);
			i++;
		}
		

		return stages;
	}

	
	
	
}
