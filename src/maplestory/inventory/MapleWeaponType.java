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
package maplestory.inventory;

import constants.skills.Assassin;
import constants.skills.Bandit;
import constants.skills.Brawler;
import constants.skills.Crossbowman;
import constants.skills.DawnWarrior;
import constants.skills.Fighter;
import constants.skills.Gunslinger;
import constants.skills.Hunter;
import constants.skills.NightWalker;
import constants.skills.Page;
import constants.skills.Spearman;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;
import lombok.Getter;
import maplestory.player.MapleCharacter;

public enum MapleWeaponType {
    NOT_A_WEAPON(0),
    AXE1H(4.4, Fighter.AXE_MASTERY),
    AXE2H(4.8, Fighter.AXE_MASTERY),
    BLUNT1H(4.4, Page.BW_MASTERY),
    BLUNT2H(4.8, Page.BW_MASTERY),
    BOW(3.4, true, Hunter.BOW_MASTERY, WindArcher.BOW_MASTERY),
    CLAW(3.6, true, Assassin.CLAW_MASTERY, NightWalker.CLAW_MASTERY),
    CROSSBOW(3.6, true, Crossbowman.CROSSBOW_MASTERY),
    DAGGER(4, Bandit.DAGGER_MASTERY),
    GUN(3.6, true, Gunslinger.GUN_MASTERY),
    KNUCKLE(4.8, Brawler.KNUCKLER_MASTERY, ThunderBreaker.KNUCKLER_MASTERY),
    POLE_ARM(5.0, Spearman.POLEARM_MASTERY),
    SPEAR(5.0, Spearman.SPEAR_MASTERY),
    STAFF(3.6),
    SWORD1H(4.0, Fighter.SWORD_MASTERY, Page.SWORD_MASTERY, DawnWarrior.SWORD_MASTERY),
    SWORD2H(4.6, Fighter.SWORD_MASTERY, Page.SWORD_MASTERY, DawnWarrior.SWORD_MASTERY),
    WAND(3.6);
    
    
    
    private double damageMultiplier;
    
    @Getter
    private boolean consumableUsedOnAttack;
    
    private int[] masterySkills = new int[0];

    private MapleWeaponType(double maxDamageMultiplier, int... mastery) {
    	this(maxDamageMultiplier, false);
    }
    
    private MapleWeaponType(double maxDamageMultiplier, boolean cuoa, int... mastery) {
    	damageMultiplier = maxDamageMultiplier;
    	consumableUsedOnAttack = cuoa;
        this.masterySkills = mastery;
	}

    public double getMaxDamageMultiplier() {
        return damageMultiplier;
    }
    
    public int[] getMasterySkills() {
		return masterySkills;
	}

	public int getMainStat(MapleCharacter mc){
		if(this == BOW || this == CROSSBOW || this == GUN){
			return mc.getDex();
		}else if(this == CLAW || this == DAGGER){
			return mc.getLuk();
		}
		
		return mc.getStr();
	}
	
	public int getSecondaryStat(MapleCharacter mc){
		if(this == BOW || this == CROSSBOW || this == GUN){
			return mc.getStr();
		}else if(this == CLAW || this == DAGGER){
			return mc.getDex() + mc.getStr();
		}
		
		return mc.getDex();
	}
}
