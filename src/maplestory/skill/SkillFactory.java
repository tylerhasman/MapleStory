
package maplestory.skill;

import constants.MapleElement;
import constants.skills.Aran;
import constants.skills.Archer;
import constants.skills.Assassin;
import constants.skills.Bandit;
import constants.skills.Beginner;
import constants.skills.Bishop;
import constants.skills.BlazeWizard;
import constants.skills.Bowmaster;
import constants.skills.Buccaneer;
import constants.skills.ChiefBandit;
import constants.skills.Cleric;
import constants.skills.Corsair;
import constants.skills.Crossbowman;
import constants.skills.Crusader;
import constants.skills.DarkKnight;
import constants.skills.DawnWarrior;
import constants.skills.DragonKnight;
import constants.skills.FPArchMage;
import constants.skills.FPMage;
import constants.skills.FPWizard;
import constants.skills.Fighter;
import constants.skills.GM;
import constants.skills.Gunslinger;
import constants.skills.Hermit;
import constants.skills.Hero;
import constants.skills.Hunter;
import constants.skills.ILArchMage;
import constants.skills.ILMage;
import constants.skills.ILWizard;
import constants.skills.Legend;
import constants.skills.Magician;
import constants.skills.Marauder;
import constants.skills.Marksman;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Noblesse;
import constants.skills.Page;
import constants.skills.Paladin;
import constants.skills.Pirate;
import constants.skills.Priest;
import constants.skills.Ranger;
import constants.skills.Rogue;
import constants.skills.Shadower;
import constants.skills.Sniper;
import constants.skills.Spearman;
import constants.skills.SuperGM;
import constants.skills.Swordsman;
import constants.skills.ThunderBreaker;
import constants.skills.WhiteKnight;
import constants.skills.WindArcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import maplestory.player.MapleJob;
import maplestory.server.MapleStory;
import me.tyler.mdf.MapleFile;
import me.tyler.mdf.Node;

public class SkillFactory {
	
	private static boolean loaded = false;
	
	private static Map<Integer, Skill> skills = new HashMap<>();

	private static Map<MapleJob, List<Skill>> jobSkills = new HashMap<>();
	
	private static final String REGEX = "^[0-9]*$";//Matches digits, aka jobs
	
	public static Skill getSkill(int id) {
		if (!skills.isEmpty()) {
			return skills.get(id);
		}
		return null;
	}
	
	private static void createJobSkillEntry(MapleJob job, Skill skill){
		if(!jobSkills.containsKey(job)){
			jobSkills.put(job, new LinkedList<>());
		}
		
		List<Skill> skills = jobSkills.get(job);
		
		skills.add(skill);
	}
	
	public static List<Skill> getJobsSkills(MapleJob job){
		return Collections.unmodifiableList(jobSkills.getOrDefault(job, new ArrayList<>()));
	}
	
	public static List<Skill> getAllSkillsAtJob(MapleJob job){
		
		List<Skill> skills = new LinkedList<>();
		
		MapleJob checked = job;
		
		do{
			
			skills.addAll(getJobsSkills(checked));
			
		}while((checked = job.getPrevious()) != null);
		
		
		return Collections.unmodifiableList(skills);
	}

	public static void loadAllSkills() {
		
		if(loaded){
			throw new IllegalStateException("Skills already loaded.");
		}
		
		MapleFile source = MapleStory.getDataFile("Skill.mdf");
		
		for(Node job : source.getRootNode().getChildren()){
			String name = job.getName();
			
			if(name.replace(".img", "").matches(REGEX)){
				
				Node skillsNode = job.getChild("skill");
				
				for(Node skill : skillsNode.getChildren()){
					
					int skillId = Integer.parseInt(skill.getName());
					
					Skill loadedSkill = loadFromNode(skillId, skill);
					
					int jobId = Integer.parseInt(name.replace(".img", ""));
					
					loadedSkill.job = jobId;
					
					createJobSkillEntry(loadedSkill.getJob(), loadedSkill);
					
					skills.put(skillId, loadedSkill);
					
				}
				
				
			}
			
		}
		
		loaded = true;

	}

	private static Skill loadFromNode(int id, Node data) {
		Skill ret = new Skill(id);
		boolean isBuff = false;
		int skillType = data.readInt("skillType", -1);
		String elem = data.readString("elemAttr");
		if (elem != null) {
			ret.setElement(MapleElement.getFromChar(elem.charAt(0)));
		} else {
			ret.setElement(MapleElement.NEUTRAL);
		}
		Node effect = data.getChild("effect");
		if (skillType != -1) {
			if (skillType == 2) {
				isBuff = true;
			}
		} else {
			Node actionNode = data.getChild("action");
			boolean action = false;
			if (actionNode == null) {
				if (data.getChild("prepare/action") != null) {
					action = true;
				} else {
					switch (id) {
					case 5201001:
					case 5221009:
						action = true;
						break;
					}
				}
			} else {
				action = true;
			}
			ret.setAction(action);
			Node hit = data.getChild("hit");
			Node ball = data.getChild("ball");
			isBuff = effect != null && hit == null && ball == null;
			isBuff |= actionNode != null && actionNode.readString("0", "").equals("alert2");
			switch (id) {
			case Hero.RUSH:
			case Paladin.RUSH:
			case DarkKnight.RUSH:
			case DragonKnight.SACRIFICE:
			case FPMage.EXPLOSION:
			case FPMage.POISON_MIST:
			case Cleric.HEAL:
			case Ranger.MORTAL_BLOW:
			case Sniper.MORTAL_BLOW:
			case Assassin.DRAIN:
			case Hermit.SHADOW_WEB:
			case Bandit.STEAL:
			case Shadower.SMOKE_SCREEN:
			case SuperGM.HEAL_PLUS_DISPEL:
			case Hero.MONSTER_MAGNET:
			case Paladin.MONSTER_MAGNET:
			case DarkKnight.MONSTER_MAGNET:
			case Gunslinger.RECOIL_SHOT:
			case Marauder.ENERGY_DRAIN:
			case BlazeWizard.FLAME_GEAR:
			case NightWalker.SHADOW_WEB:
			case NightWalker.POISON_BOMB:
			case NightWalker.VAMPIRE:
				isBuff = false;
				break;
			case Beginner.RECOVERY:
			case Beginner.NIMBLE_FEET:
			case Beginner.MONSTER_RIDER:
			case Beginner.ECHO_OF_HERO:
			case Swordsman.IRON_BODY:
			case Fighter.AXE_BOOSTER:
			case Fighter.POWER_GUARD:
			case Fighter.RAGE:
			case Fighter.SWORD_BOOSTER:
			case Crusader.ARMOR_CRASH:
			case Crusader.COMBO:
			case Hero.ENRAGE:
			case Hero.HEROS_WILL:
			case Hero.MAPLE_WARRIOR:
			case Hero.STANCE:
			case Page.BW_BOOSTER:
			case Page.POWER_GUARD:
			case Page.SWORD_BOOSTER:
			case Page.THREATEN:
			case WhiteKnight.BW_FIRE_CHARGE:
			case WhiteKnight.BW_ICE_CHARGE:
			case WhiteKnight.BW_LIT_CHARGE:
			case WhiteKnight.MAGIC_CRASH:
			case WhiteKnight.SWORD_FIRE_CHARGE:
			case WhiteKnight.SWORD_ICE_CHARGE:
			case WhiteKnight.SWORD_LIT_CHARGE:
			case Paladin.BW_HOLY_CHARGE:
			case Paladin.HEROS_WILL:
			case Paladin.MAPLE_WARRIOR:
			case Paladin.STANCE:
			case Paladin.SWORD_HOLY_CHARGE:
			case Spearman.HYPER_BODY:
			case Spearman.IRON_WILL:
			case Spearman.POLEARM_BOOSTER:
			case Spearman.SPEAR_BOOSTER:
			case DragonKnight.DRAGON_BLOOD:
			case DragonKnight.POWER_CRASH:
			case DarkKnight.AURA_OF_BEHOLDER:
			case DarkKnight.BEHOLDER:
			case DarkKnight.HEROS_WILL:
			case DarkKnight.HEX_OF_BEHOLDER:
			case DarkKnight.MAPLE_WARRIOR:
			case DarkKnight.STANCE:
			case Magician.MAGIC_GUARD:
			case Magician.MAGIC_ARMOR:
			case FPWizard.MEDITATION:
			case FPWizard.SLOW:
			case FPMage.SEAL:
			case FPMage.SPELL_BOOSTER:
			case FPArchMage.HEROS_WILL:
			case FPArchMage.INFINITY:
			case FPArchMage.MANA_REFLECTION:
			case FPArchMage.MAPLE_WARRIOR:
			case ILWizard.MEDITATION:
			case ILMage.SEAL:
			case ILWizard.SLOW:
			case ILMage.SPELL_BOOSTER:
			case ILArchMage.HEROS_WILL:
			case ILArchMage.INFINITY:
			case ILArchMage.MANA_REFLECTION:
			case ILArchMage.MAPLE_WARRIOR:
			case Cleric.INVINCIBLE:
			case Cleric.BLESS:
			case Priest.DISPEL:
			case Priest.DOOM:
			case Priest.HOLY_SYMBOL:
			case Priest.MYSTIC_DOOR:
			case Bishop.HEROS_WILL:
			case Bishop.HOLY_SHIELD:
			case Bishop.INFINITY:
			case Bishop.MANA_REFLECTION:
			case Bishop.MAPLE_WARRIOR:
			case Archer.FOCUS:
			case Hunter.BOW_BOOSTER:
			case Hunter.SOUL_ARROW:
			case Ranger.PUPPET:
			case Bowmaster.CONCENTRATE:
			case Bowmaster.HEROS_WILL:
			case Bowmaster.MAPLE_WARRIOR:
			case Bowmaster.SHARP_EYES:
			case Crossbowman.CROSSBOW_BOOSTER:
			case Crossbowman.SOUL_ARROW:
			case Sniper.PUPPET:
			case Marksman.BLIND:
			case Marksman.HEROS_WILL:
			case Marksman.MAPLE_WARRIOR:
			case Marksman.SHARP_EYES:
			case Rogue.DARK_SIGHT:
			case Assassin.CLAW_BOOSTER:
			case Assassin.HASTE:
			case Hermit.MESO_UP:
			case Hermit.SHADOW_PARTNER:
			case NightLord.HEROS_WILL:
			case NightLord.MAPLE_WARRIOR:
			case NightLord.NINJA_AMBUSH:
			case NightLord.SHADOW_STARS:
			case Bandit.DAGGER_BOOSTER:
			case Bandit.HASTE:
			case ChiefBandit.MESO_GUARD:
			case ChiefBandit.PICKPOCKET:
			case Shadower.HEROS_WILL:
			case Shadower.MAPLE_WARRIOR:
			case Shadower.NINJA_AMBUSH:
			case Pirate.DASH:
			case Marauder.TRANSFORMATION:
			case Buccaneer.SUPER_TRANSFORMATION:
			case Corsair.BATTLE_SHIP:
			case GM.HIDE:
			case SuperGM.HASTE:
			case SuperGM.HOLY_SYMBOL:
			case SuperGM.BLESS:
			case SuperGM.HIDE:
			case SuperGM.HYPER_BODY:
			case Noblesse.BLESSING_OF_THE_FAIRY:
			case Noblesse.ECHO_OF_HERO:
			case Noblesse.MONSTER_RIDER:
			case Noblesse.NIMBLE_FEET:
			case Noblesse.RECOVERY:
			case DawnWarrior.COMBO:
			case DawnWarrior.FINAL_ATTACK:
			case DawnWarrior.IRON_BODY:
			case DawnWarrior.RAGE:
			case DawnWarrior.SOUL:
			case DawnWarrior.SOUL_CHARGE:
			case DawnWarrior.SWORD_BOOSTER:
			case BlazeWizard.ELEMENTAL_RESET:
			case BlazeWizard.FLAME:
			case BlazeWizard.IFRIT:
			case BlazeWizard.MAGIC_ARMOR:
			case BlazeWizard.MAGIC_GUARD:
			case BlazeWizard.MEDITATION:
			case BlazeWizard.SEAL:
			case BlazeWizard.SLOW:
			case BlazeWizard.SPELL_BOOSTER:
			case WindArcher.BOW_BOOSTER:
			case WindArcher.EAGLE_EYE:
			case WindArcher.FINAL_ATTACK:
			case WindArcher.FOCUS:
			case WindArcher.PUPPET:
			case WindArcher.SOUL_ARROW:
			case WindArcher.STORM:
			case WindArcher.WIND_WALK:
			case NightWalker.CLAW_BOOSTER:
			case NightWalker.DARKNESS:
			case NightWalker.DARK_SIGHT:
			case NightWalker.HASTE:
			case NightWalker.SHADOW_PARTNER:
			case ThunderBreaker.DASH:
			case ThunderBreaker.ENERGY_CHARGE:
			case ThunderBreaker.ENERGY_DRAIN:
			case ThunderBreaker.KNUCKLER_BOOSTER:
			case ThunderBreaker.LIGHTNING:
			case ThunderBreaker.SPARK:
			case ThunderBreaker.LIGHTNING_CHARGE:
			case ThunderBreaker.SPEED_INFUSION:
			case ThunderBreaker.TRANSFORMATION:
			case Legend.BLESSING_OF_THE_FAIRY:
			case Legend.AGILE_BODY:
			case Legend.ECHO_OF_HERO:
			case Legend.RECOVERY:
			case Legend.MONSTER_RIDER:
			case Aran.MAPLE_WARRIOR:
			case Aran.HEROS_WILL:
			case Aran.POLEARM_BOOSTER:
			case Aran.COMBO_DRAIN:
			case Aran.SNOW_CHARGE:
			case Aran.BODY_PRESSURE:
			case Aran.SMART_KNOCKBACK:
			case Aran.COMBO_BARRIER:
			case Aran.COMBO_ABILITY:
				isBuff = true;
				break;
			}
		}
		for (Node level : data.getChild("level")) {
			 ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff));
		}
		ret.setAnimationTime(0);
		int animationTime = 0;
		if (effect != null) {
			for (Node effectEntry : effect) {
				animationTime += effectEntry.readInt("delay", 0);
			}
			ret.setAnimationTime(animationTime);
		}
		return ret;
	}

	public static String getSkillName(int skillid) {
		MapleFile stringData = MapleStory.getDataFile("String.mdf");

		Node skillData = stringData.getRootNode().getChild("Skill.img");
		StringBuilder skill = new StringBuilder();
		skill.append(String.valueOf(skillid));
		if (skill.length() == 4) {
			skill.delete(0, 4);
			skill.append("000").append(String.valueOf(skillid));
		}
		if (skillData.getChild(skill.toString()) != null) {
			for (Node skillStringData : skillData.getChild(skill.toString()).getChildren()) {
				if (skillStringData.getName().equals("name"))
					return skillStringData.getValue().toString();
			}
		}

		return null;
	}

	public static Collection<Skill> getAllSkills() {
		return skills.values();
	}
}
