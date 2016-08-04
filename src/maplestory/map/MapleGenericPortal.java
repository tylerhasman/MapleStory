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
import java.util.ArrayList;
import java.util.List;

import lombok.ToString;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;

@ToString
public class MapleGenericPortal implements MaplePortal {

	
	private List<Integer> blocked;
    private String name;
    private String target;
    private Point position;
    private int targetmap;
    private int type;
    private boolean status = true;
    private int id;
    private String scriptName;
    private boolean portalState;

    public MapleGenericPortal(int type) {
        this.type = type;
        blocked = new ArrayList<>();
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void setPortalStatus(boolean newStatus) {
        this.status = newStatus;
    }

    @Override
    public boolean getPortalStatus() {
        return status;
    }

    @Override
    public int getTargetMapId() {
        return targetmap;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTargetMapId(int targetmapid) {
        this.targetmap = targetmapid;
    }

    @Override
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public void enterPortal(MapleClient c) {
    	MapleMap to = c.getChannel().getMapFactory().getMap(targetmap);
    	if(to == null){
    		c.sendReallowActions();
    		return;
    	}
        MaplePortal pto = to.getPortal(getTarget());
        if (pto == null) {// fallback for missing portals - no real life case anymore - intresting for not implemented areas
            pto = to.getFallbackPortal();
        }
        c.getCharacter().changeMap(to, pto); //late resolving makes this harder but prevents us from loading the whole world at once
    }

    @Override
    public void setPortalState(boolean state) {
        this.portalState = state;
    }

    @Override
    public boolean getPortalState() {
        return portalState;
    }

	@Override
	public int compareTo(MaplePortal arg0) {
		return 0;
	}

	@Override
	public void blockUsage(MapleCharacter chr) {
		if(isBlocked(chr)){
			return;
		}
		blocked.add(chr.getId());
	}

	@Override
	public void unblockUsage(MapleCharacter chr) {
		if(!isBlocked(chr)){
			return;
		}
		int indexOf = blocked.indexOf(chr.getId());
		blocked.remove(indexOf);
	}

	@Override
	public boolean isBlocked(MapleCharacter chr) {
		return blocked.contains(chr.getId());
	}
}
