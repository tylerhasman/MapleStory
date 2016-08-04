package maplestory.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import maplestory.client.MapleClient;
import maplestory.inventory.Inventory;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.player.MapleCharacter;
import maplestory.quest.MapleQuestInstance;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.server.MapleServer;
import maplestory.server.net.PacketFactory;

public class MapleMapItem extends AbstractMapleMapObject {

	@Getter @Setter(value=AccessLevel.PACKAGE)
	private int mesos;
	@Getter
	private Item item;
	
	private Point sourcePosition;
	
	private MapleMapObjectType sourceType;
	@Getter
	private DropType dropType;
	@Getter
	private int owner;
	@Getter
	private MapleMap map;
	
	private boolean sendDestroyData;
	
	MapleMapItem(Item item, int owner, Point location, DropType dropType, MapleMap map, MapleMapObject source) {
		this.owner = owner;
		this.dropType = dropType;
		if(source != null){
			this.sourceType = source.getType();
			this.sourcePosition = source.getPosition();	
		}
		this.map = map;
		this.item = item;
		setPosition(location);
		sendDestroyData = true;
	}
	
	MapleMapItem(int mesos, int owner, Point location, DropType dropType, MapleMap map, MapleMapObject source) {
		this(null, owner, location, dropType, map, source);
		this.mesos = mesos;
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
		Point to = map.calcDropPosition(getPosition());

		//Spawns it with no drop effect
		client.sendPacket(PacketFactory.getDropItemPacket(this, to, null, 2));
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		if(sendDestroyData){
			client.sendPacket(PacketFactory.getDeleteDroppedItemPacket(this));
		}
			
	}
	
	public void destroy(){
		map.removeObject(getObjectId());
	}
	
	public boolean isQuestItem(){
		if(isMesoDrop()){
			return false;
		}
		
		return ItemInfoProvider.isQuestItem(getItemId());
	}
	
	public void pickup(MapleCharacter chr){
		boolean success = false;
		if(isMesoDrop()){
			chr.giveMesos(mesos);
			chr.getClient().sendReallowActions();
			success = true;
		}else{
			success = chr.getInventory(item.getItemId()).addItem(item).isSuccess();
		}
		
		if(success){
			map.broadcastPacket(PacketFactory.getPickupDroppedItemPacket(this, chr));
			sendDestroyData = false;
			map.removeObject(getObjectId());
			sendDestroyData = true;
			if(isQuestItem()){
				List<MapleMapItem> sameItemId = new ArrayList<>();
				
				int owned = chr.getItemQuantity(getItemId(), true);
				
				for(MapleMapObject obj : map.getObjects()){
					if(obj instanceof MapleMapItem){
						MapleMapItem dropped = (MapleMapItem) obj;
						if(dropped.getItemId() == getItemId() && dropped.getOwner() == owner){
							sameItemId.add(dropped);
						}
					}
				}
				
				boolean destroyOthers = true;
				
				for(MapleQuestInstance quest : chr.getQuests(MapleQuestStatus.STARTED)){
					if(quest.getQuest().getQuestInfo().getRelevantItems().containsKey(getItemId())){
						int needed = quest.getQuest().getQuestInfo().getRelevantItems().get(getItemId());
						if(owned < needed){
							destroyOthers = false;
							break;
						}
					}
				}
				
				if(destroyOthers){
					sameItemId.forEach(item -> item.destroy());
				}
				
			}
		}else{
			chr.getClient().sendPacket(PacketFactory.getShowInventoryFull());
		}
	}

	public void broadcastDropPacket() {
		map.broadcastPacket(PacketFactory.getDropItemPacket(this, map.calcDropPosition(getPosition()), sourcePosition == null ? getPosition() : sourcePosition, 1));
		map.broadcastPacket(PacketFactory.getDropItemPacket(this, map.calcDropPosition(getPosition()), sourcePosition == null ? getPosition() : sourcePosition, 0));
	}
	
	public boolean isMesoDrop(){
		return mesos > 0;
	}
	
	public int getItemId(){
		return isMesoDrop() ? mesos : item.getItemId();
	}
	
	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.ITEM;
	}
	
	public boolean isPlayerDrop(){
		return sourceType == MapleMapObjectType.PLAYER;
	}
	
	public static MapleMapItem getMesoDrop(int amount, Point location, MapleMap map, int owner, DropType dropType, MapleMapObject source){
		return new MapleMapItem(amount, owner, location, dropType, map, source);
	}
	
	public static MapleMapItem getItemDrop(Item item, Point location, MapleMap map, int owner, DropType dropType, MapleMapObject source){
		return new MapleMapItem(item, owner, location, dropType, map, source);
	}

	public static enum DropType{
		
		OWNER_ONLY(0),
		PARTY_ONLY(1),
		FFA(2),
		EXPLOSIVE_FFA(3)
		;
		
		private final int id;
		
		DropType(int id){
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
	}

}
