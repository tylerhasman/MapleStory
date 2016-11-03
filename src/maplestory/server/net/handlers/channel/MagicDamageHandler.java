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

import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;
import io.netty.buffer.ByteBuf;

public final class MagicDamageHandler extends AbstractDealDamageHandler {
    @Override
    public final void handle(ByteBuf buf, MapleClient c) {
        MapleCharacter player = c.getCharacter();
        AttackInfo attack = parseDamage(buf, player, false);
        byte[] packet = PacketFactory.magicAttack(player, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, attack.allDamage, -1, attack.speed, attack.direction, attack.display);
        if (attack.skill == 2121001 || attack.skill == 2221001 || attack.skill == 2321001) {
            packet = PacketFactory.magicAttack(player, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.charge, attack.speed, attack.direction, attack.display);
        }
        player.getMap().broadcastPacket(packet, player.getId());
        MapleStatEffect effect = attack.getAttackEffect(player, null);
        Skill skill = SkillFactory.getSkill(attack.skill);
        MapleStatEffect effect_ = skill.getEffect(player.getSkillLevel(skill));
        if (effect_.getCooldown() > 0) {
            if (player.isSkillCoolingDown(attack.skill)) {
                return;
            } else {
                player.addCooldown(SkillFactory.getSkill(attack.skill), attack.skilllevel);
            }
        }
        /*if(skill.getAnimationTime() > 0){
        	TimerManager.schedule(() -> applyAttack(attack, player, effect.getAttackCount()), skill.getAnimationTime());
        }else{
        	applyAttack(attack, player, effect.getAttackCount());
        }*/
        
        applyAttack(attack, player, effect.getAttackCount());
      
        Skill eaterSkill = SkillFactory.getSkill((player.getJob().getId() - (player.getJob().getId() % 10)) * 10000);// MP Eater, works with right job
        int eaterLevel = player.getSkillLevel(eaterSkill);
        if (eaterLevel > 0) {
            for (Integer singleDamage : attack.allDamage.keySet()) {
                eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getObject(singleDamage), 0);
            }
        }
    }
}
