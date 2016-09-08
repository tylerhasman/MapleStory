package maplestory.player;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maplestory.cashshop.CashShopWallet;
import maplestory.client.MapleClient;
import maplestory.guild.MapleGuild;
import maplestory.guild.MapleGuildRankLevel;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.MapleCashInventory;
import maplestory.inventory.MapleEquippedInventory;
import maplestory.inventory.MapleInventory;
import maplestory.inventory.MapleWeaponType;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.DueyParcel;
import maplestory.inventory.item.EquipItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.PetItem;
import maplestory.inventory.storage.MapleStorageBox;
import maplestory.life.MapleLifeFactory;
import maplestory.life.MapleMonster;
import maplestory.life.MapleMount;
import maplestory.life.MapleNPC;
import maplestory.life.MapleSummon;
import maplestory.life.MobSkill;
import maplestory.map.AbstractAnimatedMapleMapObject;
import maplestory.map.MapleMagicDoor;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapObjectType;
import maplestory.map.MaplePortal;
import maplestory.party.MapleParty;
import maplestory.party.PartyOperationType;
import maplestory.party.MapleParty.PartyEntry;
import maplestory.quest.MapleQuest;
import maplestory.quest.MapleQuestInstance;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.script.MapleScript;
import maplestory.script.MapleScriptInstance;
import maplestory.script.NpcConversationManager;
import maplestory.script.QuestScriptManager;
import maplestory.server.MapleServer;
import maplestory.server.net.PacketFactory;
import maplestory.server.net.handlers.channel.GroupChatHandler.GroupChatType;
import maplestory.shop.MapleShop;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillData;
import maplestory.skill.SkillFactory;
import maplestory.util.Pair;
import maplestory.util.Randomizer;
import maplestory.world.World;
import tools.CRand32;
import tools.TimerManager;
import tools.TimerManager.MapleTask;
import constants.ExpTable;
import constants.LoginStatus;
import constants.MapleBuffStat;
import constants.MapleDisease;
import constants.MapleEmote;
import constants.MapleStat;
import constants.MessageType;
import constants.PopupInfo;
import constants.ServerConstants;
import constants.skills.Assassin;
import constants.skills.Bishop;
import constants.skills.BlazeWizard;
import constants.skills.Bowmaster;
import constants.skills.Buccaneer;
import constants.skills.Corsair;
import constants.skills.DarkKnight;
import constants.skills.DawnWarrior;
import constants.skills.FPArchMage;
import constants.skills.GM;
import constants.skills.Gunslinger;
import constants.skills.Hermit;
import constants.skills.ILArchMage;
import constants.skills.Magician;
import constants.skills.Marksman;
import constants.skills.Paladin;
import constants.skills.Priest;
import constants.skills.Ranger;
import constants.skills.Sniper;
import constants.skills.Spearman;
import constants.skills.SuperGM;
import constants.skills.Swordsman;
import constants.skills.ThunderBreaker;
import database.MapleDatabase;
import database.QueryResult;

@ToString(includeFieldNames=true)
public class MapleCharacter extends AbstractAnimatedMapleMapObject {

	private static final int[] DEFAULT_KEY = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31, 34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63, 64, 57, 48, 29, 7, 24, 33, 41, 39};
    private static final int[] DEFAULT_TYPE = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4, 4};
    private static final int[] DEFAULT_ACTION = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2, 17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103, 104, 105, 54, 22, 52, 21, 25, 26, 23, 27};
	
	private MapleClient client;
	
	@Setter @Getter
	private String name;
	
	@Getter
	private int level;
	
	private int int_, str, dex, luk;//Separate because we don't want the getter
	
	@Getter
	private int hp, mp;
	
	private int maxHp, maxMp;
	
	@Getter
	private int remainingAp, remainingSp;
	
	@Getter
	private int exp, fame, gachaExp;
	
	@Getter
	private int mapId;
	
	@Getter @Setter
	private int id;
	
	@Getter @Setter
	private int gender;
	
	@Getter
	private int skinColor, hair, face;
	
	@Getter
	private MapleJob job;
	
	@Getter//What is linkedName?, Marriage maybe
	private String linkedName;
	
	@Getter
	private int meso;

	private Map<InventoryType, Inventory> inventories;
	
	@Getter
	private int rank, rankMove, jobRank, jobRankMove;
	
	@Getter
	private boolean gm;
	
	@Getter
	private int accountId;
	
	@Setter
	private int world;
	
	@Getter @Setter
	private Point2D location;
	
	private Map<Integer, Pair<Integer, Integer>> keyBindings;
	
	@Getter
	private Map<Integer, SkillData> skills;
	
	private Map<Integer, MapleMonster> controlled;
	
	@Getter @Setter
	private int fmReturnMap;
	
	private Map<Integer, MapleSummon> summons;
	
	@Getter @Setter
	private int battleshipHp;
	
	private EnumMap<MapleBuffStat, MapleBuffStatValueHolder> effects;
	
	@Getter
	private MapleMount mount;
	
	private long lastDoorCreated;
	
	private int lastPortalId;
	
	@Getter
	private List<MapleMagicDoor> magicDoors;
	
	private Map<Integer, CooldownValueHolder> cooldowns;
	
	private long lastFame;
	private Map<Integer, Long> fameThisMonth;
	
	private Map<MapleDisease, MapleTask> diseases;
	
	private String lastHint;
	private long lastHintTime;
	
	@Getter
	private boolean godModeEnabled;
	
	private Map<Integer, MapleQuestInstance> quests;
	
	@Getter
	private MapleShop openShop;
	
	@Getter
	private MapleParty party;
	
	private List<Integer> partyInvites;
	
	private int guildId;
	
	@Getter
	private boolean cashShopOpen;
	
	@Getter @Setter
	private MapleScriptInstance activeNpc;
	@Getter @Setter
	private NpcConversationManager activeNpcConversation;
	
	@Getter @Setter
	private MapleStorageBox openStorageBox;
	
	@Getter
	private boolean dueyOpen;
	
	private List<MapleNote> notes;
	
	private List<DueyParcel> parcels;
	
	private MapleTrade openTrade;
	
	private PartyQuestProgress pqProgress;
	
	@Getter
	private CRand32 damageNumberGenerator;
	
	@Getter
	private MaplePetInstance[] pets;
	
	@Getter @Setter
	private int activeChair;
	
	public MapleCharacter(MapleClient client) {
		this.client = client;
		inventories = new HashMap<>();
		keyBindings = new HashMap<>();
		godModeEnabled = false;
		activeChair = 0;
		quests = new HashMap<>();
		
        for (InventoryType type : InventoryType.values()) {
            byte b = 48;
            if (type == InventoryType.CASH) {
                b = 96;
                inventories.put(type, new MapleCashInventory(this, b));
            }else if(type == InventoryType.EQUIPPED){
            	inventories.put(type, new MapleEquippedInventory(this, b));
            }else{
            	inventories.put(type, new MapleInventory(this, b, type));
            }
            
        }
        controlled = Collections.synchronizedMap(new HashMap<>());
        mapId = -1;
        skills = new HashMap<>();
        summons = new HashMap<>();
        effects = new EnumMap<>(MapleBuffStat.class);
        lastPortalId = 0;
        magicDoors = new ArrayList<>();
        cooldowns = Collections.synchronizedMap(new HashMap<>());
        fameThisMonth = new HashMap<>();
        diseases = new HashMap<>();
        partyInvites = new ArrayList<>();
        guildId = -1;
        name = "";
        notes = new ArrayList<>();
        damageNumberGenerator = new CRand32();
        pets = new MaplePetInstance[3];
	}
	
	public int getStr(){
		return calculateStatsWithBonuses()[0];
	}
	
	public int getDex(){
		return calculateStatsWithBonuses()[1];
	}
	
	public int getLuk(){
		return calculateStatsWithBonuses()[2];
	}
	
	public int getInt(){
		return calculateStatsWithBonuses()[3];
	}
	
	public int getMaxHp(){
		return calculateStatsWithBonuses()[4];
	}
	
	public int getMaxMp(){
		return calculateStatsWithBonuses()[5];
	}
	
	public int getWeaponAttack(){
		return calculateStatsWithBonuses()[6];
	}
	
	public int getMagicAttack(){
		return calculateStatsWithBonuses()[7];
	}
	
	public double getWeaponMastery(MapleWeaponType type){
		
		double mastery = 0;
		
		for(int skillId : type.getMasterySkills()){
			int level = getSkillLevel(skillId);
			if(level == 0){
				continue;
			}
			
			mastery += 0.15D;
			
			for(int i = 0; i <= level;i++){
				if(i < 2)
				{
					continue;
				}
				
				if(i % 2 == 1){
					mastery += 0.05D;
				}
			}
			
		}
		
		return Math.max(0.1D, mastery);
		
	}
	
/*	public static void main(String[] args) {
		for(int level = 0; level < 20;level++){

			double mastery = 0D;
			
			do{
				if(level == 0){
					break;
				}
				
				mastery += 0.15D;
				
				for(int i = 0; i <= level;i++){
					if(i < 2)
					{
						continue;
					}
					
					if(i % 2 == 1){
						mastery += 0.05D;
					}
				}
			}while(false);
			
			System.out.println("Level "+level+" mastery "+(mastery * 100D)+"%");
		}
	}*/
	
	/*
	 *  mindmg = ((((matk * matk) / 1000) + matk * (mast / 100) * 0.9) / 30 + int / 200) * basicattack * (amp / 100);
	 */
	
	public int getMinDamage(){
		
		int min = 0;
		int atk = getWeaponAttack();
		
		Item weapon = getInventory(InventoryType.EQUIPPED).getItem(-11);
		
		if(weapon != null){
			MapleWeaponType wType = ItemInfoProvider.getWeaponType(weapon.getItemId());
			
			int main = wType.getMainStat(this);
			int secondary = wType.getSecondaryStat(this);
			
			double dmin = (main * wType.getMaxDamageMultiplier() * 0.9 * (getWeaponMastery(wType) * 100D)) / 100D + secondary;
			dmin /= 100D;
			dmin *= atk;
			min = (int) dmin;
		}
		
		
		return min;
	}
	
	/*
	 *  maxdmg = ((((matk * matk) / 1000) + matk) / 30 + int / 200) * basicattack * (amp / 100);
           
	 */
	
	public int getMaxDamage(){
		int max = 0;
		
		Item weapon = getInventory(InventoryType.EQUIPPED).getItem(-11);
		
		if(weapon != null){
			MapleWeaponType wType = ItemInfoProvider.getWeaponType(weapon.getItemId());
			
			if(wType != MapleWeaponType.WAND && wType != MapleWeaponType.STAFF){
				int atk = getWeaponAttack();
				int main = wType.getMainStat(this);
				int secondary = wType.getSecondaryStat(this);
				
				max = (int) (((wType.getMaxDamageMultiplier() * main + secondary) / 100.0) * atk);	
			}else{
				int matk = getMagicAttack();
				int ba = 1;//Basic Attack, wtf is this
				double dmax = Math.pow(matk, 2D) / 1000D + matk / 30D;
				
				dmax += (getInt() / 200D);
				dmax *= ba;
				
			}
			
		}
		
		return max;
	}
	
	public int getBaseStr(){
		return str;
	}
	
	public int getBaseDex(){
		return dex;
	}
	
	public int getBaseLuk(){
		return luk;
	}
	
	public int getBaseInt() {
		return int_;
	}
	
	public int getBaseMaxHp(){
		return maxHp;
	}
	
	public int getBaseMaxMp(){
		return maxMp;
	}
	
/*	public int getStr(){
		int strength = str;
		
		
		return strength;
	}*/
	
	/**
	 * Calculates the players stats with bonuses
	 * <p>
	 * array[0] => str<br>
	 * array[1] => dex<br>
	 * array[2] => luk<br>
	 * array[3] => int<br>
	 * array[4] => max hp<br>
	 * array[5] => max mp<br>
	 * array[6] => watk<br>
	 * array[7] => matk
	 * </p>
	 * @return an array of the players stats with bonuses
	 */
	private int[] calculateStatsWithBonuses(){
		
		final int STR = 0, DEX = 1, LUK = 2, INT = 3,
				HP = 4, MP = 5, WATK = 6, MATK = 7;
		
		int[] stats = new int[8];
		
		stats[STR] = getBaseStr();
		stats[DEX] = getBaseDex();
		stats[LUK] = getBaseLuk();
		stats[INT] = getBaseInt();
		stats[HP] = getBaseMaxHp();
		stats[MP] = getBaseMaxMp();
		stats[WATK] = 0;
		stats[MATK] = getBaseInt();
		
		for(Item item : getInventory(InventoryType.EQUIPPED).getItems().values()){
			if(item instanceof EquipItem){
				EquipItem equip = (EquipItem) item;
				stats[STR] += equip.getStr();
				stats[DEX] += equip.getDex();
				stats[LUK] += equip.getLuk();
				stats[INT] += equip.getInt();
				stats[HP] += equip.getHp();
				stats[MP] += equip.getMp();
				stats[WATK] += equip.getWeaponAttack();
				stats[MATK] += equip.getInt() + equip.getMagicAttack();
			}
		}
		
		stats[MATK] = Math.min(stats[MATK], 2000);
		
		stats[HP] += ((double) getBuffedValue(MapleBuffStat.HYPERBODYHP) / 100.0D) * stats[HP];
		stats[MP] += ((double) getBuffedValue(MapleBuffStat.HYPERBODYMP) / 100.0D) * stats[MP];
		
		stats[HP] = Math.min(30000, stats[HP]);
		stats[MP] = Math.min(30000, stats[MP]);
		
		stats[WATK] += getBuffedValue(MapleBuffStat.WATK) + getBuffedValue(MapleBuffStat.ARAN_COMBO);
		
		stats[STR] += getBaseStr() * getBuffedValue(MapleBuffStat.MAPLE_WARRIOR) / 100;
		stats[DEX] += getBaseDex() * getBuffedValue(MapleBuffStat.MAPLE_WARRIOR) / 100;
		stats[LUK] += getBaseLuk() * getBuffedValue(MapleBuffStat.MAPLE_WARRIOR) / 100;
		stats[INT] += getBaseInt() * getBuffedValue(MapleBuffStat.MAPLE_WARRIOR) / 100;
		
		
		
		if(job.isA(MapleJob.BOWMAN)){
			Skill expert = null;
			
			if(job.isA(MapleJob.MARKSMAN)){
				expert = SkillFactory.getSkill(Marksman.MARKSMAN_BOOST);
			}else if(job.isA(MapleJob.BOWMASTER)){
				expert = SkillFactory.getSkill(Bowmaster.BOW_EXPERT);
			}
			
			if(expert != null){
				int level = getSkillLevel(expert);
				
				if(level > 0){
					stats[WATK] += expert.getEffect(level).getX();
				}
			}
			
		}
		
		stats[MATK] += getBuffedValue(MapleBuffStat.MATK);
		
		int blessing = getSkillLevel(10000000 * (job.getId() / 1000) + 12);
		
		if(blessing > 0){
			stats[WATK] += blessing;
			stats[MATK] += blessing * 2;
		}
		
		stats[WATK] += getWeaponConsumableAttackBonus();
		
		return stats;
	}
	
	/**
	 * Calculates the weapon attack bonus from a players throwing stars or bullets
	 * @return the watk bonus
	 */
	private int getWeaponConsumableAttackBonus(){
		
		if(job.isConsumableUsedOnAttack()){
			
			Item weapon = getInventory(InventoryType.EQUIPPED).getItem(-11);

			if(weapon != null){
				MapleWeaponType type = ItemInfoProvider.getWeaponType(weapon.getItemId());
				
				if(type != null){
					if(type.isConsumableUsedOnAttack()){
						Inventory useInventory = getInventory(InventoryType.USE);
						
						int projectileId = useInventory.getProjectileId(0, weapon);
						
						if(projectileId == 0){
							return 0;
						}
						
						return ItemInfoProvider.getProjectileWatkBonus(projectileId);
					}
				}
			}
			
		}

		return 0;
	}
	
	public void setDueyOpen(boolean dueyOpen) {
		this.dueyOpen = dueyOpen;
		if(!dueyOpen){
			parcels = null;
		}
	}
	
	public List<DueyParcel> getDueyParcels() throws SQLException {
		
		if(parcels != null){
			return parcels;
		}
		
		parcels = new ArrayList<>();
		
		MapleDatabase.getInstance().execute("DELETE FROM `duey_packages` WHERE `expiration_time` <= ?", System.currentTimeMillis());
		
		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT * FROM `duey_packages` WHERE `recipient`=?", getId());
		
		for(QueryResult result : results){
			
			Item item = null;
			int itemId = result.get("item");
			
			if(itemId >= 0){
				item = ItemFactory.getItem(result.get("item"), result.get("item_amount"));
				
				if(item instanceof EquipItem){
					EquipItem eq = (EquipItem) item;
					String data = result.get("item_data");
					eq.getStatInfo().deserialize(data);
				}
				
			}
			
			DueyParcel parcel = new DueyParcel(result.get("id"), result.get("sender"), result.get("mesos"), item, result.get("expiration_time"), result.get("message"));
			
			
			parcels.add(parcel);
		}
		
		return parcels;
		
	}
	
	public void sendNote(MapleNote note){
		notes.add(note);
		
		client.sendPacket(PacketFactory.showNotes(notes));
	}
	
	public void openDuey(){
		try {
			client.sendPacket(PacketFactory.openDuey(getDueyParcels()));
			dueyOpen = true;
		} catch (SQLException e) {
			e.printStackTrace();
			sendMessage(MessageType.POPUP, "An error occured while opening duey.");
		}
	}
	
	public void openCashshop(){
		if(!cashShopOpen){
			getMap().removePlayer(this);
			pauseCooldowns();
			cashShopOpen = true;
			client.sendPacket(PacketFactory.openCashshop(this));
			client.sendPacket(PacketFactory.updateCashshopCash(CashShopWallet.getWallet(client.getCharacter())));
		}else{
			throw new RuntimeException("Cannot open cashshop when we are already inside it");
		}
	}
	
	public void closeCashshop(){
		if(cashShopOpen){
			try {
				client.changeChannel(client.getChannelId());
				cashShopOpen = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			throw new RuntimeException("Cannot close cashshop when we aren't inside it");
		}
	}
	
	public MapleGuild getGuild(){
		if(client.getWorld() == null){
			return null;
		}
		return client.getWorld().getGuild(guildId);
	}

	public boolean joinGuild(MapleGuild guild){
		if(getGuild() != null){
			return false;
		}
		
		if(guild.isMember(this)){
			guildId = guild.getGuildId();
			
			if(client.getLoginStatus() == LoginStatus.IN_GAME){
				client.sendPacket(PacketFactory.guildUpdateInfo(this, guild));	
			}
			return true;
		}else{
			guild.addMember(this, MapleGuildRankLevel.MEMBER_1);
			return true;
		}
		
	}
	
	
	public boolean leaveGuild(){
		MapleGuild guild = getGuild();
		if(guild == null){
			return false;
		}
		
		guild.removeMember(this);
		guildId = -1;
		
		if(client.getLoginStatus() == LoginStatus.IN_GAME){
			client.sendPacket(PacketFactory.guildUpdateInfo(this, null));	
		}
		
		return true;
	}
	
	public boolean createParty(){
		if(party != null){
			client.sendPacket(PacketFactory.partyStatus(PartyOperationType.ALREADY_IN_PARTY));
			return false;
		}
		if(getLevel() < 10 && !ServerConstants.ALLOW_BEGINNER_PARTIES){
			getClient().sendPacket(PacketFactory.partyStatus(PartyOperationType.CANT_CREATE_BEGINNER));
			return false;
		}
		party = MapleParty.createParty(this);
		client.sendPacket(PacketFactory.partyCreate(this));
		return true;
	}
	
	/**
	 * Fails silently if the player isn't in a party
	 */
	public void leaveParty(){
		if(party != null){
			party.leave(this);
			party = null;	
		}
	}
	
	public void warpToFreeMarket(){
		if(getMapId() == 910000000){
			return;
		}
		
		setFmReturnMap(getMapId());
		changeMap(910000000);
		
	}
	
	public boolean inviteToParty(MapleCharacter source){
		if(party != null){
			source.getClient().sendPacket(PacketFactory.partyStatus(PartyOperationType.ALREADY_IN_PARTY));
			return false;
		}
		
		client.sendPacket(PacketFactory.partyInvite(source));
		partyInvites.add(source.getParty().getPartyId());
		return true;
	}

	public void searchForExistingParty() {
		for(MapleParty party : getClient().getWorld().getParties()){
			if(party.isMember(this)){
				this.party = party;
				party.getEntry(this).updatePlayerReference(this);
				party.updateMember(this);
			}
		}
	}
	
	public void searchForExistingGuild(){
		for(MapleGuild guild : getClient().getWorld().getGuilds()){
			if(guild.isMember(this)){
				this.guildId = guild.getGuildId();
				guild.getEntry(this).updatePlayerReference(this);
				break;
			}
		}
	}
	
	public boolean joinParty(MapleParty targetParty) {
		if(party != null){
			leaveParty();
		}
		
		party = targetParty;
		targetParty.addPlayer(this);
		
		return true;
	}
	
	public boolean isInvitedToParty(MapleParty party){
		return partyInvites.contains(party.getPartyId());
	}
	
	public MapleCharacterSnapshot createSnapshot(){
		return new MapleCharacterSnapshot(this);
	}
	
	public Collection<MapleSummon> getSummons(){
		return summons.values();
	}
	
	public MapleSummon getSummon(int sourceid){
		return summons.get(sourceid);
	}
	
	public MapleSummon getSummonByObjectId(int oid){
		for(MapleSummon s : getSummons()){
			if(s.getObjectId() == oid){
				return s;
			}
		}
		
		return null;
	}
	
	public void addCooldown(Skill skill){
		int level = getSkillLevel(skill);
		
		addCooldown(skill, level);
	}
	
	public void addCooldown(Skill skill, int level){
		
		MapleStatEffect effect = skill.getEffect(level);
		
		int cooldownTime = effect.getCooldown();//This is in seconds!!!!!
		
		if(cooldownTime == 0){
			return;
		}
		
		
		addCooldown(skill.getId(), cooldownTime * 1000);
		
	}
	
	public void addCooldown(int id, long delay){
		
		MapleTask cdFinish = null;
		
		if(client.getLoginStatus() == LoginStatus.IN_GAME){
			cdFinish = TimerManager.schedule(new Runnable() {
				
				@Override
				public void run() {
					clearCooldown(id);
				}
			}, delay);
			getClient().sendPacket(PacketFactory.skillCooldown(id, (int) (delay / 1000)));	
		}
		
		synchronized (cooldowns) {
			cooldowns.put(id, new CooldownValueHolder(cdFinish, delay));
		}
		
	}
	
	@Data
	public static class CooldownValueHolder {
		
		private MapleTask cancelTask;
		private long timeLeft;
		private long cancelTaskStartTime;


		public CooldownValueHolder(MapleTask cdFinish, long delay) {
			cancelTask = cdFinish;
			timeLeft = delay;
		}
		
		
		public void cancel() {
			if(cancelTask != null){
				cancelTask.cancel(false);
				cancelTask = null;
			}
		}

		public void pauseTask(){
			if(cancelTask != null){
				cancelTask.cancel(false);
				timeLeft -= (System.currentTimeMillis() - cancelTaskStartTime);
			}
		}
		
		public void startTask(MapleCharacter chr, int skillId) {
			if(cancelTask == null){
				cancelTask = TimerManager.schedule(new Runnable() {
					
					@Override
					public void run() {
						chr.clearCooldown(skillId);
					}
				}, timeLeft);
				cancelTaskStartTime = System.currentTimeMillis();
			}
		}
		
	}
	
	public boolean canCreateMagicDoor(){
		return System.currentTimeMillis() - lastDoorCreated >= 5000;
	}
	
	public void resetMagicDoorTimer(){
		lastDoorCreated = System.currentTimeMillis();
	}
	
	public PartyQuestProgress getPartyQuestProgress(){
		if(pqProgress != null){
			return pqProgress;
		}
		return (pqProgress = PartyQuestProgress.getPartyQuestProgress(getId()));
	}
	
	public void controlMonster(MapleMonster monster) {
		monster.setController(this);
		
		client.sendPacket(PacketFactory.getMonsterControlPacket(monster, monster.hasAggro()));
		
		synchronized (controlled) {
			controlled.put(monster.getId(), monster);
		}
	}
	
	public Collection<MapleMonster> getControlledMonsters(){
		synchronized (controlled) {
			return new ArrayList<>(controlled.values());
		}
	}
	
	public Inventory getInventory(InventoryType type){
		return inventories.get(type);
	}
	
	public Inventory getInventory(int itemId){
		return getInventory(InventoryType.getByItemId(itemId));
	}
	
	public MapleClient getClient() {
		return client;
	}
	
	public MapleMap getMap(){
		
		if(getClient().getChannel() == null){
		/*	return MapleServer.getWorld(0).getChannelById(1).getMapFactory().getMap(mapId);*/
			throw new IllegalStateException("NOT IN A WORLD OR CHANNEL! "+getClient().getWorldId()+" "+getClient().getChannelId()+" "+getName()+" "+getMapId());
		}
		
		return getClient().getChannel().getMapFactory().getMap(getMapId());
	}
	
	public String getFullName(){
		
		String medal = "";
		
		Item medalItem = getInventory(InventoryType.EQUIPPED).getItem((short) -49);
		
		if(medalItem != null){
			medal = "<"+ItemInfoProvider.getItemName(medalItem.getItemId())+"> ";
		}
		
		return medal + getName();
	}
	
	public void setHair(int hair) {
		this.hair = hair;
		updateStat(MapleStat.HAIR, hair);
		respawnPlayerForOthers();
	}
	
	public void setSkinColor(int skinColor) {
		this.skinColor = skinColor;
		updateStat(MapleStat.SKIN, skinColor);
		respawnPlayerForOthers();
	}
	
	public void setFace(int face) {
		this.face = face;
		updateStat(MapleStat.FACE, face);
		respawnPlayerForOthers();
	}
	
	public void loadFromQuery(QueryResult result){
		
		String name = result.get("name");
		int level = result.get("level");
		int id = result.get("id");
		int job = result.get("job");
		int hair = result.get("hair");
		int face = result.get("face");
		int skincolor = result.get("skincolor");
		
		int gender = result.get("gender");
		
		int str = result.get("str");
		int dex = result.get("dex");
		int luk = result.get("luk");
		int int_ = result.get("int_");
		int meso = result.get("meso");
		int mapId = result.get("map");
		
		int fame = result.get("fame");
		
		int hp = result.get("hp");
		int maxhp = result.get("maxhp");
		
		int mp = result.get("mp");
		int maxmp = result.get("maxmp");
		
		int exp = result.get("exp");
		
		int ap = result.get("ap");
		int sp = result.get("sp");
		
		int fmReturn = result.get("fm_return_map");
		
		accountId = result.get("owner");
		
		lastPortalId = result.get("last_portal");
		
		setGender(gender);
		
		setId(id);
		setName(name);
		this.job = MapleJob.getById(job);
		
		this.level = level;
		this.exp = exp;
		
		setHair(hair);
		setFace(face);
		setSkinColor(skincolor);
		
		this.str = str;
		this.dex = dex;
		this.luk = luk;
		this.int_ = int_;
		this.remainingAp = ap;
		this.remainingSp = sp;
		this.fmReturnMap = fmReturn;
		
		this.fame = fame;
		
		this.meso = (int) meso;
		
		setMapId(mapId);

		this.maxHp = maxhp;
		this.hp = hp;

		this.maxMp = maxmp;
		this.mp = mp;
		
		try {
			loadItems(client.getLoginStatus() == LoginStatus.LOGGED_IN);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		int mount_tiredness = result.get("mount_tiredness");
		int mount_exp = result.get("mount_exp");
		int mount_level = result.get("mount_level");
		
		if(mount != null){
			mount.setTiredness(mount_tiredness);
			mount.setExp(mount_exp);
			mount.setLevel(mount_level);
		}
		
		lastFame = result.get("last_fame");
		
	}
	
	public int getSkillLevel(Skill skill){
		if(skill == null){
			return 0;
		}
		return getSkillLevel(skill.getId());
	}
	
	public int getSkillLevel(int skillId){
		return skills.getOrDefault(skillId, SkillData.EMPTY).getLevel();
	}
	
	public int getMasterLevel(int skillId) {
		return skills.getOrDefault(skillId, SkillData.EMPTY).getMasterLevel();
	}
	
	public int getMasterLevel(Skill skill){
		if(skill == null){
			return 0;
		}
		return getMasterLevel(skill.getId());
	}
	
	public void setRemainingAp(int remainingAp) {
		this.remainingAp = remainingAp;
		
		updateStat(MapleStat.AVAILABLEAP, remainingAp);
	}
	
	public void setRemainingSp(int remainingSp) {
		this.remainingSp = remainingSp;
		
		updateStat(MapleStat.AVAILABLESP, remainingSp);
	}

	public boolean canDropItems(){
		return true;
	}
	
	public void changeJob(MapleJob job){
		
		if(this.job.isBeginnerJob() && !job.isBeginnerJob()){
			
			int pointsToGive = 0;
			pointsToGive += str - 12;
			pointsToGive += dex - 5;
			
			setStr(12);
			setDex(5);
			
			setRemainingAp(remainingAp + pointsToGive);
			
		}
		
		remainingSp++;
		
		if(job.getId() % 10 == 2){
			remainingSp += 2;
		}
		
		if(job.getId() % 10 > 1){
			remainingAp += 5;
		}
		
		if(job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)){
			maxHp += Randomizer.rand(200, 250);
		}else if(job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)){
			maxMp += Randomizer.rand(100, 150);
		}else if(job.getId() % 1000 % 100 == 0){
			maxHp += Randomizer.rand(100, 150);
			maxHp += Randomizer.rand(25, 50);
		}else if(job.getId() % 1000 < 300){
			maxMp += Randomizer.rand(450, 500);
		}else if(job.getId() % 1000 > 0 && job.getId() % 1000 != 1000){
			maxHp += Randomizer.rand(300, 350);
            maxMp += Randomizer.rand(150, 200);
		}
		
		if (maxHp >= 30000) {
            maxHp = 30000;
        }
        if (maxMp >= 30000) {
            maxMp = 30000;
        }
		
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(5);
        statup.add(new Pair<>(MapleStat.MAXHP, Integer.valueOf(maxHp)));
        statup.add(new Pair<>(MapleStat.MAXMP, Integer.valueOf(maxMp)));
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp));
        statup.add(new Pair<>(MapleStat.JOB, Integer.valueOf(job.getId())));
		client.sendPacket(PacketFactory.updatePlayerStats(statup, false));
        
		getMap().broadcastPacket(PacketFactory.getShowForeignEffect(getId(), 8), getId());
		this.job = job;
	}
	
	public void setJob(MapleJob job){
		if(job == null){
			throw new IllegalArgumentException("job may not be null");
		}
		this.job = job;
		updateParty();
		updateStat(MapleStat.JOB, job.getId());
		
		if(client.getLoginStatus() == LoginStatus.IN_GAME){
			if(getMap() != null) {
				getMap().broadcastPacket(PacketFactory.getShowForeignEffect(getId(), 8), getId());
			}
		}
	}
	
	private void updateParty(){
		if(party != null){
			party.updateMember(this);
		}
	}
	
	public void setKeybinding(int keyId, int type, int action){
		keyBindings.put(keyId, new Pair<>(type, action));
		
		try{
			
			for(int i = 0; i < DEFAULT_KEY.length;i++){
				if(DEFAULT_KEY[i] == keyId){
					if(DEFAULT_ACTION[i] == action && DEFAULT_TYPE[i] == type){
						MapleDatabase.getInstance().execute("DELETE FROM `keybindings` WHERE `character`=? AND `key`=?", getId(), keyId);
						return;
					}
				}
			}
			
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT COUNT(*) FROM `keybindings` WHERE `character`=? AND `key`=?", getId(), keyId);
			
			if(results.get(0).getCountResult() == 0){
				MapleDatabase.getInstance().execute("INSERT INTO `keybindings` (`character`, `key`, `type`, `action`) VALUES (?, ?, ?, ?)", getId(), keyId, type, action);
			}else{
				MapleDatabase.getInstance().execute("UPDATE `keybindings` SET `action`=?,`type`=? WHERE `character`=? AND `key`=?", action, type, getId(), keyId);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		
	}
	
	public void setHp(int hp){
		if(hp < 0){
			hp = 0;
		}else if(hp > getMaxHp()){
			hp = getMaxHp();
		}
		this.hp = hp;
		updateStat(MapleStat.HP, hp);
		updatePartyHp();
	}
	
	private void updatePartyHp(){
		if(getParty() != null){
			getParty().broadcastPacket(PacketFactory.updatePartyMemberHp(this), id);
			for(PartyEntry entry : getParty().getMembers()){
				if(entry.getSnapshot().getMapId() == getMapId()){
					if(entry.getSnapshot().isOnline()){
						client.sendPacket(PacketFactory.updatePartyMemberHp(entry.getSnapshot().getLiveCharacter()));
					}
				}
			}
		}
	}
	
	public void setMp(int mp){
		this.mp = Math.max(0, Math.min(getMaxMp(), mp));
		updateStat(MapleStat.MP, mp);
	}
	
	public void setMaxHp(int maxHp){
		this.maxHp = maxHp;
		updateStat(MapleStat.MAXHP, maxHp);
	}
	
	public void setMaxMp(int maxMp){
		this.maxMp = maxMp;
		updateStat(MapleStat.MAXMP, maxMp);
	}
	
	public void setMapId(int id){
		if(this.mapId != id){
			this.mapId = id;
			updateParty();	
		}
	}
	
    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter(c);
        ret.client = c;
        ret.hp = 50;
        ret.maxHp = 50;
        ret.mp = 5;
        ret.maxMp = 5;
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountId = c.getId();
        ret.mapId = 0;
        //ret.buddylist = new BuddyList(20);
        ret.mount = null;
        ret.getInventory(InventoryType.EQUIP).setMaxSize(96);
        ret.getInventory(InventoryType.USE).setMaxSize(96);
        ret.getInventory(InventoryType.SETUP).setMaxSize(96);
        ret.getInventory(InventoryType.ETC).setMaxSize(96);
        for (int i = 0; i < DEFAULT_KEY.length; i++) {
            ret.keyBindings.put(DEFAULT_KEY[i], new Pair<Integer, Integer>(DEFAULT_TYPE[i], DEFAULT_ACTION[i]));
        }
        //to fix the map 0 lol
        /*for (int i = 0; i < 5; i++) {
            ret.trockmaps[i] = 999999999;
        }
        for (int i = 0; i < 10; i++) {
            ret.viptrockmaps[i] = 999999999;
        }*/
        return ret;
    }
    
    public void loadKeybindings(){
    	
    	for (int i = 0; i < DEFAULT_KEY.length; i++) {
			keyBindings.put(DEFAULT_KEY[i], new Pair<Integer, Integer>(DEFAULT_TYPE[i], DEFAULT_ACTION[i]));
		}
    	
    	try{
    		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `key`,`type`,`action` FROM `keybindings` WHERE `character`=?", getId());
    	
    		for(QueryResult result : results){
    			
    			int key = result.get("key");
    			int type = result.get("type");
    			int action = result.get("action");
    			
    			keyBindings.put(key, new Pair<>(type, action));
    			
    		}
    	}catch(SQLException e){
    		e.printStackTrace();
    	}
    	
    	
    }
    
    public void sendKeybindings(){
    	client.sendPacket(PacketFactory.getKeybindings(keyBindings));
    }
    
    public void sendMessage(MessageType type, String text){
    	client.sendPacket(PacketFactory.getServerMessagePacket(type, text, client.getChannelId(), false));
    }
    
    public World getWorld(){
    	return MapleServer.getWorld(world);
    }
    
    public int getWorldId(){
    	return world;
    }
    
    public void saveToDatabase(boolean create) throws SQLException{
    	
    	getWorld().getLogger().debug("Saving "+getName()+" to database.");
    	
    	if(create){
    		MapleDatabase.getInstance().execute("INSERT INTO `characters` (`name`, `owner`, `world`,`gender`) VALUES (?, ?, ?,?)", name, client.getId(), world, gender);
    		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `id` FROM `characters` WHERE `name`=?", name);
    		
    		if(results.size() > 0){
    			QueryResult result = results.get(0);
    			
    			id = result.get("id");
    			saveToDatabase(false);
    		}
    	}else{
    		
    		String script = "UPDATE `characters` SET name=?, map=?, meso=?, hair=?, face=?, skincolor=?, gender=?, job=?, level=?, fame=?, str=?, dex=?, luk=?, int_=?, hp=?, maxhp=?, mp=?, maxmp=?, exp=?, ap=?, sp=?, fm_return_map=?, mount_tiredness =?, mount_exp = ?, mount_level = ?, last_portal = ?, last_fame = ? WHERE `id`=?";
    		
    		if(mount == null){
    			mount = new MapleMount(this, 0, 0);
    		}
    		
    		MapleDatabase.getInstance().execute(script, name, mapId, meso, hair, face, skinColor, gender, job.getId(), level, fame, str, dex, luk, int_, hp, maxHp, mp, maxMp, exp, remainingAp, remainingSp, fmReturnMap, mount.getTiredness(), mount.getExp(), mount.getLevel(), lastPortalId, lastFame, id);
    		
    		saveInventory();
    		saveCooldowns();
    		saveQuests();
    		if(pqProgress != null){
    			pqProgress.saveToDatabase(getId());
    		}
    		
    		if(getOpenStorageBox() != null){
    			getOpenStorageBox().commitChanges(accountId);
    			setOpenStorageBox(null);
    		}
    		
    	}
    	
    }
    
    public void saveCooldowns() throws SQLException{
    	
    	for(int skillId : cooldowns.keySet()){
    		
    		long delay = getCooldownTimeLeft(skillId);/*cooldowns.get(skillId).getRight().getDelay(TimeUnit.MILLISECONDS);*/
    		
    		MapleDatabase.getInstance().execute("INSERT INTO `cooldowns` (`owner`, `skill_id`, `delay`) VALUES (?, ?, ?)", getId(), skillId, delay);
    		
    	}
    	
    }
    
    public void openStorage(int npc){
    	try {
    		MapleStorageBox box = MapleStorageBox.getStorage(client);
			openStorage(npc, box);
			openStorageBox = box;
		} catch (SQLException e) {
			e.printStackTrace();
			sendMessage(MessageType.POPUP, "Failed to load storage!");
		}
    }
    
    public void openStorage(int npc, MapleStorageBox storage){
    	client.sendPacket(PacketFactory.storageOpen(npc, storage));
    }
    
    public void loadCooldowns() throws SQLException{
    	
    	List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `skill_id`, `delay` FROM `cooldowns` WHERE `owner`=?", getId());
    	
    	for(QueryResult result : results){
    		int skillId = result.get("skill_id");
    		long delay = result.get("delay");
    		
    		addCooldown(skillId, delay);
    		getWorld().getLogger().debug("Loaded cooldown "+skillId+" "+delay);
    	}
    	
    	MapleDatabase.getInstance().execute("DELETE FROM `cooldowns` WHERE `owner`=?", getId());
    	
    }
    
    public void saveInventory() throws SQLException{
       
    	MapleDatabase.getInstance().execute("DELETE FROM `inventory_items` WHERE `player`=?", getId());
    	
        for (Inventory iv : inventories.values()) {
        	try{//Surround with try-catch because otherwise if an item causes an eror the player might lose all their items!
            	String script = "INSERT INTO `inventory_items` (`inventory_type`,`slot`,`player`,`itemid`,`amount`,`owner`,`flag`,`expiration`,`unique_id`) VALUES (?,?,?,?,?,?,?,?,?)";
            	String scriptWithData = "INSERT INTO `inventory_items` (`inventory_type`,`slot`,`player`,`itemid`,`amount`,`owner`,`flag`,`expiration`,`unique_id`,`data`) VALUES (?,?,?,?,?,?,?,?,?,?)";
            	
            	for(int slot : iv.getItems().keySet()){
            		Item item = iv.getItems().get(slot);
                	
                	long expirationDate = -1;
                	long unique_id = -1;

            		if(item instanceof CashItem){
            			expirationDate = ((CashItem)item).getExpirationDate();
            			unique_id = ((CashItem)item).getUniqueId();
            		}
            		
            		String data = null;
            		
            		if(item instanceof EquipItem){
            			EquipItem eq = (EquipItem) item;
            			
            			data = eq.getStatInfo().serialize().toString();
            		}
            		
            		if(item instanceof PetItem){
            			data = ((PetItem) item).createPetSnapshot().serialize();
            		}
            		
            		if(data != null){
            			MapleDatabase.getInstance().execute(scriptWithData, iv.getType().getId(), slot, getId(), item.getItemId(), item.getAmount(), item.getOwner(), item.getFlag(), expirationDate, unique_id, data);
            		}else{
            			MapleDatabase.getInstance().execute(script, iv.getType().getId(), slot, getId(), item.getItemId(), item.getAmount(), item.getOwner(), item.getFlag(), expirationDate, unique_id);
            		}
            	}	
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        	
        }
        
    }
    
	public static boolean checkNameTaken(String name) throws SQLException {
		
		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `id` FROM `characters` WHERE `name`=?", name);
		
		if(results.size() > 0){
			return true;
		}else{
			return false;
		}
		
	}
	
	public void loadSkills() throws SQLException{
		
		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `skillid`,`level` FROM `skills` WHERE `owner`=?", getId());
		
		for(QueryResult result : results){
			
			int skillId = result.get("skillid");
			int level = result.get("level");
			
			skills.put(skillId, new SkillData(level, 0));//TODO: Implement mastery
			
		}
		
	}

	/**
	 * Load this players items
	 * @param onlyLoadEquip if true then we will only load the players equip items. Used to speed up the character selection screen
	 * @throws SQLException
	 */
	public void loadItems(boolean onlyLoadEquip) throws SQLException {
		
		List<QueryResult> results = null;
		
		/*if(!onlyLoadEquip){
			results = MapleDatabase.getInstance().query("SELECT * FROM `inventory_items` LEFT JOIN `equipment_data` ON equipment_data.inventory_id=inventory_items.id WHERE `player`=?", getId());
		}else{
			results = MapleDatabase.getInstance().query("SELECT * FROM `inventory_items` LEFT JOIN `equipment_data` ON equipment_data.inventory_id=inventory_items.id WHERE `player`=? AND `inventory_type`=?", getId(), InventoryType.EQUIPPED.getId());
		}*/
		
		if(!onlyLoadEquip){
			results = MapleDatabase.getInstance().query("SELECT * FROM `inventory_items` WHERE `player`=?", getId());
		}else{
			results = MapleDatabase.getInstance().query("SELECT * FROM `inventory_items` WHERE `player`=? AND `inventory_type`=?", getId(), InventoryType.EQUIPPED.getId());
		}
		
		for(QueryResult result : results){
			Item item = ItemFactory.getItem(result);
			
			int invType = result.get("inventory_type");
			InventoryType inv = InventoryType.getById(invType);
			int slot = result.get("slot");
			
			if(item instanceof PetItem){
				PetItem pet = (PetItem) item;
				
				if(pet.isSummoned()){
					if(getNextPetSlot() >= 0){
						pets[getNextPetSlot()] = new MaplePetInstance(this, pet);
					}else{
						pet.setSummoned(false);
					}
				}
			}
			
			getInventory(inv).setItem(slot, item);	
		}
		
	/*	for(Inventory inventory : inventories.values()){
			
		}
		*/
		/*List<Pair<Item, MapleInventoryType>> items = ItemFactory.INVENTORY.loadItems(id, login);
		
		for(Pair<Item, MapleInventoryType> item : items){
			getInventory(item.getRight()).addFromDB(item.getLeft());
		}
		
        int mountid = getJob().getId() * 10000000 + 1004;
        if (getInventory(MapleInventoryType.EQUIPPED).getItem(-18) != null) {
            mount = new MapleMount(this, getInventory(MapleInventoryType.EQUIPPED).getItem(-18).getItemId(), mountid);
        } else {
            mount = new MapleMount(this, 0, mountid);
        }*/
		
	}
	
	public void loadQuests() throws SQLException{
		
		List<QueryResult> results = MapleDatabase.getInstance().query("SELECT `questid`,`state`,`forfeit`,`completion_time`,`npc` FROM `quests` WHERE `owner`=?", getId());
		
		List<QueryResult> progressResults = MapleDatabase.getInstance().query("SELECT `progress_id`,`progress`,`questid` FROM `quest_progress` WHERE `owner`=?", getId());
		
		for(QueryResult result : results){
			
			int id = result.get("questid");
			int state = result.get("state");
			int forfeit = result.get("forfeit");
			long completionTime = result.get("completion_time");
			int npc = result.get("npc");

			MapleQuestInstance inst = new MapleQuestInstance(MapleQuest.getQuest(id), forfeit, npc, MapleQuestStatus.getById(state));
			
			inst.setCompletionTime(completionTime);
			
			List<QueryResult> progress = progressResults.stream().filter(pr -> (int) pr.get("questid") == id).collect(Collectors.toList());
			
			for(QueryResult progressResult : progress){
				int p_id = progressResult.get("progress_id");
				int p_value = progressResult.get("progress");
				
				inst.setProgress(p_id, p_value);
			}
			
			quests.put(id, inst);
			
		}
		
		MapleDatabase.getInstance().execute("DELETE FROM `quests` WHERE `owner`=?", getId());
		MapleDatabase.getInstance().execute("DELETE FROM `quest_progress` WHERE `owner`=?", getId());
		
	}
	
	public void saveQuests() throws SQLException {
		
		for(MapleQuestInstance inst : quests.values()){
			
			MapleDatabase.getInstance().execute("INSERT INTO `quests` (`owner`,`questid`,`state`,`forfeit`,`completion_time`,`npc`) VALUES (?, ?, ?, ? , ? ,?)", getId(), inst.getQuest().getId(), inst.getStatus().getId(), inst.getForfeits(), inst.getCompletionTime(), inst.getNpc());
			
			for(int id : inst.getProgress().keySet()){
				int value = inst.getProgress(id);
				
				MapleDatabase.getInstance().execute("INSERT INTO `quest_progress` (`owner`, `questid`, `progress_id`, `progress`) VALUES (?,?,?,?)", getId(), inst.getQuest().getId(), id, value);
			}
			
		}
		
	}

	public static MapleCharacter loadFromDb(int cid, MapleClient client2) throws SQLException {
		List<QueryResult> result = MapleDatabase.getInstance().query("SELECT * FROM `characters` WHERE `id`=?", cid);
		
		if(result.size() == 0){
			throw new IllegalArgumentException("No character could be found with id "+cid);
		}
		
		MapleCharacter chr = new MapleCharacter(client2);
		chr.loadFromQuery(result.get(0));
		chr.loadSkills();
		chr.loadKeybindings();
		chr.loadCooldowns();
		chr.loadQuests();
		
		chr.searchForExistingParty();
		chr.searchForExistingGuild();
		
		return chr;
	}
	
	@Override
	public int getObjectId() {
		return getId();
	}

	public void newClient(MapleClient client) {
		this.client = client;
	}
	
	public void respawnPlayerForOthers(){
		if(getMap() != null){
			
			MapleMap map = getMap();
			
			map.broadcastPacket(PacketFactory.removePlayerFromMap(getId()), getId());
			map.broadcastPacket(PacketFactory.spawnPlayerMapObject(this), getId());
			
		}
	}

	@Override
	public void sendSpawnData(MapleClient client) {
		client.sendPacket(PacketFactory.spawnPlayerMapObject(this));
		for(int i = 0; i < pets.length;i++){
			MaplePetInstance inst = pets[i];
			
			if(inst != null){
				client.sendPacket(PacketFactory.spawnPet(this, inst, i));
			}
		}
	}

	@Override
	public void sendDestroyData(MapleClient client) {
		client.sendPacket(PacketFactory.removePlayerFromMap(getId()));
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.PLAYER;
	}
	
	public void changeMap(MapleMap to){
		changeMap(to, to.getFallbackPortal());
	}

	public void changeMap(MapleMap to, MaplePortal pto) {
		if(getMap() != null){
			getMap().removePlayer(this);
		
			for(MapleSummon summon : getSummons()){
				getMap().removeObject(summon.getObjectId());
			}
		}
		
		synchronized (controlled) {
			controlled.clear();
		}
		
		setMapId(to.getMapId());
		
		setPosition(pto.getPosition());
		
		MapleMap map = getMap();
		
		client.sendPacket(PacketFactory.getWarpToMap(to, pto == null ? 0 : pto.getId(), this));
		
		map.addPlayer(this);
		
		for(MapleSummon summon : getSummons()){
			summon.setPosition(pto.getPosition());
			map.spawnSummon(summon);
		}
		
		lastPortalId = pto.getId();
		updatePartyHp();
	}

	public void chat(String text, byte show) {
		getMap().broadcastPacket(PacketFactory.getGeneralChatPacket(getId(), text, isGm(), show));
	}

	public void chat(String text, GroupChatType type){
		if(type == GroupChatType.BUDDY){
			//Do nothing yet...
		}else if(type == GroupChatType.GUILD){
			MapleGuild guild = getGuild();
			if(guild != null){
				guild.broadcastPacket(PacketFactory.groupChat(getName(), text, type), getId());
			}
		}else if(type == GroupChatType.PARTY){
			if(getParty() != null){
				getParty().broadcastPacket(PacketFactory.groupChat(getName(), text, type), getId());
			}
		}
	}
	
	public void uncontrolMonster(MapleMonster monster) {
		monster.setController(null);
		synchronized (controlled) {
			controlled.remove(monster.getId());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof MapleCharacter){
			if(((MapleCharacter) obj).getId() == getId()){
				return true;
			}
		}
		
		return super.equals(obj);
	}

	public boolean isAlive() {
		return hp > 0;
	}

	public void damage(int damage) {
		if(godModeEnabled){
			return;
		}
		setHp(hp - damage);
		
		updateStat(MapleStat.HP, hp);
	}

	public void restoreHp(int amount) {
		setHp(hp + amount);
	}
	
	public void restoreMp(int amount) {
		setMp(mp + amount);
	}
	
	public void updateStat(MapleStat stat, int value){
		if(client.getLoginStatus() == LoginStatus.IN_GAME){
			List<Pair<MapleStat, Integer>> stats = new ArrayList<>();
			
			stats.add(new Pair<>(stat, value));
			
			client.sendPacket(PacketFactory.updatePlayerStats(stats, false));
		}
	}

	public void setExp(int exp){
		this.exp = exp;
		
		updateStat(MapleStat.EXP, exp);
	}
	
	public void setLevel(int level){
		this.level = level;
		updateParty();
		updateStat(MapleStat.LEVEL, level);
	}
	
	public void setStr(int str){
		this.str = str;
		updateStat(MapleStat.STR, str);
	}
	
	public void setDex(int dex){
		this.dex = dex;
		updateStat(MapleStat.DEX, dex);
	}
	
	public void setLuk(int luk){
		this.luk = luk;
		updateStat(MapleStat.LUK, luk);
	}
	
	public void setInt(int int_){
		this.int_ = int_;
		updateStat(MapleStat.INT, int_);
	}
	
	public void setMeso(int meso) {
		this.meso = meso;
		
		updateStat(MapleStat.MESO, meso);
	}
	
	public void giveExp(int amount, int partyBonus) {
		if(level == getMaxLevel()){
			return;
		}
		client.sendPacket(PacketFactory.getShowExpGain(amount, partyBonus, 0, false, true));
		giveExpInternal(amount);
	}
	
	public void giveExp(int amount) {
		giveExp(amount, 0);
	}
	
	private void giveExpInternal(int amount){
		int needed = ExpTable.getExpNeededForLevel(level);
		if(exp + amount < needed){
			setExp(exp + amount);
		}else{
			
			int totalExp = exp + amount;
			
			while(totalExp >= ExpTable.getExpNeededForLevel(level)){
				
				totalExp -= ExpTable.getExpNeededForLevel(level);
				setExp(ExpTable.getExpNeededForLevel(level));
				levelUp();
				if(getLevel() == getMaxLevel()){
					setExp(0);
					break;
				}
				
			}
			
			setExp(totalExp);
		}
	}
	
	public int getMaxLevel(){
		return isCygnus() ? 120 : 200;
	}
	
	public MapleQuestInstance getQuest(int id){
		if(!quests.containsKey(id)){
			return new MapleQuestInstance(MapleQuest.getQuest(id), 0, 0, MapleQuestStatus.NOT_STARTED);
		}
		return quests.get(id);
	}
	
	public void levelUp(){
		
		Skill improveHpGain = null;
		Skill improveMpGain = null;
		int hpGainLevel = 0;
		int mpGainLevel = 0;
		
		setLevel(level+1);
		
		if(isCygnus() && level < 70){
			setRemainingAp(remainingAp + 6);
		}else{
			setRemainingAp(remainingAp + 5);
		}
		
		if(getJob().isBeginnerJob() && level < 11){
			remainingAp = 0;
			if(getLevel() < 6){
				setStr(str + 6);
			}else{
				str += 4;
				dex += 1;
			}
		}else{
			if(isCygnus() && level < 70){
				setRemainingAp(remainingAp + 6);
			}else{
				setRemainingAp(remainingAp + 5);
			}
		}
		
        if (job == MapleJob.BEGINNER || job == MapleJob.NOBLESSE || job == MapleJob.LEGEND) {
            maxHp += Randomizer.rand(12, 16);
            maxMp += Randomizer.rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1)) {
            improveHpGain = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_INCREASE) : SkillFactory.getSkill(Swordsman.IMPROVED_MAX_HP_INCREASE);
            if (job.isA(MapleJob.CRUSADER)) {
                improveMpGain = SkillFactory.getSkill(1210000);
            } else if (job.isA(MapleJob.DAWNWARRIOR2)) {
                improveMpGain = SkillFactory.getSkill(11110000);
            }
            hpGainLevel = getSkillLevel(improveHpGain);
            maxHp += Randomizer.rand(24, 28);
            maxMp += Randomizer.rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            improveMpGain = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
            mpGainLevel = getSkillLevel(improveMpGain);
            maxHp += Randomizer.rand(10, 14);
            maxMp += Randomizer.rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || (job.getId() > 1299 && job.getId() < 1500)) {
            maxHp += Randomizer.rand(20, 24);
            maxMp += Randomizer.rand(14, 16);
        } else if (job.isA(MapleJob.GM)) {
            maxHp = 30000;
            maxMp = 30000;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            improveHpGain = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAX_HP) : SkillFactory.getSkill(5100000);
            hpGainLevel = getSkillLevel(improveHpGain);
            maxHp += Randomizer.rand(22, 28);
            maxMp += Randomizer.rand(18, 23);
        } else if (job.isA(MapleJob.ARAN1)) {
            maxHp += Randomizer.rand(44, 48);
            int aids = Randomizer.rand(4, 8);
            maxMp += aids + Math.floor(aids * 0.1);
        }
        
        if (hpGainLevel > 0 && (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.PIRATE) || job.isA(MapleJob.DAWNWARRIOR1))) {
            maxHp += improveHpGain.getEffect(hpGainLevel).getX();
        }
        if (mpGainLevel > 0 && (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.CRUSADER) || job.isA(MapleJob.BLAZEWIZARD1))) {
            maxMp += improveMpGain.getEffect(mpGainLevel).getX();
        }
        
        maxMp += int_ / 10;
        
        maxHp = Math.min(30000, maxHp);
        maxMp = Math.min(30000, maxMp);
        
        hp = maxHp;
        mp = maxMp;
        
        List<Pair<MapleStat, Integer>> statup = new ArrayList<>(10);
        statup.add(new Pair<>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<>(MapleStat.HP, maxHp));
        statup.add(new Pair<>(MapleStat.MP, maxMp));
        statup.add(new Pair<>(MapleStat.EXP, exp));
        statup.add(new Pair<>(MapleStat.LEVEL, level));
        statup.add(new Pair<>(MapleStat.MAXHP, maxHp));
        statup.add(new Pair<>(MapleStat.MAXMP, maxMp));
        
        if (job.getId() % 1000 > 0) {
            remainingSp += 3;
            statup.add(new Pair<>(MapleStat.AVAILABLESP, remainingSp));
        }
        
        client.sendPacket(PacketFactory.updatePlayerStats(statup, false));
		
        client.sendPacket(PacketFactory.showOwnBuffEffect(0, 0));
        getMap().broadcastPacket(PacketFactory.getShowForeignEffect(getId(), 0), getId());
        
	}

	public void giveMesos(int mesos) {
		giveMesos(mesos, true, true);
	}
	
	public void giveMesos(int mesos, boolean showInSideBar, boolean showInChat) {
		
		long m = (long) meso + (long) mesos;
		
		if(m > Integer.MAX_VALUE){
			setMeso(Integer.MAX_VALUE);
		}else{
			setMeso(meso + mesos);
		}
		
		
		if(showInSideBar || showInChat){
			if(mesos < 0){
				client.sendPacket(PacketFactory.getShowMesoGain(mesos, showInChat));
			}else{
				client.sendPacket(PacketFactory.getShowMesoGain(mesos));
			}
		}
	}

	public int addStat(MapleStat stat, int amount) {
		int r = 0;
		if(stat == MapleStat.STR){
			if(str + amount > 999){
				r = str + amount - 999;
				setStr(999);
			}else{
				setStr(str + amount);
			}
		}else if(stat == MapleStat.DEX){
			if(dex + amount > 999){
				r = dex + amount - 999;
				setDex(999);
			}else{
				setDex(dex + amount);
			}
		}else if(stat == MapleStat.LUK){
			if(luk + amount > 999){
				r = luk + amount - 999;
				setLuk(999);
			}else{
				setLuk(luk + amount);
			}
		}else if(stat == MapleStat.INT){
			if(int_ + amount > 999){
				r = int_ + amount - 999;
				setInt(999);
			}else{
				setInt(int_ + amount);
			}
		}else{
			getWorld().getLogger().debug("Can't add stat "+stat.name());
		}
		return r;
	}
	
	public void changeSkillLevel(Skill skill, int level, int masterLevel) {

		if(skill == null){
			throw new NullPointerException("skill cannot be null");
		}
		
		if(level < 0){
			throw new IllegalArgumentException("level cannot be less than 0");
		}
		
		/*if(masterLevel < 0){
			throw new IllegalArgumentException("masterLevel cannot be less than 0");
		}*/
		
		if(level > skill.getMaxLevel()){
			throw new IllegalArgumentException("level cannot be greater than "+skill.getMaxLevel()+" (SKILL "+SkillFactory.getSkillName(skill.getId()));
		}
		
		SkillData data = new SkillData(level, masterLevel);
		
		skills.put(skill.getId(), data);
		
		client.sendPacket(PacketFactory.getUpdateSkillPacket(skill.getId(), level, masterLevel, 0));
		
		boolean isNew = false;
		try {
			List<QueryResult> results = MapleDatabase.getInstance().query("SELECT COUNT(*) FROM `skills` WHERE `owner`=? AND `skillid`=?", getId(), skill.getId());
			
			if(results.get(0).getCountResult() == 0){
				isNew = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(isNew){
			try {
				MapleDatabase.getInstance().execute("INSERT INTO `skills` (`owner`, `skillid`, `level`) VALUES (?, ?, ?)", getId(), skill.getId(), level);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			try {
				MapleDatabase.getInstance().execute("UPDATE `skills` SET `level`=? WHERE `owner`=? AND `skillid`=?", level, getId(), skill.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		
	}

	public boolean isCygnus() {
		return job.isA(MapleJob.NOBLESSE);
	}

	public int getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return 0;
        }
        return mbsvh.value;
	}
	
	public void cancelBuffStats(MapleBuffStat... stats) {
		List<MapleBuffStat> buffs = Arrays.asList(stats);
		
		deregisterBuffStats(buffs);
		cancelPlayerBuffs(buffs);
	}

	public void changeMap(int mapId) {
		changeMap(client.getChannel().getMapFactory().getMap(mapId));
	}

	public int getItemQuantity(int itemid, boolean checkEquipped) {
		int possesed = getInventory(itemid).countById(itemid);
		if (checkEquipped) {
			possesed += getInventory(InventoryType.EQUIPPED).countById(itemid);
		}
		return possesed;
	}

	public void updateCharacterLook() {
		getMap().broadcastPacket(PacketFactory.getUpdatePlayerLookPacket(this));
	}

	public void addSummon(int sourceid, MapleSummon tosummon) {
		summons.put(sourceid, tosummon);
	}
	
	public void openShop(MapleShop shop, int npc){
		client.sendPacket(PacketFactory.openShop(getClient(), shop, npc));
		openShop = shop;
	}
	
	public void closeShop(){
		openShop = null;
	}
	
	public void showInfoText(String text){
		client.sendPacket(PacketFactory.showInfoText(text));
	}

	public void registerEffect(MapleStatEffect effect, long starttime, MapleTask schedule) {
		if (effect.isDragonBlood()) {
            //prepareDragonBlood(effect);
        } else if (effect.isBerserk()) {
            //checkBerserk();
        } else if (effect.isBeholder()) {
            /*final int beholder = DarkKnight.BEHOLDER;
            if (beholderHealingSchedule != null) {
                beholderHealingSchedule.cancel(false);
            }
            if (beholderBuffSchedule != null) {
                beholderBuffSchedule.cancel(false);
            }
            Skill bHealing = SkillFactory.getSkill(DarkKnight.AURA_OF_BEHOLDER);
            int bHealingLvl = getSkillLevel(bHealing);
            if (bHealingLvl > 0) {
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                int healInterval = healEffect.getX() * 1000;
                beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        addHP(healEffect.getHp());
                        client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, 5), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(beholder, 2), false);
                    }
                }, healInterval, healInterval);
            }
            Skill bBuff = SkillFactory.getSkill(DarkKnight.HEX_OF_BEHOLDER);
            if (getSkillLevel(bBuff) > 0) {
                final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
                int buffInterval = buffEffect.getX() * 1000;
                beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        buffEffect.applyTo(MapleCharacter.this);
                        client.announce(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, (int) (Math.random() * 3) + 6), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), beholder, 2), false);
                    }
                }, buffInterval, buffInterval);
            }*/
        } else if (effect.isRecovery()) {
            /*final byte heal = (byte) effect.getX();
            recoveryTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    addHP(heal);
                    client.announce(MaplePacketCreator.showOwnRecovery(heal));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showRecovery(id, heal), false);
                }
            }, 5000, 5000);*/
        }
        for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
            effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, statup.getRight().intValue(), schedule));
        }
       
	}
	
	public boolean hasBuff(MapleStatEffect effect){
		for(MapleBuffStatValueHolder holder : effects.values()){
			if(holder.effect.equals(effect)){
				return true;
			}
		}
		return false;
	}
	
    private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            if (stateffect.getValue().effect.sameSource(effect) && (startTime == -1 || startTime == stateffect.getValue().startTime)) {
                stats.add(stateffect.getKey());
            }
        }
        return stats;
    }
	
    private void deregisterBuffStats(List<MapleBuffStat> stats) {
        synchronized (stats) {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<>(stats.size());
            for (MapleBuffStat stat : stats) {
                MapleBuffStatValueHolder mbsvh = effects.get(stat);
                if (mbsvh != null) {
                    effects.remove(stat);
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel) {
                        if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                            addMbsvh = false;
                        }
                    }
                    if (addMbsvh) {
                        effectsToCancel.add(mbsvh);
                    }
                    if (stat == MapleBuffStat.RECOVERY) {
                        /*if (recoveryTask != null) {
                            recoveryTask.cancel(false);
                            recoveryTask = null;
                        }*/
                    } else if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();
                        MapleSummon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastPacket(PacketFactory.getDestroySummonPacket(summon, true));
                            getMap().removeObject(summon.getObjectId());
                            
                            //removeVisibleMapObject(summon);
                            summons.remove(summonId);
                        }
                        if (summon.getSkill() == DarkKnight.BEHOLDER) {
                            /*if (beholderHealingSchedule != null) {
                                beholderHealingSchedule.cancel(false);
                                beholderHealingSchedule = null;
                            }
                            if (beholderBuffSchedule != null) {
                                beholderBuffSchedule.cancel(false);
                                beholderBuffSchedule = null;
                            }*/
                        }
                    } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                        /*dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;*/
                    }
                }
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
                if (cancelEffectCancelTasks.schedule != null) {
                    cancelEffectCancelTasks.schedule.cancel(false);
                    this.cancelEffect(cancelEffectCancelTasks.effect, false, -1);
                }
            }
        }
    }
    
    public void destroyMagicDoors(){
    	
    	if(magicDoors.isEmpty()){
    		return;
    	}
    	
    	List<MapleMagicDoor> doors = new ArrayList<>(magicDoors);
    	
    	getWorld().getLogger().debug("Destroying "+doors.size()+" doors");
    	
    	for(MapleMagicDoor door : doors){
    		door.destroy();
    	}
    	
    }
    
	public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime){
		List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
        	
        	destroyMagicDoors();
        	
            /*if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                if (party != null) {
                    for (MaplePartyCharacter partyMembers : getParty().getMembers()) {
                        partyMembers.getPlayer().getDoors().remove(door);
                        partyMembers.getDoors().remove(door);
                    }
                    silentPartyUpdate();
                } else {
                    clearDoors();
                }
            }*/
        }
        if (effect.getSourceId() == Spearman.HYPER_BODY || effect.getSourceId() == GM.HYPER_BODY || effect.getSourceId() == SuperGM.HYPER_BODY) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(4);
            statup.add(new Pair<>(MapleStat.HP, Math.min(hp, maxHp)));
            statup.add(new Pair<>(MapleStat.MP, Math.min(mp, maxMp)));
            statup.add(new Pair<>(MapleStat.MAXHP, maxHp));
            statup.add(new Pair<>(MapleStat.MAXMP, maxMp));
            client.sendPacket(PacketFactory.updatePlayerStats(statup, this));
        }
        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != Corsair.BATTLE_SHIP) {
            	if(getMount() != null){
                    getMount().cancelSchedule();
                    getMount().setActive(false);	
            	}
            }
        }
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
        }
	}
	
	public void sendHint(String hint, int width, int height){
		if(hint.equals(lastHint)){
			if(System.currentTimeMillis() - lastHintTime < 20000){
				return;
			}
		}
		lastHint = hint;
		lastHintTime = System.currentTimeMillis();
		client.sendPacket(PacketFactory.sendHint(hint, width, height));
	}
	
    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        /*if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.sendPacket(PacketFactory.cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                getMap().broadcastPacket(PacketFactory.cancelForeignBuff(getId(), buffstats), getId());
            }
        }*/
    	client.sendPacket(PacketFactory.cancelBuff(buffstats));
        if (buffstats.size() > 0) {
            getMap().broadcastPacket(PacketFactory.cancelForeignBuff(getId(), buffstats), getId());
        }
    }
	
	@AllArgsConstructor
	@Data
	private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public MapleTask schedule;
        
	}

	public void mount(int ridingLevel, int sourceid) {
		mount = new MapleMount(this, ridingLevel, sourceid);
	}

	public void resetBattleshipHp() {
		battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP)) + ((getLevel() - 120) * 2000);
	}

	public void emote(MapleEmote emote) {
		getMap().broadcastPacket(PacketFactory.facialExpression(this, emote), getId());
	}

	public void dispelSkill(int skillId) {
		LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillId == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004 || isDispellable(mbsvh.effect.getSourceId()))) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillId) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
	}
	
	private boolean isDispellable(int skillid) {
        switch (skillid) {
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Priest.SUMMON_DRAGON:
            case Bishop.BAHAMUT:
            case Ranger.PUPPET:
            case Ranger.SILVER_HAWK:
            case Sniper.PUPPET:
            case Sniper.GOLDEN_EAGLE:
            case Hermit.SHADOW_PARTNER:
                return true;
            default:
                return false;
        }
    }

	public int getFh() {
		Point pos = this.getPosition();
		pos.y -= 6;
		if (getMap().getFootholds().findBelow(pos) == null) {
			return 0;
		} else {
			return getMap().getFootholds().findBelow(pos).getY1();
		}
	}
	
	public MapleStatEffect getBuffEffect(MapleBuffStat stat){
		MapleBuffStatValueHolder holder = effects.get(stat);
		
		return holder == null ? null : holder.effect;
	}

	public void checkMonsterAggro(MapleMonster monster) {
		if(!monster.hasAggro()){
			if(monster.getController() == this){
				monster.setAggro(true);
			}else{
				controlMonster(monster);
			}
		}
	}

	public void cleanup() {
		destroyMagicDoors();
		getControlledMonsters().forEach(monster -> uncontrolMonster(monster));
		synchronized (cooldowns) {
			cooldowns.values().forEach(sf -> sf.cancel());	
		}
		for(MapleDisease disease : MapleDisease.values()){
			dispelDebuff(disease);
		}
	}

	public MaplePortal getInitialSpawnpoint() {
		for(MaplePortal portal : getMap().getPortals()){
			if(portal.getId() == lastPortalId){
				return portal;
			}
		}
		
		return getMap().getFallbackPortal();
	}
	
	public int getInitialSpawnpointId(){
		return lastPortalId;
	}
	
	public boolean isSkillCoolingDown(Skill skill) {
		return isSkillCoolingDown(skill.getId());
	}

	public boolean isSkillCoolingDown(int skillId){
		
		synchronized (cooldowns) {
			return cooldowns.containsKey(skillId);
		}
		
	}

	public void cancelEffect(int skill) {
		cancelEffect(SkillFactory.getSkill(skill).getEffect(getSkillLevel(skill)), false, -1);
	}

	public int giveFameTo(MapleCharacter target, int amount) {
		
		long fameDeltaTime = System.currentTimeMillis() - lastFame;
		
		long days = TimeUnit.DAYS.convert(fameDeltaTime, TimeUnit.MILLISECONDS);
		
		if(days >= 1){
			
			long lastFameToThisPerson = System.currentTimeMillis() -  fameThisMonth.getOrDefault(target.getId(), 0L);
			
			long daysForThisPerson = TimeUnit.DAYS.convert(lastFameToThisPerson, TimeUnit.MILLISECONDS);
			
			if(daysForThisPerson < 30){
				return -1;
			}
			
		}else{
			return -2;
		}
		
		if(target.getFame() + amount > 30000 || target.getFame() + amount < -30000){
			return -3;
		}
		
		lastFame = System.currentTimeMillis();
		fameThisMonth.put(target.getId(), System.currentTimeMillis());
		
		target.gainFame(amount);
		
		return 0;
	}
	
	public void reviveAtClosestTown(){
		if(isAlive()){
			throw new IllegalStateException("Can't revive if already alive!");
		}
		
		MapleMap map = client.getCharacter().getMap();
		
		client.getCharacter().changeMap(map.getReturnMap(), map.getReturnMap().getFallbackPortal());
		
		client.getCharacter().setHp(50);
	}
	
	public void revive(){
		if(isAlive()){
			throw new IllegalStateException("Can't revive if already alive!");
		}

		playSkillEffect(Bishop.RESURRECTION);
		setHp(getMaxHp());
		setMeso((int) (meso * 0.9));
		setExp((int) (exp * 0.9));
		
	}
	
	public void playSkillEffect(int skillId){
		client.sendPacket(PacketFactory.showOwnBuffEffect(skillId, 1));
	}
	
	public void gainFame(int amount){
		fame += amount;
		fame = Math.min(30000, fame);
		fame = Math.max(-30000, fame);
		updateStat(MapleStat.FAME, fame);
	}

	public void clearCooldown(int skillId){
		synchronized (cooldowns) {
			if(cooldowns.containsKey(skillId)){
				
				cooldowns.get(skillId).cancel();
				
				cooldowns.remove(skillId);
				client.sendPacket(PacketFactory.skillCooldown(skillId, 0));
				
			}	
		}
	}
	
	public void clearAllCooldowns() {
		synchronized (cooldowns) {
			for(int i : cooldowns.keySet()){
				clearCooldown(i);
			}	
		}
	}

	public Set<Integer> getAllCooldowns() {
		synchronized (cooldowns) {
			return cooldowns.keySet();
		}
	}

	public long getCooldownTimeLeft(int skillId) {
		synchronized (cooldowns) {
			return cooldowns.get(skillId).getTimeLeft();	
		}
	}

	public void pauseCooldowns() {
		for(int skillId : getAllCooldowns()){
			synchronized (cooldowns) {
				CooldownValueHolder holder = cooldowns.get(skillId);
				
				holder.pauseTask();
			}
		}
	}
	
	public void timeleap(){
		for (int i : getAllCooldowns()) {
        	if(i != Buccaneer.TIME_LEAP)
        		clearCooldown(i);
        }
	}

	public void startCooldownTimers() {
		for(int skillId : getAllCooldowns()){
			synchronized (cooldowns) {
				CooldownValueHolder holder = cooldowns.get(skillId);
				
				holder.startTask(this, skillId);
			}
		}
	}

	public void dispelAllSkills() {
		for(MapleBuffStatValueHolder holder : new ArrayList<>(effects.values())){
			if(holder.effect.isSkill()){
				cancelEffect(holder.effect, false, holder.startTime);
			}
		}
	}

	public void banish(int mapId, String portal, String msg) {
		sendMessage(MessageType.PINK_TEXT, msg);
		
		MapleMap map = client.getChannel().getMapFactory().getMap(mapId);
		
		changeMap(map, map.getPortal(portal));
	}

	public boolean hasDisease(MapleDisease disease){
		return diseases.containsKey(disease);
	}
	
	public void dispelDebuff(MapleDisease disease){
		
		if(hasDisease(disease)){
			
			client.sendPacket(PacketFactory.cancelDebuff(disease));
			getMap().broadcastPacket(PacketFactory.cancelForeignDebuff(id, disease), id);
			
			diseases.remove(disease).cancel(false);
		}
		
	}
	
	public void dispelDebuffs(){
		for(MapleDisease dis : diseases.keySet()){
			dispelDebuff(dis);
		}
	}
	
	public void giveDebuff(MapleDisease disease, MobSkill skill) {
		
		if(!hasDisease(disease) && diseases.size() < 2){
			if(!(disease == MapleDisease.SEDUCE) || disease == MapleDisease.STUN){
				if(getBuffedValue(MapleBuffStat.HOLY_SHIELD) != 0){
					return;
				}
			}
		}
		
		MapleTask cancelTask = TimerManager.schedule(new Runnable() {
			
			@Override
			public void run() {
				dispelDebuff(disease);
			}
		}, skill.getDuration());
		
		diseases.put(disease, cancelTask);
		
		client.sendPacket(PacketFactory.giveDebuff(disease, skill));
		getMap().broadcastPacket(PacketFactory.giveForeignDebuff(id, disease, skill), id);
	}

	public void toggleGodMode() {
		godModeEnabled = !godModeEnabled;
		if(godModeEnabled){
			Skill skill = SkillFactory.getSkill(Paladin.STANCE);
			MapleStatEffect effect = skill.getEffect(skill.getMaxLevel());
			effect.setDuration(Integer.MAX_VALUE);
			effect.setMpCon((short) 0);
			effect.applyTo(this);
			playSkillEffect(Paladin.STANCE);
			sendMessage(MessageType.LIGHT_BLUE_TEXT, "God mode enabled!");
		}else{
			dispelSkill(Paladin.STANCE);
			sendMessage(MessageType.LIGHT_BLUE_TEXT, "God mode disabled!");
		}
	}

	public void showInfo(PopupInfo info) {
		getClient().sendPacket(PacketFactory.showInfo(info.getPath()));
	}

	public void updateQuest(MapleQuestInstance quest) {
		quests.put(quest.getQuest().getId(), quest);
		MapleQuestStatus qs = quest.getStatus();
		
		if(qs == MapleQuestStatus.NOT_STARTED || qs == MapleQuestStatus.STARTED){
			client.sendPacket(PacketFactory.updateQuest(quest, false));
			if(quest.getQuest().getQuestInfo().getInfoNumber() > 0){
				client.sendPacket(PacketFactory.updateQuest(quest, true));
			}
		}
		
		if(qs == MapleQuestStatus.STARTED){
			client.sendPacket(PacketFactory.updateQuestInfo(quest));
		}else if(qs == MapleQuestStatus.COMPLETED){
			client.sendPacket(PacketFactory.completeQuest(quest));
		}
	}

	public Collection<MapleQuestInstance> getQuests(MapleQuestStatus status) {
		return Collections.unmodifiableList(quests.values().stream().filter(q -> q.getStatus() == status).collect(Collectors.toList()));
	}
	
	
	public void disposeOpenNpc(){
		activeNpc = null;
		activeNpcConversation = null;
	}
	
	public MapleScriptInstance openNpc(int id){
		return openNpc(MapleLifeFactory.getNPC(id));
	}
	
	public MapleScriptInstance openNpc(MapleNPC npc) {
		MapleScript script = new MapleScript("scripts/npc/"+npc.getId()+".js", "scripts/npc/fallback.js");
		
		return openNpc(script, npc);
	}

	public MapleScriptInstance openQuestNpc(MapleScript script, int quest, int npc, boolean end){
		MapleScriptInstance instance = null;
		
		try{
			QuestScriptManager qm = new QuestScriptManager(this, quest, npc);
			
			SimpleBindings sb = new SimpleBindings();
			sb.put("qm", qm);
			
			instance = script.execute(sb);
			instance.setQuestEnd(end);
			
			setActiveNpc(instance);
			setActiveNpcConversation(qm);
			
			instance.questAction(1, 0, -1);
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		}
		
		return instance;
	}
	
	public void openQuestNpc(MapleScriptInstance script, int quest, int npc, boolean end){
		try{
			QuestScriptManager qm = new QuestScriptManager(this, quest, npc);
			
			script.setVariable("qm", qm);
			
			script.setQuestEnd(end);
			
			setActiveNpc(script);
			setActiveNpcConversation(qm);
			
			script.questAction(1, 0, -1);
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		}
	}
	
	public MapleScriptInstance openNpc(MapleScript script, MapleNPC npc) {
		return openNpc(script, new SimpleBindings(), npc);
	}
	
	public MapleScriptInstance openNpc(MapleScript script, int npc) {
		return openNpc(script, new SimpleBindings(), MapleLifeFactory.getNPC(npc));
	}
	
	public MapleScriptInstance openNpc(MapleScript script, Bindings sb, MapleNPC npc) {
		MapleScriptInstance instance = null;
		
		try{
			NpcConversationManager cm = new NpcConversationManager(this, npc);
			
			sb.put("cm", cm);
			
			instance = script.execute(sb);

			setActiveNpc(instance);
			setActiveNpcConversation(cm);
			
			instance.startNpc();
			
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		}
		
		return instance;
	}

	public int getMaxSlotForItem(Item item) {
		return getMaxSlotForItem(item.getItemId());
	}
	
	public int getMaxSlotForItem(int item) {
		int max = ItemInfoProvider.getSlotMax(item);
		if(getJob().isA(MapleJob.ASSASSIN))
			max += getSkillLevel(Assassin.CLAW_MASTERY) * 10;
		if(getJob().isA(MapleJob.GUNSLINGER))
			max += getSkillLevel(Gunslinger.GUN_MASTERY) * 10;
		
		return max;
	}

	public boolean hasInventorySpace(List<Item> items) {
		if(items.size() == 0){//I'm adding this for future cases
			return true;	  //As of now, there is no reason for this I think
		}
		int eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
		for(Item item : items){
			InventoryType type = InventoryType.getByItemId(item.getItemId());
			
			switch(type){
			case EQUIP:
				eq++;
				break;
			case USE:
				use++;
				break;
			case SETUP:
				setup++;
				break;
			case ETC:
				etc++;
				break;
			case CASH:
				cash++;
				break;
				default:
					break;
			}
		}
		
		if(eq > 0 && getInventory(InventoryType.EQUIP).getFreeSlot(eq) == -1){
			return false;
		} else if(use > 0 && getInventory(InventoryType.USE).getFreeSlot(use) == -1){
			return false;
		}else if(setup > 0 && getInventory(InventoryType.SETUP).getFreeSlot(setup) == -1){
			//I don't think anyone in the history of EVER has ever had their setup inventory full
			return false;
		}else if(etc > 0 && getInventory(InventoryType.ETC).getFreeSlot(etc) == -1){
			return false;
		}else if(cash > 0 && getInventory(InventoryType.CASH).getFreeSlot(cash) == -1){
			return false;
		}
			
		
		return true;
	}

	public MapleTrade createTrade() {
		if(openTrade != null){
			throw new IllegalStateException("Trade already open");
		}
		return (openTrade = new MapleTrade());
	}

	public boolean isPetSpawned(int itemId) {
		return getPetSlot(itemId) != -1;
	}

	public boolean spawnPet(PetItem item) {
		if(isPetSpawned(item.getItemId())){
			return false;
		}
		int slot = getNextPetSlot();
		if(slot == -1){
			return false;
		}
		pets[slot] = new MaplePetInstance(this, item);
		
		pets[slot].setFoothold(getMap().getFoothold(getPosition()));
		pets[slot].setPosition(getPosition());
		
		getMap().broadcastPacket(PacketFactory.spawnPet(this, pets[slot], slot));
		
		client.sendPacket(PacketFactory.petStatUpdate(this));
		
		item.setSummoned(true);
		
		return true;
	}

	private int getNextPetSlot(){
		for(int i = 0; i < pets.length;i++){
			if(pets[i] == null){
				return i;
			}
		}
		return -1;
	}

	public int getPetSlot(int petItemId){
		for(int i = 0; i < pets.length;i++){
			if(pets[i] != null){
				if(pets[i].getSource().getItemId() == petItemId){
					return i;
				}
			}
		}
		return -1;
	}
	
	public boolean despawnPet(PetItem item) {
		if(!isPetSpawned(item.getItemId())){
			return false;
		}
		int slot = getPetSlot(item.getItemId());
		getMap().broadcastPacket(PacketFactory.destroyPet(this, pets[slot], slot, false));
		item.setSummoned(false);
		pets[slot] = null;
		client.sendPacket(PacketFactory.petStatUpdate(this));
		
		return true;
	}

	public MaplePetInstance getPetByUniqueId(int petSlot) {
		
		for(MaplePetInstance inst : getPets()){
			if(inst != null){
				if(inst.getSource().getUniqueId() == petSlot){
					return inst;
				}
			}
		}
		
		return null;
	}
	
	
}
