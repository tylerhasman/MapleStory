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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import constants.MapleElement;
import constants.ElementalEffectiveness;
import maplestory.map.AbstractLoadedMapleLife;
import maplestory.server.MapleStory;
import maplestory.util.Pair;
import maplestory.util.StringUtil;
import me.tyler.mdf.Node;

public class MapleLifeFactory {

	private static Node mobStringData = getStringDataRoot().readNode("Mob.img");
	private static Node npcStringData = getStringDataRoot().readNode("Npc.img");
	private static Map<Integer, MapleMonsterStats> monsterStats = new HashMap<>();

	private static Node getStringDataRoot(){
    	return MapleStory.getDataFile("String.mdf").getRootNode();
    }
    
    private static Node getMobData(){
    	return MapleStory.getDataFile("Mob.mdf").getRootNode();
    }
    
    private static Node getNpcData(){
    	return MapleStory.getDataFile("Npc.mdf").getRootNode();
    }
    
    public static AbstractLoadedMapleLife getLife(int id, String type) {
        if (type.equalsIgnoreCase("n")) {
            return getNPC(id);
        } else if (type.equalsIgnoreCase("m")) {
        	return getMonster(id);
        } else {
        	MapleStory.getLogger().warn("Unknown life type '"+type+"'");
            return null;
        }
    }

    public static MapleMonster getMonster(int mid) {
        MapleMonsterStats stats = monsterStats.get(mid);
        if (stats == null) {
            Node monsterData = getMobData().readNode(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
            if (monsterData == null) {
                return null;
            }
            Node monsterInfoData = monsterData.getChild("info");
            stats = new MapleMonsterStats();
            stats.setHp(monsterInfoData.readInt("maxHP"));
            stats.setMp(monsterInfoData.readInt("maxMP"));
            stats.setPADamage(monsterInfoData.readInt("PADamage"));
            stats.setPDDamage(monsterInfoData.readInt("PDDamage"));
            stats.setMADamage(monsterInfoData.readInt("MADamage"));
            stats.setMDDamage(monsterInfoData.readInt("MDDamage"));
            stats.setExp(monsterInfoData.readInt("exp"));
            stats.setLevel(monsterInfoData.readInt("level"));
            stats.setRemoveAfter(monsterInfoData.readInt("removeAfter"));
            stats.setBoss(monsterInfoData.readInt("boss") > 0);
            stats.setExplosiveReward(monsterInfoData.readInt("explosiveReward") > 0);
            stats.setFfaLoot(monsterInfoData.readInt("publicReward") > 0);
            stats.setUndead(monsterInfoData.readInt("undead") > 0);
            stats.setName(mobStringData.readString(mid + "/name", "No Name"));
            stats.setBuffToGive(monsterInfoData.readInt("buff", -1));
            stats.setCP(monsterInfoData.readInt("getCP"));
            stats.setRemoveOnMiss(monsterInfoData.readInt("removeOnMiss") > 0);

            Node special = monsterInfoData.getChild("coolDamage");
            if (special != null) {
                int coolDmg = monsterInfoData.readInt("coolDamage");
                int coolProb = monsterInfoData.readInt("coolDamageProb");
                stats.setCoolDamage(new Pair<>(coolDmg, coolProb));
            }
            special = monsterInfoData.getChild("loseItem");
            if (special != null) {
                for (Node liData : special.getChildren()) {
                    stats.addLoseItem(new LoseItem(liData.readInt("id"), liData.readByte("prop"), liData.readByte("x")));
                }
            }
            special = monsterInfoData.getChild("selfDestruction");
            if (special != null) {
                stats.setSelfDestruction(new SelfDestruction(special.readByte("action"), special.readInt("removeAfter", -1), special.readInt("hp", -1)));
            }
            int firstAttack = monsterInfoData.readInt("firstAttack");
            stats.setFirstAttack(firstAttack > 0);
            stats.setDropPeriod(monsterInfoData.readInt("dropItemPeriod") * 10000);
            
            stats.setTagColor(monsterInfoData.readInt("hpTagColor"));
            stats.setTagBgColor(monsterInfoData.readInt("hpTagBgColor"));

            for (Node idata : monsterData) {
                if (!idata.getName().equals("info")) {
                    int delay = 0;
                    for (Node pic : idata.getChildren()) {
                        delay += pic.readInt("delay");
                    }
                    stats.setAnimationTime(idata.getName(), delay);
                }
            }
            Node reviveInfo = monsterInfoData.getChild("revive");
            if (reviveInfo != null) {
                List<Integer> revives = new LinkedList<>();
                for (Node data_ : reviveInfo) {
                    revives.add(data_.intValue());
                }
                stats.setRevives(revives);
            }
            decodeElementalString(stats, monsterInfoData.readString("elemAttre", ""));
            Node monsterSkillData = monsterInfoData.getChild("skill");
            if (monsterSkillData != null) {
                int i = 0;
                List<Pair<Integer, Integer>> skills = new ArrayList<>();
                while (monsterSkillData.getChild(Integer.toString(i)) != null) {
                    skills.add(new Pair<>(monsterSkillData.readInt(i+"/skill"), monsterSkillData.readInt(i+"/level")));
                    i++;
                }
                stats.setSkills(skills);
            }
            Node banishData = monsterInfoData.getChild("ban");
            if (banishData != null) {
                stats.setBanishInfo(new BanishInfo(banishData.readString("banMsg"), banishData.readInt("banMap/0/field", -1), banishData.readString("banMap/0/portal", "sp")));
            }
            monsterStats.put(Integer.valueOf(mid), stats);
        }
        MapleMonster ret = new MapleMonster(mid, stats);
        return ret;
    }
    
    private static void decodeElementalString(MapleMonsterStats stats, String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(MapleElement.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
        }
    }
    
    public static MapleNPC getNPC(int nid) {
    	
    	Node speakData = getNpcData().readNode(StringUtil.getLeftPaddedStr(String.valueOf(nid), '0', 7)+".img");
    	
    	boolean hasClientSideScript = searchAllChildren(speakData, "speak") != null;
    	
        return new MapleNPC(nid, hasClientSideScript, getNpcName(nid));
    }
    
    public static String getNpcName(int nid){
    	String name = npcStringData.readString(nid + "/name", "No Name");
    	
    	return name;
    }
    
    private static Node searchAllChildren(Node data, String name){
    	if(data == null){
    		return null;
    	}
    	for(Node d : data.getChildren()){
    		if(d.getName().equalsIgnoreCase(name)){
    			return d;
    		}else{
    			if(d.getChildren() != null && d.getChildren().size() > 0){
    				Node result = searchAllChildren(d, name);
    				if(result != null){
    					return result;
    				}
    			}
    		}
    	}
    	return null;
    }

    public static class BanishInfo {

        private int map;
        private String portal, msg;

        public BanishInfo(String msg, int map, String portal) {
            this.msg = msg;
            this.map = map;
            this.portal = portal;
        }

        public int getMap() {
            return map;
        }

        public String getPortal() {
            return portal;
        }

        public String getMsg() {
            return msg;
        }
    }

    public static class LoseItem {

        private int id;
        private byte chance, x;

        private LoseItem(int id, byte chance, byte x) {
            this.id = id;
            this.chance = chance;
            this.x = x;
        }

        public int getId() {
            return id;
        }

        public byte getChance() {
            return chance;
        }

        public byte getX() {
            return x;
        }
    }

    public static class SelfDestruction {

        private byte action;
        private int removeAfter;
        private int hp;

        private SelfDestruction(byte action, int removeAfter, int hp) {
            this.action = action;
            this.removeAfter = removeAfter;
            this.hp = hp;
        }

        public int getHp() {
            return hp;
        }
        
        public byte getAction() {
            return action;
        }

        public int removeAfter() {
            return removeAfter;
        }
    }

}
