package maplestory.life;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import constants.MessageType;
import constants.MonsterStatus;
import constants.ServerConstants;
import constants.skills.FPMage;
import tools.TimerManager;
import tools.TimerManager.MapleTask;
import database.MonsterDropManager;
import database.MonsterDropManager.MonsterDrop;
import lombok.Getter;
import lombok.Setter;
import maplestory.client.MapleClient;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.life.movement.AbsoluteLifeMovement;
import maplestory.map.AbstractLoadedMapleLife;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapObject;
import maplestory.map.MapleMapObjectType;
import maplestory.party.MapleParty.PartyEntry;
import maplestory.player.MapleCharacter;
import maplestory.player.monsterbook.MonsterBook;
import maplestory.quest.MapleQuestInstance;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.server.MapleServer;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.MonsterStatusEffect;
import maplestory.skill.SkillFactory;
import maplestory.util.Pair;
import maplestory.util.Randomizer;

public class MapleMonster extends AbstractLoadedMapleLife {

	@Getter
	private MapleMonsterStats stats;
	
	private WeakReference<MapleCharacter> controller;

	private int mapId, channel, world;
	
	@Getter
	private boolean spawned;
	
	@Getter @Setter
	private int hp, mp;
	
	private boolean aggro;
	
	private List<MonsterDeathListener> deathListeners;
	
	@Getter
	private Map<MonsterStatus, MonsterStatusEffect> statusEffects;
	
	@Getter
	private boolean bossHpBarVisible;
	
	private Map<Integer, Integer> usedMobSkills;
	
	private List<Pair<Integer, Integer>> cooldowns;
	
	private List<MapleTask> tasks;
	
	@Getter @Setter
	private int spawnEffect;
	
	@Getter
	private boolean untargetable;
	
	public MapleMonster(int id, MapleMonsterStats stats) {
		super(id);
		this.stats = stats;
		controller = new WeakReference<MapleCharacter>(null);
		initialize();
		spawned = false;
		statusEffects = new HashMap<>(MonsterStatus.values().length);
		usedMobSkills = new HashMap<>();
		cooldowns = new ArrayList<>();
		tasks = Collections.synchronizedList(new ArrayList<>());
		
		if(stats.isBoss()){
			setBossHpBarVisible(true);
		}
		spawnEffect = -1;
	}

	public void teleport(Point point){
		MapleCharacter controller = getController();
		setController(null);
		setPosition(getMap().calcDropPosition(point));
		getMap().broadcastPacket(PacketFactory.getMoveMonsterPacket(0, -1, 0, 0, 0, 0, getObjectId(), getPosition(), Collections.singletonList(new AbsoluteLifeMovement(0, getPosition(), 10, 0))));
		if(controller != null)
			controller.controlMonster(this);
	}
	
	public void setBossHpBarVisible(boolean bossHpBarVisible) {
		if(!stats.isBoss()){
			throw new IllegalStateException("Only bosses can have HP bars! "+toString());
		}
		if(spawned && bossHpBarVisible){
			getMap().broadcastPacket(PacketFactory.createBossHpBar(this));
		}
		this.bossHpBarVisible = bossHpBarVisible;
	}
	
	public void setUntargetable(boolean untargetable) {
		this.untargetable = untargetable;
		
		if(spawned){
			for(MapleCharacter chr : getMap().getPlayers()){
				sendDestroyData(chr.getClient());
				sendSpawnData(chr.getClient());
			}
		}
		
	}
	
	public void addDeathListener(MonsterDeathListener mdl){
		if (deathListeners == null) {
			deathListeners = new ArrayList<>();
		}
		
		deathListeners.add(mdl);
	}
	
	public void initialize(){
		setStance(5);
		hp = stats.getHp();
		mp = stats.getMp();
	}
	
	public MapleMap getMap(){
		return MapleServer.getChannel(world, channel).getMapFactory().getMap(mapId);
	}
	
	public void setMap(MapleMap map){
		mapId = map.getMapId();
		channel = map.getChannel().getId();
		world = map.getChannel().getWorld().getId();
	}
	
	public void spawn(){
		if(spawned){
			throw new IllegalStateException("Already spawned! "+toString());
		}
		
		findNewController();
		
		for(MapleCharacter chr : getMap().getPlayers()){
			sendSpawnData(chr.getClient());
		}
		
		spawned = true;
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
		if(untargetable){
			client.sendPacket(PacketFactory.getSpawnUntargetableMonsterPacket(this));
		}else{
			client.sendPacket(PacketFactory.getMonsterSpawnPacket(this));
		}
		
		if(statusEffects.size() > 0){
			for(MonsterStatusEffect effect : statusEffects.values()){
				client.sendPacket(PacketFactory.applyMonsterStatus(getObjectId(), effect));
			}
		}
		
		if(isBossHpBarVisible()){
			//client.sendPacket(PacketFactory.createBossHpBar(this));
		}
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.sendPacket(PacketFactory.killMonster(getObjectId(), false));
	}
	
	public void damage(MapleMapObject source, int amount){
		if(!isAlive()){
			return;
		}
		
		hp -= amount;
		
		if(isAlive()){
			if(source instanceof MapleMonster){
				getMap().broadcastPacket(PacketFactory.mobDamageMobFriendly(this, amount));
			}else if(source instanceof MapleCharacter){
				MapleCharacter chr = (MapleCharacter) source;
				showHpTo(chr);
				getMap().broadcastPacket(PacketFactory.getMonsterDamagePacket(getObjectId(), amount), chr.getId());
			}else{
				getMap().broadcastPacket(PacketFactory.getMonsterDamagePacket(getObjectId(), amount));
			}
		}else{
			getMap().executeMapScript(null, "onMonsterKilled", this, source);
			if(source instanceof MapleCharacter){
				showHpTo((MapleCharacter) source);
				kill((MapleCharacter) source);
			}else{
				kill();	
			}
		}
		
	}
	
	public void showHpTo(MapleCharacter source) {
		int calcRemaining = (int) Math.max(1, hp * 100f / getMaxHp());
		
		source.getClient().sendPacket(PacketFactory.getShowMonsterHp(getObjectId(), calcRemaining));
	}

	public void kill(){
		kill(null);
	}
	
	public void kill(MapleCharacter source){
		
		if(source != null){

			if(stats.getExp() > 0){
				
				if(source.getParty() != null){
					int exp = stats.getExp() * ServerConstants.EXP_RATE;
					
					double bonus = source.getParty().getExpBonus(source);
					
					source.giveExp(exp, (int) (exp * bonus));
					
					for(PartyEntry entry : source.getParty().getMembers()){
						if(entry.getSnapshot().isOnline()){
							if(entry.getSnapshot().getId() != source.getId()){
								if(entry.getSnapshot().getMapId() == source.getMapId()){
									entry.getSnapshot().getLiveCharacter().giveExp((int) (exp * (1D/3D)));
								}
							}
						}
					}
					
				}else{
					source.giveExp(stats.getExp() * ServerConstants.EXP_RATE);
				}
				
			}
			
			for(MapleQuestInstance quest : source.getQuests(MapleQuestStatus.STARTED)){
				if(quest.getQuest().getQuestInfo().getRelevantMobs().containsKey(getId())){
					if(quest.progressMob(getId())){
						source.getClient().sendPacket(PacketFactory.updateQuest(quest, false));	
					}
				}
			}
		}
		
		
		for(MapleCharacter chr : getMap().getPlayers()){
			chr.getClient().sendPacket(PacketFactory.killMonster(getObjectId(), true));
		}
		
		hp = 0;
		getMap().removeObject(getObjectId());
		
		TimerManager.schedule(new Runnable() {
			
			@Override
			public void run() {
				if(deathListeners != null){
					deathListeners.forEach(dl -> dl.onMonsterDeath(getStats().getAnimationTimes().get("die1")));
				}
				
				dropItems(source);
				
				setController(null);
			}
			
		}, stats.getAnimationTimes().getOrDefault("die1", 0));
		
		synchronized (tasks) {
			tasks.forEach(task -> task.cancel(false));
			tasks.clear();	
		}
		
		for(MonsterStatus status : statusEffects.keySet()){
			statusEffects.get(status).cancelTasks();
		}
		statusEffects.clear();
		
	}
	
	private void dropItems(MapleCharacter chr, List<MonsterDrop> drops){
		if(drops.isEmpty()){
			return;
		}
		int startX = -drops.size() * 15 / 2;
		
		for(MonsterDrop drop : drops){
			if(drop.getItemId() > 0 && ItemInfoProvider.isQuestItem(drop.getItemId())){
				if(chr != null){
					if(!shouldDropQuestItem(chr, drop)){
						continue;
					}
				}else{
					continue;
				}
			}
			Point real = (Point) getPosition().clone();
			real.translate(startX, 0);
			if(drop.getItemId() == 0){
				getMap().dropMesos(drop.getAmount() * ServerConstants.MESO_RATE, real, chr, this);
			}else{
				getMap().dropItem(drop.getItem(), real, chr, this);
			}
			startX += 15;
		}
		
	}
	
	private void dropItems(MapleCharacter chr){
		List<MonsterDrop> possible = MonsterDropManager.getInstance().getPossibleDrops(getId());
		
		List<MonsterDrop> actual = possible.stream().filter(drop -> drop.shouldDrop()).collect(Collectors.toList());
		List<MonsterDrop> global = MonsterDropManager.getInstance().getGlobalDrops().stream().filter(drop -> drop.shouldDrop()).collect(Collectors.toList());
		
		actual.addAll(global);
		
		dropItems(chr, actual);
	}
	
	private boolean shouldDropQuestItem(MapleCharacter chr, MonsterDrop drop){
		for(MapleQuestInstance quest : chr.getQuests(MapleQuestStatus.STARTED)){
			Map<Integer, Integer> rel = quest.getQuest().getQuestInfo().getRelevantItems();
			int needed = rel.getOrDefault(drop.getItemId(), 0);
			if(needed > 0){
				InventoryType invType = InventoryType.getByItemId(drop.getItemId());
				
				if(chr.getInventory(invType).countById(drop.getItemId()) < needed){
					return true;
				}else if(invType == InventoryType.EQUIP){
					if(chr.getInventory(InventoryType.EQUIPPED).countById(drop.getItemId()) < needed){
						return true;
					}
				}
			}
			
		}
		
		return false;

	}
	
	public MapleCharacter getController(){
		return controller.get();
	}
	
	public void setController(MapleCharacter c){
		
		if(controller != null && controller.get() != null){
			
			MapleCharacter chr = controller.get();
			
			controller = null;
			
			chr.uncontrolMonster(this);
			
		}
		
		controller = new WeakReference<MapleCharacter>(c);
	}
	
	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.MONSTER;
	}

	public byte getTeam() {
		return -1;
	}

	public void findNewController() {
		
		if(getMap().getPlayers().size() == 0){
			setController(null);
			return;
		}
		
		aggro = false;
		
		MapleCharacter lowest = null;
		int lowestAmount = Integer.MAX_VALUE;
		
		for(MapleCharacter chr : getMap().getPlayers()){
			if(chr.getControlledMonsters().size() < lowestAmount){
				lowestAmount = chr.getControlledMonsters().size();
				lowest = chr;
			}
		}
		
		lowest.controlMonster(this);
		
	}

	public int getMaxHp() {
		return stats.getHp();
	}
	
	public int getMaxMp() {
		return stats.getMp();
	}

	public boolean canUseSkill(MobSkill toUse) {
		
		if(toUse == null){
			return false;
		}

		if(getMp() < toUse.getMpCost()){
			return false;
		}
		
		for(Pair<Integer, Integer> cooldown : cooldowns){
			if(cooldown.getLeft() == toUse.getSkillId() && cooldown.getRight() == cooldown.getRight()){
				return false;
			}
		}
		
		if(toUse.getLimit() > 0){
			if(usedMobSkills.getOrDefault(toUse.getSkillId(), 0) >= toUse.getLimit()){
				return false;
			}
		}
		
		if(toUse.getSkillId() == 200){
			Collection<MapleMapObject> mmo = getMap().getObjects();
            int i = 0;
            for (MapleMapObject mo : mmo) {
                if (mo.getType() == MapleMapObjectType.MONSTER) {
                    i++;
                }
            }
            if (i > 100) {
                return false;
            }
		}
		
		return true;
	}

	public void useSkill(MobSkill skill, MapleCharacter target){
		
		if(!isAlive()){
			return;
		}
		
		mp = Math.max(0, mp - skill.getMpCost());
		
		skill.applyEffect(target, this, true);
		
		int skillId = skill.getSkillId();
		int level = skill.getLevel();
		
		if(skill.getCoolTime() > 0){
			cooldowns.add(new Pair<Integer, Integer>(skill.getSkillId(), skill.getLevel()));
			
			MapleTask cancelCooldownTask = TimerManager.schedule(new Runnable() {
				
				@Override
				public void run() {
					
					synchronized (tasks) {
						tasks.remove(this);
					}
					
					clearCooldown(skillId, level);
					
				}
			}, skill.getCoolTime());
			
			synchronized (tasks) {
				tasks.add(cancelCooldownTask);
			}
			
		}
	}
	
	public void clearCooldown(int skill, int level){
		
		for(int i = 0; i < cooldowns.size();i++){
			Pair<Integer, Integer> cd = cooldowns.get(i);
			
			if(cd.getLeft() == skill && cd.getRight() == level){
				cooldowns.remove(i);
				break;
			}
		}
		
	}
	
	public boolean hasSkill(int skill, int level) {
		for(Pair<Integer, Integer> mskill : stats.getSkills()){
			if(mskill.getLeft() == skill && mskill.getRight() == level){
				return true;
			}
		}
		return false;
	}

	public void setAggro(boolean b) {
		aggro = b;
	}
	
	public boolean hasAggro(){
		return aggro;
	}
	
	public boolean applyStatusEffect(MapleCharacter source, MonsterStatusEffect effect, long duration){
		
		if(effect.isMonsterSkill()){
			return false;
		}
		
		boolean effective = stats.isEffective(effect.getSource().getElement());
		
		if(!effective){
			return false;
		}
		
		MapleStatEffect statusEffect = effect.getStatEffect();
		
		List<Pair<MonsterStatus, Integer>> statusChanges = effect.getStatusChanges();
		
		if(statusEffect.isPoison()){
			if(getHp() <= 1){
				return false;
			}
		}
		
		if(stats.isBoss()){
			for(Pair<MonsterStatus, Integer> changes : statusChanges){
				if(!changes.getLeft().canApplyToBosses()){
					return false;
				}
			}
		}
		
		for(Pair<MonsterStatus, Integer> changes : statusChanges){
			MonsterStatusEffect oldEffect = statusEffects.get(changes.getLeft());
			
			if(oldEffect != null){
				oldEffect.removeStatus(changes.getLeft());
			}
			
			effect.setStatusValue(changes.getLeft(), changes.getRight());
		}
		
		effect.createCancelTask(this, duration);
		
		if(statusEffect.isPoison()){
			int level = effect.getLevel();
			int poisonDamage = Math.min(Short.MAX_VALUE, (int) (getMaxHp() / (70.0 - level) + 0.999));
			effect.setStatusValue(MonsterStatus.POISON, poisonDamage);
			effect.createDamageTask(this, poisonDamage, source.getObjectId(), 1000, 1000);
		}
		
		getMap().broadcastPacket(PacketFactory.applyMonsterStatus(getObjectId(), effect));
		
		return true;
	}
	
	public boolean isAlive() {
		return hp > 0;
	}

	@Override
	public String toString() {
		return "MapleMonster: { id: '"+getId()+"' name: '"+stats.getName()+"' map: '"+getMap().getMapId()+"' }";
	}
	
	public static interface MonsterDeathListener {
		
		public void onMonsterDeath(int animationTime);
		
	}

	public void heal(int healAmount) {
		if(!isAlive()){
			return;
		}
		
		hp = Math.min(getMaxHp(), hp + healAmount);

		getMap().broadcastPacket(PacketFactory.getMonsterDamagePacket(getObjectId(), -healAmount));
	}

	public void restoreMp(int y) {
		mp += y;
	}

	public boolean isBuffed(MonsterStatus status) {
		return statusEffects.containsKey(status);
	}
	
	public void applyMonsterBuff(List<Pair<MonsterStatus, Integer>> stats2, int x, int skillId, long duration, MobSkill mobSkill, List<Integer> reflection) {
		
		MapleTask cancelTask = TimerManager.schedule(new Runnable() {
			
			@Override
			public void run() {
				if(isAlive()){
					getMap().broadcastPacket(PacketFactory.cancelMonsterStatus(getObjectId(), stats2));
					for(Pair<MonsterStatus, Integer> pair : stats2){
						statusEffects.remove(pair.getLeft());
					}
				}
			}
		}, duration);
		
		synchronized (tasks) {
			tasks.add(cancelTask);	
		}
		
		MonsterStatusEffect effect = new MonsterStatusEffect(mobSkill);
		
		for(Pair<MonsterStatus, Integer> pair : stats2){
			statusEffects.put(pair.getLeft(), effect);
			effect.setStatusValue(pair.getLeft(), pair.getRight());
		}
		
		getMap().broadcastPacket(PacketFactory.applyMonsterStatus(getObjectId(), effect, reflection));
		
		effect.setCancelTask(cancelTask);
	}

}
