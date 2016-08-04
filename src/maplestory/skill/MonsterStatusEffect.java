package maplestory.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;



import tools.TimerManager;
import tools.TimerManager.MapleTask;
import lombok.Getter;
import lombok.Setter;
import maplestory.life.MapleMonster;
import maplestory.life.MobSkill;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;
import maplestory.util.Pair;
import constants.MonsterStatus;

public class MonsterStatusEffect {

	private Map<MonsterStatus, Integer> statusChanges;
	@Getter
	private Skill source;
	@Getter
	private int level;
	@Getter
	private MobSkill mobSkill;
	@Setter
	private MapleTask cancelTask;
	@Setter
	private MapleTask damageTask;
	
	MonsterStatusEffect() {
		statusChanges = new HashMap<>();
	}
	
	public MonsterStatusEffect(Skill source, int level){
		this();
		this.source = source;
		this.level = level;
	}
	
	public MonsterStatusEffect(MobSkill mobSkill){
		this();
		this.mobSkill = mobSkill;
	}
	
	public void setStatusValue(MonsterStatus status, int value){
		statusChanges.put(status, value);
	}
	
	public void removeStatus(MonsterStatus status){
		statusChanges.remove(status);
		if(statusChanges.isEmpty()){
			cancelDamageTask();
			cancelCancelTask();
		}
	}
	
	public int getStatusValue(MonsterStatus status){
		return statusChanges.getOrDefault(status, 0);
	}
	
	public boolean isMonsterSkill(){
		return mobSkill != null;
	}
	
	public List<Pair<MonsterStatus, Integer>> getStatusChanges(){
		List<Pair<MonsterStatus, Integer>> changes = new ArrayList<>();
		
		for(MonsterStatus status : statusChanges.keySet()){
			Pair<MonsterStatus, Integer> pair = new Pair<>(status, getStatusValue(status));
			
			changes.add(pair);
		}
		
		return changes;
	}
	
	public MapleStatEffect getStatEffect(){
		if(isMonsterSkill()){
			throw new IllegalStateException("Only player skills have status effects!");
		}
		
		return source.getEffect(level);
	}
	
	public void cancelTasks(){
		cancelCancelTask();
		cancelDamageTask();
	}
	
	private void cancelDamageTask(){
		if(damageTask != null){
			damageTask.cancel(false);
			damageTask = null;
		}
	}
	
	private void cancelCancelTask(){
		if(cancelTask != null){
			cancelTask.cancel(false);
			cancelTask = null;
		}
	}

	public void createCancelTask(MapleMonster monster, long duration) {
		if(isMonsterSkill()){
			throw new IllegalStateException("Monster Skills can't create a cancel task like this.");
		}
		long delay = duration + getSource().getAnimationTime();
		
		cancelTask = TimerManager.schedule(new Runnable() {
			
			@Override
			public void run() {
				if(monster.isAlive()){
					monster.getMap().broadcastPacket(PacketFactory.cancelMonsterStatus(monster.getObjectId(), getStatusChanges()));
				}
				for(MonsterStatus status : monster.getStatusEffects().keySet()){
					monster.getStatusEffects().remove(status);
				}
				cancelDamageTask();
			}
		}, delay);
	}

	public void createDamageTask(MapleMonster monster, int damageAmount, int sourceObjectId, long initialDelay, long msBetweenExecution) {
		
		damageTask = TimerManager.scheduleRepeatingTask(new Runnable() {
			
			@Override
			public void run() {
				MapleCharacter chr = monster.getMap().getChannel().getPlayerById(sourceObjectId);
				
				monster.damage(chr, damageAmount);
				
				if(!monster.isAlive()){
					cancelDamageTask();
					cancelCancelTask();
				}
				
			}
		}, initialDelay, msBetweenExecution, TimeUnit.MILLISECONDS);
		
		
	}
	
}
