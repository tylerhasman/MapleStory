package maplestory.life;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import constants.MapleBuffStat;
import constants.MapleDisease;
import constants.MonsterStatus;
import lombok.Data;
import maplestory.life.MapleLifeFactory.BanishInfo;
import maplestory.map.MapleMapObject;
import maplestory.map.MapleMapObjectType;
import maplestory.map.MapleMist;
import maplestory.player.MapleCharacter;
import maplestory.util.Pair;
import maplestory.util.Randomizer;
import static constants.MonsterStatus.*;

@Data
public class MobSkill {

	private List<Integer> summons;
	
	private long coolTime;
	
	private long duration;
	
	private int hp, mpCost;
	
	private int spawnEffect;

	private int x, y;
	
	private float prop;
	
	private int limit;
	
	private Point lt, rb;
	
	private final int skillId;
	
	private final int level;
	
	public MobSkill(int skillId, int level){
		this.skillId = skillId;
		this.level = level;
	}
	
	public void setLtRb(Point lt, Point rb){
		setLt(lt);
		setRb(rb);
	}

	private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        int multiplier = facingLeft ? 1 : -1;
        Point mylt = new Point(lt.x * multiplier + posFrom.x, lt.y + posFrom.y);
        Point myrb = new Point(rb.x * multiplier + posFrom.x, rb.y + posFrom.y);
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
        List<MapleMapObjectType> objectTypes = new ArrayList<MapleMapObjectType>();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInBox(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), objectTypes);
    }
    
    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }
    
	public void applyEffect(MapleCharacter character, MapleMonster monster, boolean skill) {
		
		Map<MonsterStatus, Integer> stats = new HashMap<>();
		List<Integer> reflection = new LinkedList<Integer>();
		
		MapleDisease disease = null;
		
		if(skillId == 100 || skillId == 110 || skillId == 150){
			stats.put(WEAPON_ATTACK_UP, x);
		}else if(skillId == 101 || skillId == 111 || skillId == 151){
			stats.put(MAGIC_ATTACK_UP, x);
		}else if(skillId == 102 || skillId == 112 || skillId == 152){
			stats.put(WEAPON_DEFENSE_UP, x);
		}else if(skillId == 103 || skillId == 113 || skillId == 153){
			stats.put(MAGIC_DEFENSE_UP, x);
		}else if(skillId == 114){
			if(lt != null && rb != null && skill){
				List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
				
				int healAmount = (getX() / 1000) * (int) (950 + 1050 * Math.random());
				
				for(MapleMapObject obj : objects){
					if(obj instanceof MapleMonster){
						MapleMonster target = (MapleMonster) obj;
						
						target.heal(healAmount);
						target.restoreMp(getY());
					}
				}
			}else{
				monster.heal(getX());
				monster.restoreMp(getY());
			}
		}else if(skillId == 120){
			disease = MapleDisease.SEAL;
		}else if(skillId == 121){
			disease = MapleDisease.DARKNESS;
		}else if(skillId == 122){
			disease = MapleDisease.WEAKEN;
		}else if(skillId == 123){
			disease = MapleDisease.STUN;
		}else if(skillId == 124){
			disease = MapleDisease.CURSE;
		}else if(skillId == 125){
			disease = MapleDisease.POISON;
		}else if(skillId == 126){
			disease = MapleDisease.SLOW;
		}else if(skillId == 127){
			if(lt != null && rb != null && skill){
				List<MapleMapObject> players = getObjectsInRange(monster, MapleMapObjectType.PLAYER);
				players.add(character);
				
				for(MapleMapObject obj : players){
					if(obj instanceof MapleCharacter){
						MapleCharacter target = (MapleCharacter) obj;
						
						target.dispelAllSkills();
					}
				}
			}else{
				character.dispelAllSkills();
			}
		}else if(skillId == 128){
			disease = MapleDisease.SEDUCE;
		}else if(skillId == 129){
			if(lt != null && rb != null && skill){
				
				List<MapleMapObject> players = getObjectsInRange(monster, MapleMapObjectType.PLAYER);
				players.add(character);
				
				for(MapleMapObject obj : players){
					if(obj instanceof MapleCharacter){
						MapleCharacter target = (MapleCharacter) obj;
						
						BanishInfo info = monster.getStats().getBanishInfo();
						
						target.banish(info.getMap(), info.getPortal(), info.getMsg());
					}
				}
				
			}else{
				BanishInfo info = monster.getStats().getBanishInfo();
				
				character.banish(info.getMap(), info.getPortal(), info.getMsg());
			}
		}else if(skillId == 131){
			MapleMist mist = new MapleMist(calculateBoundingBox(monster.getPosition(), true), monster, this);
		
			monster.getMap().spawnMist(mist, x * 10, false, false);
		}else if(skillId == 132){
			disease = MapleDisease.CONFUSE;
		}else if(skillId == 140){
			if(makeChanceResult() && !monster.isBuffed(MAGIC_IMMUNITY)){
				stats.put(WEAPON_IMMUNITY, x);
			}
		}else if(skillId == 141){
			if (makeChanceResult() && !monster.isBuffed(WEAPON_IMMUNITY)) {
                stats.put(MAGIC_IMMUNITY, x);
            }
		}else if(skillId == 143){
			stats.put(WEAPON_REFLECT, x);
			stats.put(WEAPON_IMMUNITY, x);
			reflection.add(x);
		}else if(skillId == 144){
			stats.put(MAGIC_REFLECT, x);
			stats.put(MAGIC_IMMUNITY, x);
			reflection.add(x);
		}else if(skillId == 145){
			stats.put(WEAPON_REFLECT, x);
			stats.put(WEAPON_IMMUNITY, x);
			stats.put(MAGIC_REFLECT, x);
			stats.put(MAGIC_IMMUNITY, x);
			reflection.add(x);
		}else if(skillId == 200){
			if(monster.getMap().countObjectsOfType(MapleMapObjectType.MONSTER) < 80){
				for(int mobId : getSummons()){
					
					MapleMonster toSpawn = MapleLifeFactory.getMonster(mobId);
					
					toSpawn.setPosition(monster.getPosition());
					int xpos, ypos;
					
					xpos = (int) monster.getPosition().getX();
					ypos = (int) monster.getPosition().getY();
					
					if(mobId == 8500003){ //Pap bomb high
						toSpawn.setFh((int) Math.ceil(Math.random() * 19D));
						ypos = -590;
					}else if(mobId == 8500003){
						xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
                        if (ypos != -590) {
                            ypos = (int) monster.getPosition().getY();
                        }
					}else if(mobId == 8510100){
						if (Math.ceil(Math.random() * 5) == 1) {
                            ypos = 78;
                            xpos = (int) Randomizer.nextInt(5) + (Randomizer.nextInt(2) == 1 ? 180 : 0);
                        } else {
                            xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
                        }
					}
					
					if(monster.getMap().getMapId() == 220080001){
						if (xpos < -890) {
                            xpos = (int) (Math.ceil(Math.random() * 150) - 890);
                        } else if (xpos > 230) {
                            xpos = (int) (230 - Math.ceil(Math.random() * 150));
                        }
					}else if(monster.getMap().getMapId() == 230040420){
						if (xpos < -239) {
                            xpos = (int) (Math.ceil(Math.random() * 150) - 239);
                        } else if (xpos > 371) {
                            xpos = (int) (371 - Math.ceil(Math.random() * 150));
                        }
					}
					
					toSpawn.setPosition(new Point(xpos, ypos));
					toSpawn.setSpawnEffect(getSpawnEffect());
					
					monster.getMap().spawnMonster(toSpawn);
				}
			}
			
		}
		
		if(stats.size() > 0){
			List<Pair<MonsterStatus, Integer>> statChanges = new ArrayList<>();
			
			for(MonsterStatus key : stats.keySet()){
				statChanges.add(new Pair<>(key, stats.get(key)));
			}
			if(lt != null && rb != null && skill){
				List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
				
				for(MapleMapObject obj : objects){
					if(obj instanceof MapleMonster){
						MapleMonster target = (MapleMonster) obj;
						
						target.applyMonsterBuff(statChanges, getX(), getSkillId(), getDuration(), this, reflection);
					}
				}
			}else{
				monster.applyMonsterBuff(statChanges, x, skillId, duration, this, reflection);
			}
			
		}
		
		if(disease != null){
			if(lt != null && rb != null && skill){
				
				List<MapleMapObject> players = getObjectsInRange(monster, MapleMapObjectType.PLAYER);
				players.add(character);
				
				int i = 0;
				
				for(MapleMapObject obj : players){
					if(obj instanceof MapleCharacter){
						MapleCharacter target = (MapleCharacter) obj;
						
						boolean isBlockedByHolyShield = disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN;
						
						if(target.getBuffedValue(MapleBuffStat.HOLY_SHIELD) != 0 && isBlockedByHolyShield){
							return;
						}
						
						if(isBlockedByHolyShield){
							if(i < 10){
								target.giveDebuff(disease, this);
								i++;
							}	
						}else{
							target.giveDebuff(disease, this);
						}
						
					}
				}
				
			}else{
				if(character.getBuffedValue(MapleBuffStat.HOLY_SHIELD) != 0 && (disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN)){
					return;
				}
				character.giveDebuff(disease, this);
			}
		}
		
		//TODO: Finish this, gah
		
	}
	
	
	
}
