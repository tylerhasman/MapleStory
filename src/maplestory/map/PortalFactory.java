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

import me.tyler.mdf.Node;

public class PortalFactory {
    private int nextDoorPortal;

    public PortalFactory() {
        nextDoorPortal = 0x80;
    }

    public MaplePortal makePortal(int type, Node portal) {
        MapleGenericPortal ret = null;
        if (type == MaplePortal.MAP_PORTAL) {
            ret = new MapleMapPortal();
        } else {
            ret = new MapleGenericPortal(type);
        }
        loadPortal(ret, portal);
        return ret;
    }

    private void loadPortal(MapleGenericPortal myPortal, Node portal) {
        myPortal.setName(portal.readString("pn"));
        myPortal.setTarget(portal.readString("tn"));
        myPortal.setTargetMapId(portal.readInt("tm"));
        int x = portal.readInt("x");
        int y = portal.readInt("y");
        myPortal.setPosition(new Point(x, y));
        String script = portal.readString("script");
        if (script != null && script.equals("")) {
            script = null;
        }
        myPortal.setScriptName(script);
        if (myPortal.getType() == MaplePortal.DOOR_PORTAL) {
            myPortal.setId(nextDoorPortal);
            nextDoorPortal++;
        } else {
            myPortal.setId(Integer.parseInt(portal.getName()));
        }
    }
}
