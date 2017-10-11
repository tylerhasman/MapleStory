package maplestory.life;

import constants.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.map.AbstractMapleMapObject;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapObjectType;
import maplestory.player.MapleCharacter;
import maplestory.player.ui.HiredMerchantInterface;
import maplestory.player.ui.MapleHiredMerchantInterface;
import maplestory.server.MapleServer;
import maplestory.server.net.PacketFactory;

public class MapleHiredMerchant extends AbstractMapleMapObject {

	@Getter
	private final int ownerId;
	@Getter
	private final String ownerName;
	@Getter
	private final int merchantType;
	
	@Getter
	private final String description;
	
	private HiredMerchantInterface userInterface;
	
	public  MapleHiredMerchant(MapleCharacter owner, int merchantType, String description) {
		this.ownerId = owner.getId();
		this.ownerName = owner.getName();
		this.merchantType = merchantType;
		this.description = description;
		userInterface = new MapleHiredMerchantInterface(this);
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
		client.sendPacket(PacketFactory.hiredMerchantSpawn(this));
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.sendPacket(PacketFactory.hiredMerchantRemove(this));
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.HIRED_MERCHANT;
	}

	public boolean isOwner(MapleCharacter chr){
		return chr.getId() == ownerId;
	}
	
	public void openFor(MapleCharacter chr){
		if(userInterface.isOpen() || userInterface.isOwner(chr)){
			chr.openInterface(userInterface);
		}else{
			chr.sendMessage(MessageType.POPUP, "That store is not open at the moment.");
		}
	}

	public boolean isOpen() {
		return userInterface.isOpen();
	}

}
