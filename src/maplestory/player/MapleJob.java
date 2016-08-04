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
package maplestory.player;

import lombok.Getter;

public enum MapleJob {
    BEGINNER(0),

    WARRIOR(100),
    FIGHTER(110), CRUSADER(111), HERO(112),
    PAGE(120), WHITEKNIGHT(121), PALADIN(122),
    SPEARMAN(130), DRAGONKNIGHT(131), DARKKNIGHT(132),

    MAGICIAN(200),
    FP_WIZARD(210, "Fire/Poision Wizard"), FP_MAGE(211, "Fire/Poision Mage"), FP_ARCHMAGE(212, "Fire/Poision Archmage"),
    IL_WIZARD(220, "Ice/Lightning Wizard"), IL_MAGE(221, "Ice/Lightning Mage"), IL_ARCHMAGE(222, "Ice/Lightning Archmage"),
    CLERIC(230), PRIEST(231), BISHOP(232),

    BOWMAN(300),
    HUNTER(310), RANGER(311), BOWMASTER(312),
    CROSSBOWMAN(320), SNIPER(321), MARKSMAN(322),

    THIEF(400),
    ASSASSIN(410), HERMIT(411), NIGHTLORD(412),
    BANDIT(420), CHIEFBANDIT(421, "Chief Bandit"), SHADOWER(422),

    PIRATE(500),
    BRAWLER(510), MARAUDER(511), BUCCANEER(512),
    GUNSLINGER(520), OUTLAW(521), CORSAIR(522),

    MAPLELEAF_BRIGADIER(800, "Mapleleaf Brigadier"),
    GM(900, "GM"), SUPERGM(910, "Super GM"),

    NOBLESSE(1000),
    DAWNWARRIOR1(1100, "Dawn Warrior I"), DAWNWARRIOR2(1110, "Dawn Warrior II"), DAWNWARRIOR3(1111, "Dawn Warrior III"), DAWNWARRIOR4(1112, "Dawn Warrior IV"),
    BLAZEWIZARD1(1200, "Blaze Wizard I"), BLAZEWIZARD2(1210, "Blaze Wizard II"), BLAZEWIZARD3(1211, "Blaze Wizard III"), BLAZEWIZARD4(1212, "Blaze Wizard IV"),
    WINDARCHER1(1300, "Wind Archer I"), WINDARCHER2(1310, "Wind Archer II"), WINDARCHER3(1311, "Wind Archer II"), WINDARCHER4(1312, "Wind Archer IV"),
    NIGHTWALKER1(1400, "Night  Walker I"), NIGHTWALKER2(1410, "Night  Walker II"), NIGHTWALKER3(1411, "Night  Walker III"), NIGHTWALKER4(1412, "Night  Walker IV"),
    THUNDERBREAKER1(1500, "Thunder Breaker I"), THUNDERBREAKER2(1510, "Thunder Breaker II"), THUNDERBREAKER3(1511, "Thunder Breaker III"), THUNDERBREAKER4(1512, "Thunder Breaker IV"),

    LEGEND(2000),
    ARAN1(2100, "Aran I"),ARAN2(2110, "Aran II"), ARAN3(2111, "Aran III"), ARAN4(2112, "IV");

   static{
    	BEGINNER.setNext(WARRIOR, MAGICIAN, THIEF, BOWMAN, PIRATE);
    	//Warrior
    	{
        	WARRIOR.setNext(FIGHTER, PAGE, SPEARMAN);
        	
        	FIGHTER.setNext(CRUSADER);
        	CRUSADER.setNext(HERO);	
        	
        	PAGE.setNext(WHITEKNIGHT);
        	WHITEKNIGHT.setNext(PALADIN);
        	
        	SPEARMAN.setNext(DRAGONKNIGHT);
        	DRAGONKNIGHT.setNext(DARKKNIGHT);
    	}
    	
    	//Magician
    	{
    		MAGICIAN.setNext(FP_WIZARD, IL_WIZARD, CLERIC);
    		
    		FP_WIZARD.setNext(FP_MAGE);
    		FP_MAGE.setNext(FP_ARCHMAGE);
    		
    		IL_WIZARD.setNext(IL_MAGE);
    		IL_MAGE.setNext(IL_ARCHMAGE);
    		
    		CLERIC.setNext(PRIEST);
    		PRIEST.setNext(BISHOP);
    	}
    	
    	//Archer
    	{
    		BOWMAN.setNext(HUNTER, CROSSBOWMAN);
    		
    		HUNTER.setNext(RANGER);
    		RANGER.setNext(BOWMASTER);
    		
    		CROSSBOWMAN.setNext(SNIPER);
    		SNIPER.setNext(MARKSMAN);
    		BOWMAN.setConsumableUsedOnAttackForAll(true);
    		
    	}
    	
    	//Thief
    	{
    		THIEF.setNext(ASSASSIN, BANDIT);
    		
    		ASSASSIN.setNext(HERMIT);
    		HERMIT.setNext(NIGHTLORD);
    		
    		BANDIT.setNext(CHIEFBANDIT);
    		CHIEFBANDIT.setNext(SHADOWER);
    		THIEF.setConsumableUsedOnAttackForAll(true);
    	}
    	
    	//Pirate
    	{
    		PIRATE.setNext(GUNSLINGER, BRAWLER);
    		
    		BRAWLER.setNext(MARAUDER);
    		MARAUDER.setNext(BUCCANEER);
    		
    		GUNSLINGER.setNext(OUTLAW);
    		OUTLAW.setNext(CORSAIR);
    		PIRATE.setConsumableUsedOnAttackForAll(true);
    	}
    	
    	NOBLESSE.setNext(DAWNWARRIOR1, BLAZEWIZARD1, WINDARCHER1, NIGHTWALKER1, THUNDERBREAKER1);
    	
    	DAWNWARRIOR1.setNext(DAWNWARRIOR2);
    	DAWNWARRIOR2.setNext(DAWNWARRIOR3);
    	DAWNWARRIOR3.setNext(DAWNWARRIOR4);
    	
    	BLAZEWIZARD1.setNext(BLAZEWIZARD2);
    	BLAZEWIZARD2.setNext(BLAZEWIZARD3);
    	BLAZEWIZARD3.setNext(BLAZEWIZARD4);
    	
    	WINDARCHER1.setNext(WINDARCHER2);
    	WINDARCHER2.setNext(WINDARCHER3);
    	WINDARCHER3.setNext(WINDARCHER4);
    	WINDARCHER1.setConsumableUsedOnAttackForAll(true);
    	
    	NIGHTWALKER1.setNext(NIGHTWALKER2);
    	NIGHTWALKER2.setNext(NIGHTWALKER3);
    	NIGHTWALKER3.setNext(NIGHTWALKER4);
    	NIGHTWALKER1.setConsumableUsedOnAttackForAll(true);
    	
    	THUNDERBREAKER1.setNext(THUNDERBREAKER2);
    	THUNDERBREAKER2.setNext(THUNDERBREAKER3);
    	THUNDERBREAKER3.setNext(THUNDERBREAKER4);
    	
    	LEGEND.setNext(ARAN1);
    	ARAN1.setNext(ARAN2);
    	ARAN2.setNext(ARAN3);
    	ARAN3.setNext(ARAN4);
    	
    }
    
    private final int jobid;
    private final String name;
    @Getter
    private MapleJob[] next;
    @Getter
    private MapleJob previous = null;

    @Getter
    private boolean consumableUsedOnAttack;
    
    private MapleJob(int id, String name) {
        jobid = id;
        this.name = name;
        consumableUsedOnAttack = false;
    }
    
    private void setConsumableUsedOnAttackForAll(boolean flag) {
		consumableUsedOnAttack = flag;
		
		if(next != null){
			for(MapleJob next : getNext()){
				next.setConsumableUsedOnAttackForAll(flag);
			}
		}
		
	}

	private MapleJob(int id){
    	jobid = id;
    	String n = name();
    	name = Character.toUpperCase(n.charAt(0))+n.substring(1).toLowerCase();
    	consumableUsedOnAttack = false;
    }
    
    public int getLevelRequirement(){
    	
    	int parents = countParents();
    	
    	return requirement(parents);
    	
    }
    
    public boolean isBeginnerJob(){
    	return this == BEGINNER || this == NOBLESSE || this == LEGEND;
    }
    
    private int requirement(int parents){
    	if(parents == 0){
    		return 0;
    	}else if(parents == 1){
    		return this == MAGICIAN ? 8 : 10;
    	}else if(parents == 2){
    		return 30;
    	}else if(parents == 3){
    		return 70;
    	}else if(parents == 4){
    		return 120;
    	}else{
    		throw new IllegalArgumentException(String.valueOf(parents));
    	}
    }
    
    private int countParents(){
    	return previous == null ? 0 : 1 + previous.countParents();
    }
    
    private void setNext(MapleJob... next){
    	this.next = next;
    	for(MapleJob j : next){
    		j.setPrev(this);
    	}
    }
    
    private void setPrev(MapleJob prev){
    	this.previous = prev;
    }

    public int getId() {
        return jobid;
    }
    
    public String getName(){
    	return name;
    }

    public static MapleJob getById(int id) {
        for (MapleJob l : MapleJob.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    public static MapleJob getBy5ByteEncoding(int encoded) {
        switch (encoded) {
            case 2:
                return WARRIOR;
            case 4:
                return MAGICIAN;
            case 8:
                return BOWMAN;
            case 16:
                return THIEF;
            case 32:
                return PIRATE;
            case 1024:
                return NOBLESSE;
            case 2048:
                return DAWNWARRIOR1;
            case 4096:
                return BLAZEWIZARD1;
            case 8192:
                return WINDARCHER1;
            case 16384:
                return NIGHTWALKER1;
            case 32768:
                return THUNDERBREAKER1;
            default:
                return BEGINNER;
        }
    }

    public boolean isA(MapleJob basejob) {        
        return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
    }
}
