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

import constants.GameConstants;
import constants.MonsterStatus;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.ToString;
import maplestory.life.MapleMonster;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.MonsterStatusEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;
import constants.skills.*;

public abstract class AbstractDealDamageHandler extends MaplePacketHandler {

    public static class AttackInfo {

        public int numAttacked, numDamage, numAttackedAndDamage, skill, skilllevel, stance, direction, rangedirection, charge, display;
        public Map<Integer, List<Integer>> allDamage;
        public boolean isHH = false;
        public int speed = 4;

        public MapleStatEffect getAttackEffect(MapleCharacter chr, Skill theSkill) {
            Skill mySkill = theSkill;
            if (mySkill == null) {
                mySkill = SkillFactory.getSkill(GameConstants.getHiddenSkill(skill));
            }
            int skillLevel = chr.getSkillLevel(mySkill);
            /*if (mySkill.getId() % 10000000 == 1020) {
                if (chr.getPartyQuest() instanceof Pyramid) {
                    if (((Pyramid) chr.getPartyQuest()).useSkill()) {
                        skillLevel = 1;
                    }
                }
            }*/
            if (skillLevel == 0) {
                return null;
            }
            /*if (display > 80) { //Hmm
                if (!theSkill.getAction()) {
                    AutobanFactory.FAST_ATTACK.autoban(chr, "WZ Edit; adding action to a skill: " + display);
                    return null;
                }
            }*/
            return mySkill.getEffect(skillLevel);
        }
    }

    protected synchronized void applyAttack(AttackInfo attack, MapleCharacter player, int attackCount) {
    	
    	for(int monsterId : attack.allDamage.keySet()){
    		
    		List<Integer> damage = attack.allDamage.get(monsterId);
    		
    		int totalDamage = 0;
    		
    		for(int d : damage){
    			totalDamage += d;
    		}
    		
    		if(player.getMap().getObject(monsterId) instanceof MapleMonster)
    		{
        		MapleMonster monster = (MapleMonster) player.getMap().getObject(monsterId);
        		
        		monster.damage(player, totalDamage);
        		if(monster.isAlive()){
            		monster.setAggro(true);
            		if(!player.equals(monster.getController())){
            			if(monster.getController() != null)
            				monster.getController().uncontrolMonster(monster);
            			player.controlMonster(monster);
            		}
            		
            		if(attack.skill != 0){
            			Skill skill = SkillFactory.getSkill(attack.skill);

                		MapleStatEffect effect = skill.getEffect(attack.skilllevel);
                		
                		
                		if(effect.getMonsterStati().size() > 0){
                			if(effect.makeChanceResult()){
                				monster.applyStatusEffect(player, new MonsterStatusEffect(skill, attack.skilllevel), effect.getDuration());
                			}
                		}
            		}
            		
        		}
    		}
    		
    		
    	}
    	
    	if(attack.skill != 0){
        	Skill skill = SkillFactory.getSkill(attack.skill);
        	
        	if(!player.isSkillCoolingDown(skill)){

        		MapleStatEffect effect = skill.getEffect(attack.skilllevel);
            	if(effect != null){
            		effect.applyTo(player);
            	}
        	}
        		
    	}
    	
    	
        /*Skill theSkill = null;
        MapleStatEffect attackEffect = null;
        try {

            if (attack.skill != 0) {
                theSkill = SkillFactory.getSkill(attack.skill);
                attackEffect = attack.getAttackEffect(player, theSkill);
                if (attackEffect == null) {
                    player.getClient().sendReallowActions();
                    return;
                }

                if (attack.skill != Cleric.HEAL) {
                    if (player.isAlive()) {
                        attackEffect.applyTo(player);
                    } else {
                        player.getClient().sendReallowActions();
                    }
                }
                int mobCount = attackEffect.getMobCount();
                if (attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Page.FINAL_ATTACK_BW || attack.skill == Page.FINAL_ATTACK_SWORD || attack.skill == Fighter.FINAL_ATTACK_SWORD
                        || attack.skill == Fighter.FINAL_ATTACK_AXE || attack.skill == Spearman.FINAL_ATTACK_SPEAR || attack.skill == Spearman.FINAL_ATTACK_POLEARM || attack.skill == WindArcher.FINAL_ATTACK
                        || attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Hunter.FINAL_ATTACK || attack.skill == Crossbowman.FINAL_ATTACK) {
                    mobCount = 15;//:(
                }
            }
            if (!player.isAlive()) {
                return;
            }

            //WTF IS THIS F3,1
        if (attackCount != attack.numDamage && attack.skill != ChiefBandit.MESO_EXPLOSION && attack.skill != NightWalker.VAMPIRE && attack.skill != WindArcher.WIND_SHOT && attack.skill != Aran.COMBO_SMASH && attack.skill != Aran.COMBO_PENRIL && attack.skill != Aran.COMBO_TEMPEST && attack.skill != NightLord.NINJA_AMBUSH && attack.skill != Shadower.NINJA_AMBUSH) {
             return;
             }
            int totDamage = 0;
            final MapleMap map = player.getMap();

            if (attack.skill == ChiefBandit.MESO_EXPLOSION) {
                int delay = 0;
                for (Integer oned : attack.allDamage.keySet()) {
                    MapleMapObject mapobject = map.getObject(oned.intValue());
                    if (mapobject != null && mapobject.getType() == MapleMapObjectType.ITEM) {
                        final MapleMapItem mapitem = (MapleMapItem) mapobject;
                        if (mapitem.getMesos() > 9) {
                            synchronized (mapitem) {
                                TimerManager.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                    	mapitem.destroy();
                                    }
                                }, delay);
                                delay += 100;
                            }
                        } else if (mapitem.getMesos() == 0) {
                            return;
                        }
                    } else if (mapobject != null && mapobject.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                }
            }
            for (Integer oned : attack.allDamage.keySet()) {
            	
                MapleMonster monster = null;
                
                MapleMapObject obj = map.getObject(oned.intValue());
                
                if(obj instanceof MapleMonster){
                	monster = (MapleMonster) obj;
                }
                
                if (monster != null) {
                    int totDamageToOneMonster = 0;
                    List<Integer> onedList = attack.allDamage.get(oned);
                    for (Integer eachd : onedList) {
                        totDamageToOneMonster += eachd.intValue();
                    }
                    totDamage += totDamageToOneMonster;
                   
                    player.checkMonsterAggro(monster);
                    if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != 0 && (attack.skill == 0 || attack.skill == Rogue.DOUBLE_STAB || attack.skill == Bandit.SAVAGE_BLOW || attack.skill == ChiefBandit.ASSAULTER || attack.skill == ChiefBandit.BAND_OF_THIEVES || attack.skill == Shadower.ASSASSINATE || attack.skill == Shadower.TAUNT || attack.skill == Shadower.BOOMERANG_STEP)) {
                        Skill pickpocket = SkillFactory.getSkill(ChiefBandit.PICKPOCKET);
                        int delay = 0;
                        int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET);
                        for (int eachd : onedList) {
                            if (pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()) {
                                TimerManager.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        //player.getMap().spawnMesoDrop(Math.min((int) Math.max(((double) eachd / (double) 20000) * (double) maxmeso, (double) 1), maxmeso), new Point((int) (monster.getPosition().getX() + Randomizer.nextInt(100) - 50), (int) (monster.getPosition().getY())), monster, player, true, (byte) 0);
                                        //Drop mesos here
                                    }
                                }, delay);
                                delay += 100;
                            }
                        }
                    } else if (attack.skill == Marksman.SNIPE) {
                        totDamageToOneMonster = 195000 + Randomizer.nextInt(5000);
                    } else if (attack.skill == Marauder.ENERGY_DRAIN || attack.skill == ThunderBreaker.ENERGY_DRAIN || attack.skill == NightWalker.VAMPIRE || attack.skill == Assassin.DRAIN) {
                        player.restoreHp(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skill))).getX() / 100.0), player.getMaxHp() / 2)));
                    } else if (attack.skill == Bandit.STEAL) {
                        Skill steal = SkillFactory.getSkill(Bandit.STEAL);
                        if (Math.random() < 0.3 && steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()) { //Else it drops too many cool stuff :(
                            List<MonsterDrop> toSteals = MonsterDropManager.getInstance().getPossibleDrops(monster.getId());
                            Collections.shuffle(toSteals);
                            int toSteal = toSteals.get(rand(0, (toSteals.size() - 1))).getItemId();
                            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                            Item item;
                            if (ItemConstants.getInventoryType(toSteal).equals(MapleInventoryType.EQUIP)) {
                                item = ii.randomizeStats((Equip) ii.getEquipById(toSteal));
                            } else {
                                item = new Item(toSteal, (byte) 0, (short) 1, -1);
                            }
                            player.getMap().dropItem(item, monster.getPosition(), player);
                            //player.getMap().spawnItemDrop(monster, player, item, monster.getPosition(), false, false);
                            //monster.addStolen(toSteal);
                        }
                    } else if (attack.skill == FPArchMage.FIRE_DEMON) {
                        monster.setTempEffectiveness(MapleElement.ICE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(FPArchMage.FIRE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(FPArchMage.FIRE_DEMON))).getDuration() * 1000);
                    } else if (attack.skill == ILArchMage.ICE_DEMON) {
                        monster.setTempEffectiveness(MapleElement.FIRE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(ILArchMage.ICE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(ILArchMage.ICE_DEMON))).getDuration() * 1000);
                    } else if (attack.skill == Outlaw.HOMING_BEACON || attack.skill == Corsair.BULLSEYE) {
                        //player.setMarkedMonster(monster.getObjectId());
                        //player.announce(MaplePacketCreator.giveBuff(1, attack.skill, Collections.singletonList(new Pair<>(MapleBuffStat.HOMING_BEACON, monster.getObjectId()))));
                    }
                    if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != 0) {
                        Skill hamstring = SkillFactory.getSkill(Bowmaster.HAMSTRING);
                        if (hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()) {
                            //MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, null, false);
                            //monster.applyStatus(player, monsterStatusEffect, false, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
                        }
                    }
                    if (player.getBuffedValue(MapleBuffStat.BLIND) != 0) {
                        Skill blind = SkillFactory.getSkill(Marksman.BLIND);
                        if (blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()) {
                            //MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, null, false);
                            //monster.applyStatus(player, monsterStatusEffect, false, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
                        }
                    }
                    final int id = player.getJob().getId();
                    if (id == 121 || id == 122) {
                        for (int charge = 1211005; charge < 1211007; charge++) {
                            Skill chargeSkill = SkillFactory.getSkill(charge);
                            if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)) {
                                final ElementalEffectiveness iceEffectiveness = monster.getEffectiveness(MapleElement.ICE);
                                if (totDamageToOneMonster > 0 && iceEffectiveness == ElementalEffectiveness.NORMAL || iceEffectiveness == ElementalEffectiveness.WEAK) {
                                    monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.FREEZE, 1), chargeSkill, null, false), false, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 2000);
                                }
                                break;
                            }
                        }
                    } else if (player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null || player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                        Skill skill;
                        if (player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null) {
                            skill = SkillFactory.getSkill(21101003);
                            final MapleStatEffect eff = skill.getEffect(player.getSkillLevel(skill));

                            if (eff.makeChanceResult()) {
                                monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.NEUTRALISE, 1), skill, null, false), false, eff.getX() * 1000, false);
                            }
                        }
                        if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != 0) {
                            skill = SkillFactory.getSkill(21100005);
                            player.setHp(player.getHp() + ((totDamage * skill.getEffect(player.getSkillLevel(skill)).getX()) / 100));
                        }
                    } else if (id == 412 || id == 422 || id == 1411) {
                        Skill type = SkillFactory.getSkill(player.getJob().getId() == 412 ? 4120005 : (player.getJob().getId() == 1411 ? 14110004 : 4220005));
                        if (player.getSkillLevel(type) > 0) {
                            MapleStatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
                            for (int i = 0; i < attackCount; i++) {
                                if (venomEffect.makeChanceResult()) {
                                    if (monster.getVenomMulti() < 3) {
                                        monster.setVenomMulti((monster.getVenomMulti() + 1));
                                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, null, false);
                                        monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                                    }
                                }
                            }
                        }
                    }
                    
                    if (totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0) {
                        if (attackEffect.makeChanceResult()) {
                            monster.applyStatus(player, new MonsterStatusEffect(attackEffect.getMonsterStati(), theSkill, null, false), attackEffect.isPoison(), attackEffect.getDuration());
                        }
                    }                 
                    if (attack.isHH && !monster.isBoss()) {
                        map.damageMonster(player, monster, monster.getHp() - 1);
                    } else if (attack.isHH) {
                        int HHDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Paladin.HEAVENS_HAMMER).getEffect(player.getSkillLevel(SkillFactory.getSkill(Paladin.HEAVENS_HAMMER))).getDamage() / 100));
                        map.damageMonster(player, monster, (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8)));
                    } else {
                        map.damageMonster(player, monster, totDamageToOneMonster);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    protected AttackInfo parseDamage(ByteBuf buf, MapleCharacter chr, boolean ranged) {
        AttackInfo ret = new AttackInfo();
        buf.readByte();
        ret.numAttackedAndDamage = buf.readByte();
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.allDamage = new HashMap<>();
        ret.skill = buf.readInt();
        
        if (ret.skill > 0) {
            ret.skilllevel = chr.getSkillLevel(ret.skill);
        }
        
        if (ret.skill == FPArchMage.BIG_BANG || ret.skill == ILArchMage.BIG_BANG || ret.skill == Bishop.BIG_BANG || ret.skill == Gunslinger.GRENADE || ret.skill == Brawler.CORKSCREW_BLOW || ret.skill == ThunderBreaker.CORKSCREW_BLOW || ret.skill == NightWalker.POISON_BOMB) {
            ret.charge = buf.readInt();
        } else {
            ret.charge = 0;
        }
        if (ret.skill == Paladin.HEAVENS_HAMMER) {
            ret.isHH = true;
        }
        buf.skipBytes(8);
        ret.display = buf.readByte();
        ret.direction = buf.readByte();
        ret.stance = buf.readByte();
        if (ret.skill == ChiefBandit.MESO_EXPLOSION) {
            if (ret.numAttackedAndDamage == 0) {
                buf.skipBytes(10);
                int bullets = buf.readByte();
                for (int j = 0; j < bullets; j++) {
                    int mesoid = buf.readInt();
                    buf.skipBytes(1);
                    ret.allDamage.put(Integer.valueOf(mesoid), null);
                }
                return ret;
            } else {
                buf.skipBytes(6);
            }
            for (int i = 0; i < ret.numAttacked + 1; i++) {
                int oid = buf.readInt();
                if (i < ret.numAttacked) {
                    buf.skipBytes(12);
                    int bullets = buf.readByte();
                    List<Integer> allDamageNumbers = new ArrayList<>();
                    for (int j = 0; j < bullets; j++) {
                        int damage = buf.readInt();
                        allDamageNumbers.add(Integer.valueOf(damage));
                    }
                    ret.allDamage.put(Integer.valueOf(oid), allDamageNumbers);
                    buf.skipBytes(4);
                } else {
                    int bullets = buf.readByte();
                    for (int j = 0; j < bullets; j++) {
                        int mesoid = buf.readInt();
                        buf.skipBytes(1);
                        ret.allDamage.put(Integer.valueOf(mesoid), null);
                    }
                }
            }
            return ret;
        }
        if (ranged) {
            buf.readByte();
            ret.speed = buf.readByte();
            buf.readByte();
            ret.rangedirection = buf.readByte();
            buf.skipBytes(7);
            if (ret.skill == Bowmaster.HURRICANE || ret.skill == Marksman.PIERCING_ARROW || ret.skill == Corsair.RAPID_FIRE || ret.skill == WindArcher.HURRICANE) {
                buf.skipBytes(4);
            }
        } else {
            buf.readByte();
            ret.speed = buf.readByte();
            buf.skipBytes(4);
        }
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = buf.readInt();
            buf.skipBytes(14);
            List<Integer> allDamageNumbers = new ArrayList<>();
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = buf.readInt();
                if (ret.skill == Marksman.SNIPE) {
                    damage += 0x80000000; //Critical
                }
                allDamageNumbers.add(Integer.valueOf(damage));
            }
            if (ret.skill != 5221004) {
                buf.skipBytes(4);
            }
            ret.allDamage.put(Integer.valueOf(oid), allDamageNumbers);
        }
        return ret;
    }

}
