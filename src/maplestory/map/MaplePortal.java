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
package maplestory.map;

import java.awt.Point;

import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;

public interface MaplePortal extends Comparable<MaplePortal> {
    public final int MAP_PORTAL = 2;
    public final int DOOR_PORTAL = 6;
    public static boolean OPEN = true;
    public static boolean CLOSED = false;
    int getType();
    int getId();
    Point getPosition();
    String getName();
    String getTarget();
    String getScriptName();
    void setScriptName(String newName);
    void setPortalStatus(boolean newStatus);
    boolean getPortalStatus();
    int getTargetMapId();
    void enterPortal(MapleClient c);
    void setPortalState(boolean state);
    boolean getPortalState();
    void blockUsage(MapleCharacter chr);
    void unblockUsage(MapleCharacter chr);
    boolean isBlocked(MapleCharacter chr);
    
    @Override
    public default int compareTo(MaplePortal o){
    	if (getId() < o.getId()) {
            return -1;
        } else if (getId() == o.getId()) {
            return 0;
        } else {
            return 1;
        }
    }
   
}
