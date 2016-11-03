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

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import maplestory.server.MapleStory;
import me.tyler.mdf.Node;

/**
 *
 * @author Danny (Leifde)
 */
public class MobSkillFactory {

    private static Map<String, MobSkill> mobSkills = new HashMap<String, MobSkill>();
/*    private final static NodeProvider dataSource = NodeProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Skill.wz"));
    private static Node skillRoot = dataSource.getData("MobSkill.img");*/
    private static ReentrantReadWriteLock dataLock = new ReentrantReadWriteLock();

    private static Node getDataSource(){
    	return MapleStory.getDataFile("Skill.mdf").getRootNode().readNode("MobSkill.img");
    }
    
    public static MobSkill getMobSkill(final int skillId, final int level) {
        final String key = skillId + "" + level;
        dataLock.readLock().lock();
        try {
            MobSkill ret = mobSkills.get(key);
            if (ret != null) {
                return ret;
            }
        } finally {
            dataLock.readLock().unlock();
        }
        dataLock.writeLock().lock();
        try {
            MobSkill ret;
            ret = mobSkills.get(key);
            if (ret == null) {
                Node skillData = getDataSource().getChild(skillId + "/level/" + level);
                if (skillData != null) {
                    int mpCon = skillData.readInt("mpCon", 0);
                    List<Integer> toSummon = new ArrayList<Integer>();
                    for (int i = 0; i > -1; i++) {
                        if (skillData.getChild(String.valueOf(i)) == null) {
                            break;
                        }
                        toSummon.add(skillData.readInt(String.valueOf(i), 0));
                    }
                    int effect = skillData.readInt("summonEffect", 0);
                    int hp = skillData.readInt("hp", 100);
                    int x = skillData.readInt("x", 1);
                    int y = skillData.readInt("y", 1);
                    long duration = skillData.readInt("time", 0) * 1000;
                    long cooltime = skillData.readInt("interval", 0) * 1000;
                    int iprop = skillData.readInt("prop", 100);
                    float prop = iprop / 100;
                    int limit = skillData.readInt("limit", 0);
                    Node ltd = skillData.readNode("lt");
                    Point lt = null;
                    Point rb = null;
                    if (ltd != null) {
                        lt = ltd.vectorValue().toPoint();
                        rb = skillData.readVector("rb").toPoint();
                    }
                    ret = new MobSkill(skillId, level);
                    ret.setSummons(toSummon);
                    ret.setCoolTime(cooltime);
                    ret.setDuration(duration);
                    ret.setHp(hp);
                    ret.setMpCost(mpCon);
                    ret.setSpawnEffect(effect);
                    ret.setX(x);
                    ret.setY(y);
                    ret.setProp(prop);
                    ret.setLimit(limit);
                    ret.setLtRb(lt, rb);
                }
                mobSkills.put(skillId + "" + level, ret);
            }
            return ret;
        } finally {
            dataLock.writeLock().unlock();
        }
    }
}
