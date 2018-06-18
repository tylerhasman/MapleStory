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

import java.util.Collections;
import java.util.List;

import constants.MapleBuffStat;
import constants.skills.Crusader;
import constants.skills.DawnWarrior;
import constants.skills.Hero;
import constants.skills.ThunderBreaker;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;
import maplestory.util.Pair;

public final class CloseRangeDamageHandler extends AbstractDealDamageHandler {
	
    private boolean isFinisher(int skillId) {
        return skillId > 1111002 && skillId < 1111007 || skillId == 11111002 || skillId == 11111003;
    }

    @Override
    public void handle(ByteBuf buf, MapleClient client) {
        MapleCharacter chr = client.getCharacter();
        AttackInfo attack = parseDamage(buf, chr, false);
        
        int orbs = chr.getBuffedValue(MapleBuffStat.COMBO);
        
        if (isFinisher(attack.skill)) {
        	if(orbs > 0) {
        		orbs -= 2;// -2 to get rid of the extra +1 from the buff being active
        		chr.setBuffedValue(MapleBuffStat.COMBO, orbs);
        	}//Don't do anything otherwise
        } else if (attack.numAttacked > 0) {
            if (attack.skill != Crusader.SHOUT && orbs > 0) {
            	
                MapleStatEffect effect = chr.getBuffStatSource(MapleBuffStat.COMBO);
                Skill skill = SkillFactory.getSkill(effect.getSourceId());
                
            	Skill advSkill = SkillFactory.getSkill(skill.getId() == Crusader.COMBO ? Hero.ADVANCED_COMBO : DawnWarrior.ADVANCED_COMBO);
            	
            	int advLevel = chr.getSkillLevel(advSkill);
            	
                int maxOrbs = effect.getX();
                
                boolean bonusOrb = false;
                
                if(advLevel > 0) {

                	MapleStatEffect advEffect = advSkill.getEffect(advLevel);
                	
                	maxOrbs = advEffect.getX();
                	
                	bonusOrb = advEffect.makeChanceResult();
                	
                }
                
                if (orbs <= maxOrbs) { // OR orbs <= effect.getX() IS EQUAL
                    
                	if(bonusOrb && orbs < maxOrbs) {
                		orbs++;
                	}
                	
                    chr.setBuffedValue(MapleBuffStat.COMBO, orbs++);
                    
                }
            }/* else if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100004) : SkillFactory.getSkill(5110001)) > 0 && (player.getJob().isA(MapleJob.MARAUDER) || player.getJob().isA(MapleJob.THUNDERBREAKER2))) {
                for (int i = 0; i < attack.numAttacked; i++) {
                    player.handleEnergyChargeGain();
                }
            }*/
        }
        /*if (attack.numAttacked > 0 && attack.skill == DragonKnight.SACRIFICE) {
            int totDamageToOneMonster = 0; // sacrifice attacks only 1 mob with 1 attack
            final Iterator<List<Integer>> dmgIt = attack.allDamage.values().iterator();
            if (dmgIt.hasNext()) {
                totDamageToOneMonster = dmgIt.next().get(0).intValue();
            }
            int remainingHP = player.getHp() - totDamageToOneMonster * attack.getAttackEffect(player, null).getX() / 100;
            if (remainingHP > 1) {
                player.setHp(remainingHP);
            } else {
                player.setHp(1);
            }
            player.updateSingleStat(MapleStat.HP, player.getHp());
            player.checkBerserk();
        }*/
        /*if (attack.numAttacked > 0 && attack.skill == 1211002) {
            boolean advcharge_prob = false;
            int advcharge_level = player.getSkillLevel(SkillFactory.getSkill(1220010));
            if (advcharge_level > 0) {
                advcharge_prob = SkillFactory.getSkill(1220010).getEffect(advcharge_level).makeChanceResult();
            }
            if (!advcharge_prob) {
                player.cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
            }
        }*/
        int attackCount = 1;
        /*if (attack.skill != 0) {
            attackCount = attack.getAttackEffect(player, null).getAttackCount();
        }*/
        /*if (numFinisherOrbs == 0 && isFinisher(attack.skill)) {
            return;
        }*/
        
        //int animationTime = 0;
        
        if (attack.skill > 0) {
            Skill skill = SkillFactory.getSkill(attack.skill);
            //animationTime = skill.getAnimationTime();
            MapleStatEffect effect_ = skill.getEffect(chr.getSkillLevel(skill));
            if (effect_.getCooldown() > 0) {
                if (chr.isSkillCoolingDown(attack.skill)) {
                    return;
                } else {
                    chr.addCooldown(skill);
                }
            }
        }
        
        /*if ((player.getSkillLevel(SkillFactory.getSkill(NightWalker.VANISH)) > 0 || player.getSkillLevel(SkillFactory.getSkill(WindArcher.WIND_WALK)) > 0 || player.getSkillLevel(SkillFactory.getSkill(Rogue.DARK_SIGHT)) > 0) && player.getBuffedValue(MapleBuffStat.DARKSIGHT) != null) {// && player.getBuffSource(MapleBuffStat.DARKSIGHT) != 9101004
            player.cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
            player.cancelBuffStats(MapleBuffStat.DARKSIGHT);
        }*/
        /*if(animationTime > 0){
        	TimerManager.schedule(() -> applyAttack(attack, player, attackCount), animationTime);
        }else{
        	applyAttack(attack, player, attackCount);
        }*/
        chr.getMap().broadcastPacket(PacketFactory.closeRangeAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.speed, attack.direction, attack.display), chr.getId());
        
        applyAttack(attack, chr, attackCount);
        
    }
    
}