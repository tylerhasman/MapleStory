package maplestory.map;

import java.awt.Point;
import java.awt.Rectangle;

import constants.skills.BlazeWizard;
import constants.skills.FPMage;
import constants.skills.NightWalker;
import constants.skills.Shadower;
import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.life.MobSkill;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;

public class MapleMist extends AbstractMapleMapObject {

	@Getter
	private Rectangle mistPosition;
	@Getter
	private MapleCharacter owner = null;
	private MapleMonster mob = null;
	@Getter
	private MapleStatEffect effect;
	private MobSkill skill;
	@Getter
	private boolean isMobMist, isPoisonMist, isRecoveryMist;
	@Getter
	private int skillDelay;
	
	public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
		this.mistPosition = mistPosition;
		this.mob = mob;
		this.skill = skill;
		isMobMist = true;
		isPoisonMist = true;
		isRecoveryMist = false;
		skillDelay = 0;
	}
	
	public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
		this.mistPosition = mistPosition;
		this.owner = owner;
		this.effect = source;
		skillDelay = 8;
		isMobMist = false;
		isRecoveryMist = false;
		isPoisonMist = false;
		if(source.getSourceId() == Shadower.SMOKE_SCREEN){
			isPoisonMist = false;
		}else if(source.getSourceId() == FPMage.POISON_MIST || source.getSourceId() == BlazeWizard.FLAME_GEAR || source.getSourceId() == NightWalker.POISON_BOMB){
			isPoisonMist = true;
		}
	}
	
	public Rectangle getBox(){
		return mistPosition;
	}
	
	public Skill getSourceSkill(){
		return SkillFactory.getSkill(effect.getSourceId());
	}
	
    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }
	
	@Override
	public void sendSpawnData(MapleClient client) {
		
		if(owner != null){
			client.sendPacket(PacketFactory.spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), owner.getSkillLevel(SkillFactory.getSkill(effect.getSourceId())), this));
		}else{
			client.sendPacket(PacketFactory.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getLevel(), this));
		}
		
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.sendPacket(PacketFactory.removeMist(getObjectId()));
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.MIST;
	}

}
