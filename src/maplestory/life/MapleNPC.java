/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package maplestory.life;

import java.io.File;

import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.map.AbstractLoadedMapleLife;
import maplestory.map.MapleMapObjectType;
import maplestory.server.net.PacketFactory;

public class MapleNPC extends AbstractLoadedMapleLife {
	
	@Getter
    private String name;
	
	private boolean hasClientSideScript;

    public MapleNPC(int id, boolean hasClientSideScript, String name) {
        super(id);
        this.name = name;
        this.hasClientSideScript = hasClientSideScript;
    }

/*    public boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }*/

/*    public void sendShop(MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }*/

    @Override
    public void sendSpawnData(MapleClient client) {
    	if (this.getId() > 9010010 && this.getId() < 9010014) {
         	// client.announce(MaplePacketCreator.spawnNPCRequestController(this, false));
         } else {
             client.sendPacket(PacketFactory.spawnNPC(this));
             client.sendPacket(PacketFactory.spawnNPCRequestController(this, true));
             if(hasClientSideScript){
            	 if(new File("scripts/npc/"+getId()+".js").exists()){
                	 client.sendPacket(PacketFactory.setNPCScriptable(getId(), getName())); 
            	 }
             }
         }
    }

    @Override
    public void sendDestroyData(MapleClient client) {

    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.NPC;
    }

	public void startConversation(MapleClient client) {
		client.getCharacter().openNpc(this);
	}

}
