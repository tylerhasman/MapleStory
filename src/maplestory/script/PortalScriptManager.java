package maplestory.script;

import constants.PopupInfo;
import lombok.Getter;
import lombok.Setter;
import maplestory.client.MapleClient;
import maplestory.map.MaplePortal;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class PortalScriptManager extends AbstractScriptManager {
	
	@Getter
	private MaplePortal portal;
	
	@Setter @Getter
	private int cooldown;
	
	public PortalScriptManager(MapleCharacter chr, MaplePortal portal) {
		super(chr);
		this.portal = portal;
		cooldown = 1000;
	}

	public void block(){
		portal.blockUsage(getClient().getCharacter());
	}
	
	public void unblock(){
		portal.unblockUsage(getClient().getCharacter());
	}
	
	public void playPortalSound(){
		getClient().sendPacket(PacketFactory.playPortalSound());
	}
	
}
