package maplestory.life;

import java.awt.Point;

import constants.SummonMovementType;
import constants.skills.Outlaw;
import constants.skills.Ranger;
import constants.skills.Sniper;
import constants.skills.WindArcher;
import lombok.Getter;
import lombok.Setter;
import maplestory.client.MapleClient;
import maplestory.map.AbstractAnimatedMapleMapObject;
import maplestory.map.MapleMapObjectType;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MapleSummon extends AbstractAnimatedMapleMapObject {

	@Getter
	private final MapleCharacter owner;
	@Getter
	private final byte skillLevel;
	@Getter
	private final int skill;
	@Getter @Setter
	private int hp;
	@Getter
	private final SummonMovementType movementType;
	
	private boolean spawned;
	
	public MapleSummon(MapleCharacter owner, int skill, Point pos, SummonMovementType movementType) {
		this.owner = owner;
		skillLevel = (byte) owner.getSkillLevel(skill);
		this.skill = skill;
		this.movementType = movementType;
		setPosition(pos);
		spawned = false;
	}

	@Override
	public void sendSpawnData(MapleClient client) {
		client.sendPacket(PacketFactory.getSpawnSummonPacket(this, !spawned));
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.sendPacket(PacketFactory.getDestroySummonPacket(this, false));
	}
	
	public void spawn(){
		spawned = true;
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.SUMMON;
	}

	public boolean isPuppet() {
		
		if(skill == Ranger.PUPPET || skill == Sniper.PUPPET || skill == WindArcher.PUPPET){
			return true;
		}
		
		return false;
	}

	public boolean isStationary() {
		return isPuppet() || skill == Outlaw.OCTOPUS;
	}

	public void addHP(int x) {
		hp += x;
		
		if(hp <= 0){
			owner.cancelEffect(skill);
		}
	}

	public void damage(int damage, int unkByte, int monsterIdFrom) {
		addHP(-damage);
		
		owner.getMap().broadcastPacket(PacketFactory.damageSummon(owner.getId(), skill, damage, unkByte, monsterIdFrom));
	}

}
