package maplestory.script.legacy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import constants.LoginStatus;
import constants.MessageType;
import maplestory.cashshop.CashShopWallet.CashShopCurrency;
import maplestory.client.MapleClient;
import maplestory.guild.MapleGuild;
import maplestory.guild.MapleGuildRankLevel;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.inventory.item.PetItem;
import maplestory.life.MapleLifeFactory;
import maplestory.life.MapleMonster;
import maplestory.life.MapleNPC;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapObjectType;
import maplestory.map.MaplePortal;
import maplestory.party.MapleParty;
import maplestory.party.MapleParty.PartyEntry;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.player.MaplePetInstance;
import maplestory.quest.MapleQuest;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.script.NpcConversationManager;
import maplestory.server.net.PacketFactory;
import maplestory.shop.MapleShop;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The purpose of this class is to provide backwards compatibility to odin-ms
 * based scripts We will try to mirror all the methods found in odin's Npc
 * Conversation Manager, (I am using SolaxiaV2 as a base)
 * 
 * @author Tyler
 *
 */
public class OdinNpcConversationManager extends NpcConversationManager {

	private String scriptName;

	public OdinNpcConversationManager(MapleCharacter chr, MapleNPC npc, String scriptName) {
		super(chr, npc);
		this.scriptName = scriptName;
	}

	public int getNpcId() {
		return super.getNpc().getId();
	}

	public int getNpcObjectId() {
		return super.getNpc().getObjectId();
	}

	public String getScriptName() {
		return scriptName;
	}

	public String getText() {
		return getInputText();
	}

	public int getJobId() {
		return getCharacter().getJob().getId();
	}

	public MapleJob getJob() {
		return getCharacter().getJob();
	}

	public void startQuest(short id) {
		
		if(!MapleQuest.exists(id)) {
			return;
		}
		
		startQuest(id, getNpcId());
	}

	public void completeQuest(short id) {
		
		if(!MapleQuest.exists(id)) {
			return;
		}
		
		completeQuest(id);
	}

	public boolean forceStartQuest(short id) {
		startQuest(id);
		return true;
	}

	public boolean forceCompleteQuest(short id) {
		completeQuest(id);
		return true;
	}

	public int getMeso() {
		return getCharacter().getMeso();
	}

	public void gainMeso(int meso) {
		giveMesos(meso);
	}

	public void gainExp(int exp) {
		giveExp(exp);
	}

	public int getLevel() {
		return getCharacter().getLevel();
	}

	// The super.showEffect does something else so we don't use it
	public void showEffect(String effect) {
		getCharacter().getMap().broadcastPacket(PacketFactory.showEffect(effect));
	}

	public void setHair(int hair) {
		getCharacter().setHair(hair);
	}

	public void setFace(int face) {
		getCharacter().setFace(face);
	}

	public void setSkin(int skin) {
		getCharacter().setSkinColor(skin);
	}

	public int itemQuantity(int itemId) {
		return getCharacter().getItemQuantity(itemId, false);
	}

	public void displayGuildRanks() {
		getCharacter().sendMessage(MessageType.POPUP, "Guild ranks not implemented yet!!!");
	}

	// TODO: Make a wrapper for this too!
	public MapleParty getParty() {
		return getCharacter().getParty();
	}

	public void resetMap() {
		getCharacter().getMap().resetReactors();
	}

	public void gainCloseness(int closeness) {
		for (MaplePetInstance pet : getCharacter().getPets()) {
			if (pet != null) {
				pet.getSource().setCloseness(pet.getSource().getCloseness() + closeness);
			}
		}
	}

	public String getName() {
		return getCharacter().getName();
	}

	public int getGender() {
		return getCharacter().getGender();
	}

	public void changeJobById(int job) {
		MapleJob j = MapleJob.getById(job);
		if (j == null) {
			getCharacter().sendMessage(MessageType.POPUP, "Tried to set null job " + job);
			return;
		}
		getCharacter().changeJob(j);
	}

	public void changeJob(MapleJob job) {
		getCharacter().changeJob(job);
	}

	public MapleJob getJobName(int id) {
		return MapleJob.getById(id);
	}

	public MapleStatEffect getItemEffect(int itemId) {
		return ItemInfoProvider.getItemEffect(itemId);
	}

	public void resetStats() {
		getCharacter().resetStats();
	}

	public void openShopNpc(int id) {
		MapleShop shop = MapleShop.getShop(id);

		if (shop == null) {
			getCharacter().sendMessage(MessageType.POPUP, "Unknown shop id " + id);
			return;
		}

		getCharacter().openShop(shop, getNpcId());
	}

	public void maxMastery() {
		for (Skill skill : SkillFactory.getAllSkills()) {
			getCharacter().changeSkillLevel(skill, skill.getMaxLevel(), 0);
		}
	}

	public void doGachapon() {
		getCharacter().sendMessage(MessageType.POPUP, "Gacha not implemented");
	}

	public void upgradeAlliance() {
		getCharacter().sendMessage(MessageType.POPUP, "Alliances not implemented");
	}

	public void disbandAlliance(MapleClient client, int allianceId) {
		getCharacter().sendMessage(MessageType.POPUP, "Alliances not implemented");
	}

	public boolean canBeUsedAllianceName(String name) {
		return false;
	}

	public Object createAlliance(String name) {
		return new Object();
	}

	public int getAllianceCapacity() {
		return 10;
	}

	public boolean hasMerchant() {
		return getClient().getWorld().getMerchantByOwner(getCharacter().getId()) != null;
	}

	public boolean hasMerchantItems() {
		return false;// We don't save merchant items :O
	}

	public void showFredrick() {
		getCharacter().sendMessage(MessageType.POPUP, "Fredrick not implemented");
	}

	public int partyMembersInMap() {
		int inMap = 0;
		for (MapleCharacter char2 : getCharacter().getMap().getPlayers()) {
			if (char2.getParty() == getParty()) {
				inMap++;
			}
		}
		return inMap;
	}

	public Object getEvent() {
		return null;
	}

	public void divideTeams() {
		getCharacter().sendMessage(MessageType.POPUP, "Events not implemented");
	}

	public MapleCharacter getMapleCharacter(String name) {
		return getCharacter().getClient().getChannel().getPlayerByName(name);
	}

	public void logLeaf(String prize) {
		// Don't log lol
	}

	public boolean createPyramid(String mode, boolean party) {
		return false;
	}

	public Object[] getAvailableMasteryBooks() {
		return new Object[0];
	}

	public Object[] getAvailableSkillBooks() {
		return new Object[0];
	}

	public Object[] getNamesWhoDropsItem(int itemId) {
		return ItemInfoProvider.getMonstersWhoDrop(itemId).stream()
				.map(id -> MapleLifeFactory.getMonster(id).getStats().getName()).collect(Collectors.toList()).toArray();
	}

	// FROM HERE ON ITS ALL ODIN SUPPORT STUFF
	// YOU CAN USE THESE METHODS BUT THEY ARE SOMETIMES STUPID

	public void warpMap(int map) {
		getCharacter().getMap().getPlayers().forEach(pl -> pl.changeMap(map));
	}

	public void warpParty(int id, int portalId) {
		int mapid = getMap().getMapId();
		warpParty(id, 0, mapid, mapid);
	}

	public void warpParty(int id, int min, int max) {
		warpParty(id, 0, min, max);
	}

	public void warpParty(int id, int pid, int min, int max) {
		MapleMap map = getClient().getChannel().getMap(id);
		MaplePortal portal = map.getPortal(pid);

		for (MapleCharacter mc : getPartyMembers()) {
			if (mc.getMapId() >= min && mc.getMapId() <= max) {
				mc.changeMap(map, portal);
			}
		}
	}

	public List<MapleCharacter> getPartyMembers() {
		if (getCharacter().getParty() == null) {
			return Collections.emptyList();
		}
		List<MapleCharacter> players = new ArrayList<>();

		for (PartyEntry entry : getCharacter().getParty().getMembers()) {
			if (entry.getSnapshot().isOnline()) {
				players.add(entry.getSnapshot().getLiveCharacter().get());
			}
		}

		return players;
	}

	public MapleMap getWarpMap(int map) {
		return getClient().getChannel().getMap(map);
	}

	public MapleMap getMap(int map) {
		return getWarpMap(map);
	}

	public int countMonsters() {
		return getCharacter().getMap().getMonsters().size();
	}

	public void resetMapObjects(int mapid) {
		getMap(mapid).resetReactors();
	}

	public Object getEventManager(String event) {
		throw new NotImplementedException();
	}

	public Object getEventInstanceManager() {
		throw new NotImplementedException();
	}

	public Inventory getInventory(InventoryType type) {
		return getCharacter().getInventory(type);
	}

	public boolean haveItem(int id) {
		return hasItem(id, 1);
	}

	// I can't even imagine why they would have 2 sets of
	// has / have methods, its silly
	public boolean haveItem(int id, int amount) {
		return hasItem(id, amount);
	}

	public int getItemQuanity(int id) {
		return itemAmount(id);
	}

	public boolean haveItemWithId(int id) {
		return haveItemWithId(id, false);
	}

	public boolean haveItemWithId(int id, boolean equip) {
		return getCharacter().getItemQuantity(id, equip) > 0;
	}

	public boolean canHold(int itemId, int amount) {
		return canHold(itemId);// Ignore amount I guess, like global right?
	}

	public boolean canHoldAll(List<Integer> itemids, List<Integer> quantity, boolean isInteger) {
		int size = Math.min(itemids.size(), quantity.size());

		List<Item> addedItems = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Item it = ItemFactory.getItem(itemids.get(i), quantity.get(i));
			addedItems.add(it);
		}

		return getCharacter().hasInventorySpace(addedItems);
	}

	public void openNpc(int npcid, String script) {
		super.openNpc(script, npcid);
	}

	public void updateQuest(int questId, int data) {
		if(!MapleQuest.exists(questId)) {
			return;
		}
		getCharacter().getQuest(questId).setAnyProgress(data);
		getCharacter().updateQuest(getCharacter().getQuest(questId));
	}

	public void updateQuest(int qid, String data) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		updateQuest(qid, Integer.valueOf(data));
	}

	public void updateQuest(int qid, int pid, int data) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		getCharacter().getQuest(qid).setProgress(pid, data);
		getCharacter().updateQuest(getCharacter().getQuest(qid));
	}

	public void updateQuest(int qid, int pid, String data) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		updateQuest(qid, pid, Integer.valueOf(data));
	}

	public int getQuestStatus(int id) {
		if(!MapleQuest.exists(id)) {
			return MapleQuestStatus.NOT_STARTED.getId();
		}
		return getCharacter().getQuest(id).getStatus().getId();
	}

	public MapleQuestStatus getQuestStat(int id) {
		if(!MapleQuest.exists(id)) {
			return MapleQuestStatus.NOT_STARTED;
		}
		return getCharacter().getQuest(id).getStatus();
	}

	public boolean isQuestCompleted(int quest) {
		if(!MapleQuest.exists(quest)) {
			return false;
		}
		return getQuestStat(quest) == MapleQuestStatus.COMPLETED;
	}

	public boolean isQuestActive(int quest) {
		if(!MapleQuest.exists(quest)) {
			return false;
		}
		return isQuestStarted(quest);
	}

	public void setQuestProgress(int qid, int progress) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		getCharacter().getQuest(qid).setAnyProgress(progress);
		getCharacter().updateQuest(getCharacter().getQuest(qid));
	}

	public void setQuestProgress(int qid, int pid, int progress) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		getCharacter().getQuest(qid).setProgress(pid, progress);
		getCharacter().updateQuest(getCharacter().getQuest(qid));
	}

	public void setStringQuestProgress(int qid, String progress) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		getCharacter().getQuest(qid).setAnyProgress(Integer.valueOf(progress));
		getCharacter().updateQuest(getCharacter().getQuest(qid));
	}

	public void setStringQuestProgress(int qid, int pid, String progress) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		getCharacter().getQuest(qid).setProgress(pid, Integer.valueOf(progress));
		getCharacter().updateQuest(getCharacter().getQuest(qid));
	}

	public int getQuestProgress(int qid) {
		if(!MapleQuest.exists(qid)) {
			return 0;
		}
		int progress = getCharacter().getQuest(qid).getAnyProgress();

		return progress;
	}

	public int getQuestProgress(int qid, int pid) {
		if(!MapleQuest.exists(qid)) {
			return 0;
		}
		int progress = getCharacter().getQuest(qid).getProgress(pid);

		return progress;
	}

	public String getStringQuestProgress(int qid) {
		if(!MapleQuest.exists(qid)) {
			return "0";
		}
		int progress = getCharacter().getQuest(qid).getAnyProgress();

		return String.valueOf(progress);
	}

	public String getStringQuestProgress(int qid, int pid) {
		if(!MapleQuest.exists(qid)) {
			return "0";
		}
		int progress = getCharacter().getQuest(qid).getProgress(pid);

		return String.valueOf(progress);
	}

	public void resetAllQuestProgress(int qid) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		getCharacter().getQuest(qid).getProgress().clear();
		getCharacter().updateQuest(getCharacter().getQuest(qid));
	}

	public void resetQuestProgress(int qid, int pid) {
		if(!MapleQuest.exists(qid)) {
			return;
		}
		getCharacter().getQuest(qid).getProgress().remove(pid);
		getCharacter().updateQuest(getCharacter().getQuest(qid));
	}

	public Item evolvePet(int slot, int afterId) {

		MaplePetInstance target;

		long period = TimeUnit.DAYS.toMillis(90);// Expiration

		target = getCharacter().getPets()[slot];

		if (target == null) {
			getCharacter().sendMessage(MessageType.POPUP, "Pet could not be evolved");
			return null;
		}

		Item tmp = gainItem(afterId, 1, false, true, period, target);

		getCharacter().despawnPet(target.getSource());
		getCharacter().getInventory(InventoryType.CASH).removeItem(target.getSource());

		return tmp;
	}

	public void gainItem(int id, int amount) {
		gainItem(id, amount, false, true);
	}

	public void gainItem(int id, int amount, boolean show) {
		gainItem(id, amount, false, show);
	}

	public void gainItem(int id, boolean show) {
		gainItem(id, 1, false, show);
	}

	public void gainItem(int id) {
		gainItem(id, 1, false, true);
	}

	public Item gainItem(int id, int amount, boolean randomStats, boolean show) {
		return gainItem(id, amount, randomStats, show, -1);
	}

	public Item gainItem(int id, int quantity, boolean randomStats, boolean showMessage, long expires) {
		return gainItem(id, quantity, randomStats, showMessage, expires, null);
	}

	public Item gainItem(int id, int amount, boolean randomStats, boolean show, long expire, MaplePetInstance from) {
		Inventory inv = getInventory(InventoryType.getByItemId(id));

		Item item = null;

		if (amount >= 0) {
			if (ItemType.PET.isThis(id)) {

				if (from != null) {

					item = ItemFactory.getItem(id, amount, from.getOwner().getName(),
							System.currentTimeMillis() - expire);

					PetItem pi = (PetItem) item;

					pi.setCloseness(from.getSource().getCloseness());
					pi.setFullness(from.getSource().getFullness());
					pi.setPetLevel(from.getSource().getPetLevel());
					pi.setSummoned(true);
					pi.setPetName(from.getSource().getPetName() == null ? ItemInfoProvider.getItemName(id)
							: from.getSource().getPetName());

				}

				// We didnt randomize it, oh well....

			} else {
				item = ItemFactory.getItem(id, amount, null, expire);
			}

			if (!inv.addItem(item)) {
				return null;
			}
			
		} else {
			inv.removeItem(id, amount * -1);
		}

		if (show) {
			getClient().sendPacket(PacketFactory.getShowItemGain(id, amount, true));
		}

		return item;
	}

	public void gainFame(int amount) {
		getCharacter().gainFame(amount);
	}

	public void changeMusic(String name) {
		getMap().broadcastPacket(PacketFactory.musicChange(name));
	}

	public void playerMessage(int type, String message) {
		getCharacter().sendMessage(MessageType.byId(type), message);
	}

	public void message(String msg) {
		getCharacter().sendMessage(MessageType.PINK_TEXT, msg);
	}

	public void mapMessage(int type, String msg) {
		MessageType t = MessageType.byId(type);

		getMap().getPlayers().forEach(pl -> pl.sendMessage(t, msg));

	}

	public void mapEffect(String path) {
		getClient().sendPacket(PacketFactory.showEffect(path));
	}

	public void mapSound(String path) {
		getClient().sendPacket(PacketFactory.playSound(path));
	}

	public MapleGuild getGuild() {
		return getCharacter().getGuild();
	}

	public void guildMessage(int type, String msg) {
		if (getGuild() != null) {
			getGuild().broadcastPacket(PacketFactory.getServerMessagePacket(MessageType.byId(type), msg, -1, false));
		}
	}

	public boolean isLeader() {
		return isPartyLeader();
	}

	public boolean isGuildLeader() {
		return getGuild().getRankLevel(getCharacter()) == MapleGuildRankLevel.MASTER;
	}

	public boolean isPartyLeader() {
		return getParty() == null ? false : getParty().isLeader(getCharacter());
	}

	public boolean isEventLeader() {
		return false;
	}

	public void givePartyItems(int id, int amount, List<MapleCharacter> party) {
		for (MapleCharacter pl : party) {
			if (amount >= 0) {
				pl.getInventory(id).addItem(ItemFactory.getItem(id, amount));
			} else {
				pl.getInventory(id).removeItem(id, amount * -1);
			}

			pl.getClient().sendPacket(PacketFactory.getShowItemGain(id, amount, true));
		}
	}

	public void removeHPQItems() {
		int[] items = { 4001095, 4001096, 4001097, 4001098, 4001099, 4001100, 4001101 };
		for (int i = 0; i < items.length; i++) {
			removePartyItems(items[i]);
		}
	}

	public void removePartyItems(int id) {
		if (getParty() == null) {
			removeAll(id);
			return;
		}
		for (PartyEntry entry : getParty().getMembers()) {
			MapleCharacter chr = entry.getSnapshot().getLiveCharacter().orElseGet(() -> null);
			if (chr != null && chr.getClient().getLoginStatus() == LoginStatus.LOGGED_IN) {
				removeAll(id, chr.getClient());
			}
		}
	}

	public void giveCharacterExp(int amount, MapleCharacter chr) {
		chr.giveExp(amount);
	}

	public void givePartyExp(int amount, List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			giveCharacterExp(amount, chr);
		}
	}

	public void givePartyExp(String PQ) {
		givePartyExp(PQ, true);
	}

	public void givePartyExp(String PQ, boolean instance) {
		// 1 player = +0% bonus (100)
		// 2 players = +0% bonus (100)
		// 3 players = +0% bonus (100)
		// 4 players = +10% bonus (110)
		// 5 players = +20% bonus (120)
		// 6 players = +30% bonus (130)
		MapleParty party = getParty();
		int size = party.getMembers().size();

		if (instance) {
			for (PartyEntry entry : party.getMembers()) {
				if (!entry.getSnapshot().isOnline()) {
					size--;
				}
			}
		}

		int bonus = size < 4 ? 100 : 70 + (size * 10);
		for (PartyEntry entry : party.getMembers()) {

			MapleCharacter member = entry.getSnapshot().getLiveCharacter().orElseGet(() -> null);

			if (member == null) {
				continue;
			}

			/*
			 * if(instance && member.getEventInstance() == null){ continue; // They aren't
			 * in the instance, don't give EXP. }
			 */
			/* int base = PartyQuest.getExp(PQ, member.getLevel()); */
			int base = 500;// lol
			int exp = base * bonus / 100;
			giveCharacterExp(exp, member);
		}
	}

	public void removeFromParty(int id, List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			InventoryType type = InventoryType.getById(id);
			Inventory iv = chr.getInventory(type);
			int possesed = iv.countById(id);
			if (possesed > 0) {
				iv.removeItem(id, possesed);
				getClient().sendPacket(PacketFactory.getShowItemGain(id, (short) -possesed, true));
			}
		}
	}

	public void removeAll(int id) {
		removeAll(id, getClient());
	}

	public void removeAll(int id, MapleClient cl) {
		InventoryType invType = InventoryType.getByItemId(id);
		int possessed = cl.getCharacter().getInventory(invType).countById(id);
		Inventory iv = cl.getCharacter().getInventory(invType);
		if (possessed > 0) {
			iv.removeItem(id, possessed);
			getClient().sendPacket(PacketFactory.getShowItemGain(id, (short) -possessed, true));
		}

		if (invType == InventoryType.EQUIP) {
			if (cl.getCharacter().getInventory(InventoryType.EQUIPPED).countById(id) > 0) {
				cl.getCharacter().getInventory(InventoryType.EQUIPPED).removeItem(id,
						cl.getCharacter().getInventory(InventoryType.EQUIPPED).countById(id));
				getClient().sendPacket(PacketFactory.getShowItemGain(id, (short) -possessed, true));
			}
		}
	}

	public int getMapId() {
		return getCharacter().getMapId();
	}

	public int getPlayerCount(int mapId) {
		return getMap(mapId).countObjectsOfType(MapleMapObjectType.PLAYER);
	}

	public void showInstruction(String msg, int width, int height) {
		getCharacter().sendHint(msg, width, height);
	}

	public void disableMinimap() {
		// no op I guess for now
	}

	public boolean isAllReactorState(int reactorId, int state) {
		return false;// Umm
	}

	public void resetMap(int mapid) {
		getMap(mapid).resetReactors();
		getMap(mapid).getMonsters().forEach(mon -> mon.kill());
	}

	public void cancelItem(int id) {
		getCharacter().cancelEffect(ItemInfoProvider.getItemEffect(id), false, -1);
	}

	public void teachSkill(int id, int level, int master, long exp) {
		getCharacter().changeSkillLevel(SkillFactory.getSkill(id), level, master);
	}

	public void removeEquipFromSlot(int slot) {
		getCharacter().getInventory(InventoryType.EQUIPPED).removeItemFromSlot(slot, 1);
	}

	public void gainAndEquip(int id, int slot) {
		Item old = getCharacter().getInventory(InventoryType.EQUIPPED).getItem(slot);

		if (old != null) {
			getCharacter().getInventory(InventoryType.EQUIPPED).removeItemFromSlot(slot, old.getAmount());
		}

		Item newItem = ItemFactory.getItem(id, 1);

		getCharacter().getInventory(InventoryType.EQUIPPED).setItem(slot, newItem);
	}

	public void spawnNpc(int id, Point pos, MapleMap map) {

		MapleNPC npc = MapleLifeFactory.getNPC(id);

		if (npc != null) {
			npc.setPosition(pos);
			npc.setCy(pos.y);
			npc.setRx0(pos.x + 50);
			npc.setRx1(pos.x - 50);
			npc.setFh(map.getFootholds().findBelow(pos).getId());
			map.addMapObject(npc, true);
		}

	}

	public void spawnMonster(int id, int x, int y) {
		MapleMonster mon = MapleLifeFactory.getMonster(id);
		mon.setPosition(new Point(x, y));
		getMap().spawnMonster(mon);
	}

	public MapleMonster getMonsterLifeFactory(int mid) {
		MapleMonster mon = MapleLifeFactory.getMonster(mid);

		return mon;
	}

	public void removeGuide() {
		getClient().sendPacket(PacketFactory.spawnGuide(false));
	}

	public void displayGuide(int num) {
		getClient().sendPacket(PacketFactory.showInfo("UI/tutorial.img/" + num));
	}

	public void goDojoUp() {
		getCharacter().sendMessage(MessageType.POPUP, "Dojo not ready!");
	}

	public void resetPartyDojoEnergy() {
		getCharacter().sendMessage(MessageType.POPUP, "Dojo not ready!");
	}

	public void enableActions() {
		getClient().sendReallowActions();

	}

	public void talkGuide(String msg) {
		guideTalk(msg);
	}
	
	public void giveCash(int id, int amount) {
		giveCashShopCurrency(CashShopCurrency.getById(id), amount);
	}

	/**
	 * The purpose of this class is to perfectly map to odinms's maple party
	 * 
	 * @author Tyler
	 *
	 */
	public static class OdinMapleParty {

		private MapleParty handle;

		public OdinMapleParty(MapleParty handle) {
			this.handle = handle;
		}

	}

}
