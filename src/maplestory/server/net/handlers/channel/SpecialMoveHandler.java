package maplestory.server.net.handlers.channel;

import java.awt.Point;

import tools.TimerManager;
import constants.MessageType;
import constants.skills.Brawler;
import constants.skills.Buccaneer;
import constants.skills.Hero;
import constants.skills.Paladin;
import constants.skills.Priest;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.map.MapleMapObject;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.MonsterStatusEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;

public class SpecialMoveHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		MapleCharacter chr = client.getCharacter();
		
		int timestamp = buf.readInt();
		
		int skillid = buf.readInt();
		
		Point pos = null;
		
		int skillLevel = buf.readByte();
		
		Skill skill = SkillFactory.getSkill(skillid);
		
		int chrSkillLevel = chr.getSkillLevel(skill);
		
		if(chrSkillLevel == 0 || chrSkillLevel != skillLevel){
			client.sendReallowActions();
			return;
		}
		
		MapleStatEffect effect = skill.getEffect(skillLevel);
		
		if(effect.getCooldown() > 0){
			if(chr.isSkillCoolingDown(skill)){
				client.sendReallowActions();
				return;
			}else{
				chr.addCooldown(skill, skillLevel);
			}
		}
		
		if(skillid == Hero.MONSTER_MAGNET || skillid == Paladin.MONSTER_MAGNET){
			int num = buf.readInt();
			int mobId;
			byte success;
			for(int i = 0; i < num;i++){
				mobId = buf.readInt();
				success = buf.readByte();
				chr.getMap().broadcastPacket(PacketFactory.getShowMagnetEffect(mobId, success), chr.getId());
				MapleMapObject monster = chr.getMap().getObject(mobId);
				if(monster instanceof MapleMonster){
					((MapleMonster) monster).setAggro(true);
					chr.controlMonster((MapleMonster) monster);
				}
			}
			byte direction = buf.readByte();
			chr.getMap().broadcastPacket(PacketFactory.getShowBuffEffect(chr.getId(), skillid, chrSkillLevel, direction), chr.getId());
			client.sendReallowActions();
			return;
		}else if(skillid == Buccaneer.TIME_LEAP){
			//Remove party and players cooldowns
		}else if(skillid == Brawler.MP_RECOVERY){
			int lose = chr.getMaxMp() / effect.getX();
			chr.restoreHp(-lose);
			int gain = lose * (effect.getY() / 100);
			chr.restoreMp(gain);
		}else if(skillid % 10000000 == 1004){
			buf.readShort();
			
		}else{
			chr.getMap().broadcastPacket(PacketFactory.showBuffEffect(chr.getId(), skillid, 1), chr.getId());
		}
		
		if(buf.readableBytes() == 5){
			pos = readPosition(buf);
		}
		
		final Point f_pos = pos;
		
		TimerManager.schedule(new Runnable() {
			
			@Override
			public void run() {
				if(chr.isAlive()){
					if(skillid != Priest.MYSTIC_DOOR){
						effect.applyTo(chr, f_pos);
						client.sendReallowActions();
					}else if(client.getCharacter().canCreateMagicDoor()){
						chr.sendMessage(MessageType.PINK_TEXT, "Please wait 5 seconds before casting mystic door again.");
						client.sendReallowActions();
					}
				}else{
					client.sendReallowActions();
				}
			}
		}, skill.getAnimationTime());
		

		
	}

}
