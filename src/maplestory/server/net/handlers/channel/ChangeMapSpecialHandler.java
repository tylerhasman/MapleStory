package maplestory.server.net.handlers.channel;

import javax.script.SimpleBindings;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.map.MaplePortal;
import maplestory.script.MapleScript;
import maplestory.script.PortalScriptManager;
import maplestory.server.net.MaplePacketHandler;

public class ChangeMapSpecialHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		buf.readByte();
        String startwp = readMapleAsciiString(buf);
        buf.readShort();
        MaplePortal portal = client.getCharacter().getMap().getPortal(startwp);
        if (portal == null) {
            client.sendReallowActions();
            return;
        }
        
        if(portal.isBlocked(client.getCharacter())){
        	client.sendReallowActions();
        	return;
        }
        
        if(client.getCharacter().getMapId() == 910000000){
        	client.getCharacter().changeMap(client.getCharacter().getFmReturnMap());
        }else if(portal.getTargetMapId() == 999999999){
        	MapleScript script = new MapleScript("scripts/portal/"+portal.getScriptName()+".js", "scripts/portal/fallback.js");
        	
        	PortalScriptManager pm = new PortalScriptManager(client.getCharacter(), portal);
        	client.getLogger().debug("Running portal script "+script.getFile().getPath());
        	SimpleBindings sb = new SimpleBindings();
        	sb.put("pm", pm);
        	try {
				script.execute(sb).startPortal();
			} catch (Exception e) {
				e.printStackTrace();
				client.getLogger().error("Error running script "+portal.getScriptName());
			}
        	client.sendReallowActions();
        }else{
        	portal.enterPortal(client); 
        }
        
	}

}
