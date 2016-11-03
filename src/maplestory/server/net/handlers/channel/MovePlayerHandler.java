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
package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;

import constants.LoginStatus;
import maplestory.client.MapleClient;
import maplestory.life.movement.MovementPath;
import maplestory.server.net.PacketFactory;

public final class MovePlayerHandler extends MovementPacketHandler {
	
    public final void handle(ByteBuf buf, MapleClient client) {
    	if(client.getLoginStatus() != LoginStatus.IN_GAME){
    		return;
    	}
        buf.skipBytes(9);
        MovementPath res = parseMovement(buf);
        if (res != null) {
        	res.translateLife(client.getCharacter());
            client.getCharacter().getMap().broadcastPacket(PacketFactory.movePlayer(client.getCharacter().getId(), res), client.getCharacter().getId());
        }
    }
}
