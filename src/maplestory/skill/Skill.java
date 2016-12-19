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
package maplestory.skill;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import maplestory.player.MapleJob;
import constants.MapleElement;
import constants.skills.Bishop;
import constants.skills.Bowmaster;
import constants.skills.Brawler;
import constants.skills.ChiefBandit;
import constants.skills.Corsair;
import constants.skills.DarkKnight;
import constants.skills.FPArchMage;
import constants.skills.FPMage;
import constants.skills.Gunslinger;
import constants.skills.Hero;
import constants.skills.ILArchMage;
import constants.skills.Marksman;
import constants.skills.NightWalker;
import constants.skills.Paladin;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;

public class Skill {
	@Setter
    private int id;
	
    protected List<MapleStatEffect> effects = new ArrayList<>();
    
    @Setter
    private MapleElement element;
    
    @Setter
    private int animationTime;
    
    @Setter
    private boolean action;

    protected int job;
    
    public Skill(int id) {
        this.id = id;
    }
    
    public MapleJob getJob(){
    	return MapleJob.getById(job);
    }

    public int getId() {
        return id;
    }

    public MapleStatEffect getEffect(int level) {
    	if(level <= 0){
    		throw new IllegalArgumentException("level must be greater than 0");
    	}
        return effects.get(level - 1);
    }

    public int getMaxLevel() {
        return effects.size();
    }

    public boolean isFourthJob() {
        return (id / 10000) % 10 == 2;
    }

    public MapleElement getElement() {
        return element;
    }

    public int getAnimationTime() {
        return animationTime;
    }

    public boolean isBeginnerSkill() {
        return id % 10000000 < 10000;
    }

    public boolean getAction() {
        return action;
    }

	public boolean hasEffect() {
		switch (id) {
        case FPMage.EXPLOSION:
        case FPArchMage.BIG_BANG:
        case ILArchMage.BIG_BANG:
        case Bishop.BIG_BANG:
        case Bowmaster.HURRICANE:
        case Marksman.PIERCING_ARROW:
		case ChiefBandit.CHAKRA:
        case Brawler.CORKSCREW_BLOW:
        case Gunslinger.GRENADE:
        case Corsair.RAPID_FIRE:
        case WindArcher.HURRICANE:
        case NightWalker.POISON_BOMB:
        case ThunderBreaker.CORKSCREW_BLOW:
        case Paladin.MONSTER_MAGNET:
        case DarkKnight.MONSTER_MAGNET:
        case Hero.MONSTER_MAGNET:
            return true;
        default:
            return false;
    }
	}
}