package maplestory.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;



import tools.TimerManager;
import tools.TimerManager.MapleTask;
import constants.skills.Priest;
import lombok.Getter;
import lombok.Setter;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

/**
 * TODO: This is a pretty shitty and unorginized class, it could use some touch ups...
 * @author user
 *
 */
public class MapleMagicDoor extends AbstractMapleMapObject {

	@Getter
	private MapleCharacter owner;
	@Getter @Setter
	private MapleMap target;
	private MaplePortal townPortal;
	@Getter
	private MapleMap town;
	private Point targetPosition;
	
	@Setter
	private MapleMap homeMap;
	
	private MapleTask destroyTask;

	public MapleMagicDoor(MapleCharacter owner) {
		this.owner = owner;
		this.target = owner.getMap();
		
		Point position = (Point) owner.getPosition().clone();
		position.y = owner.getFh();
		if(position.y == 0){
			position.y = owner.getPosition().y;
		}
		position.y -= 6;
		this.targetPosition = position;
		setPosition(targetPosition);
		this.town = target.getReturnMap();
		this.townPortal = getFreePortal();
		setupDestroyTask();
	}
	
	public MapleMagicDoor(MapleMagicDoor origDoor) {
		this.owner = origDoor.owner;
		this.town = origDoor.town;
		this.townPortal = origDoor.townPortal;
		this.target = origDoor.target;
		this.targetPosition = origDoor.targetPosition;
		this.townPortal = origDoor.townPortal;
		setPosition(townPortal.getPosition());
		setupDestroyTask();
	}
	
	public void setTown(MapleMap town) {
		this.town = town;
		townPortal = getFreePortal();
	}
	
	private void setupDestroyTask(){
		destroyTask = TimerManager.schedule(new Runnable() {
			
			@Override
			public void run() {
				destroy();
			}
		}, getDuration(), TimeUnit.SECONDS);
	}
	
	private int getDuration(){
		return owner.getSkillLevel(Priest.MYSTIC_DOOR) * 20;
	}
	
	private MaplePortal getFreePortal() {
		
		List<MaplePortal> freePortals = new ArrayList<>();
		
		for(MaplePortal port : town.getPortals()){
			if(port.getType() == 6){
				freePortals.add(port);
			}
		}
		
		Collections.sort(freePortals);
		
		if(freePortals.size() == 0){
			return town.getFallbackPortal();
		}
		
		return freePortals.get(0);
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
		//Do something else for parties
		
		if(owner.getId() == client.getCharacter().getId()){
			client.sendPacket(PacketFactory.spawnDoor(owner.getObjectId(), town.getMapId() == client.getCharacter().getMapId() ? townPortal.getPosition() : getPosition(), homeMap.isTown()));
			client.sendPacket(PacketFactory.spawnPortal(town.getMapId(), target.getMapId(), targetPosition));
		}else{
			client.sendPacket(PacketFactory.spawnPartyPortal(town.getMapId(), target.getMapId(), targetPosition));
		}
		
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		if(client.getCharacter().getMapId() == target.getMapId() || client.getCharacter().getMapId() == town.getMapId()){
			client.sendPacket(PacketFactory.removeDoor(owner.getId(), false));
			client.sendPacket(PacketFactory.removeDoor(owner.getId(), true));
		}
	}

	@Override
	public String toString() {
		return "MapleMagicDoor: { homeMap: '"+homeMap.getMapName()+"' town: '"+town.getMapName()+"' }";
	}
	
	public void warp(MapleCharacter chr, boolean toTown){
		
		if(chr.getId() == owner.getId()){
			if(toTown){
				chr.changeMap(town, townPortal);
			}else{
				chr.changeMap(target, target.getClosestPortal(targetPosition));
			}
		}else{
			chr.getClient().sendReallowActions();
		}
		
	}
	
	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.DOOR;
	}
	
	public void destroy() {
		destroyTask.cancel(false);
		
		homeMap.removeObject(getObjectId());
		
		owner.getMagicDoors().remove(this);
	}

}
