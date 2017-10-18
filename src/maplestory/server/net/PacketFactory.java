package maplestory.server.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.awt.Point;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import maplestory.cashshop.CashShopInventory;
import maplestory.cashshop.CashShopItemData;
import maplestory.cashshop.CashShopPackage;
import maplestory.cashshop.CashShopWallet;
import maplestory.cashshop.CashShopWallet.CashShopCurrency;
import maplestory.channel.MapleChannel;
import maplestory.channel.MapleSocketChannel;
import maplestory.client.MapleClient;
import maplestory.guild.GuildEntry;
import maplestory.guild.GuildOperationType;
import maplestory.guild.MapleGuild.MapleGuildInviteResponse;
import maplestory.guild.MapleGuildRankLevel;
import maplestory.guild.MapleGuild;
import maplestory.guild.bbs.BulletinPost;
import maplestory.guild.bbs.BulletinReply;
import maplestory.guild.bbs.GuildBulletin;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryOperation;
import maplestory.inventory.InventoryType;
import maplestory.inventory.InventoryOperation.OperationType;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.DueyParcel;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.inventory.item.ScrollResult;
import maplestory.inventory.storage.MapleStorageBox;
import maplestory.inventory.storage.MapleStorageBox.StoragePacketType;
import maplestory.life.MapleHiredMerchant;
import maplestory.life.MapleMonster;
import maplestory.life.MapleMount;
import maplestory.life.MapleNPC;
import maplestory.life.MapleSummon;
import maplestory.life.MobSkill;
import maplestory.life.movement.MovementPath;
import maplestory.map.MapleMagicDoor;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapItem;
import maplestory.map.MapleReactor;
import maplestory.map.MapleMapItem.DropType;
import maplestory.map.MapleMist;
import maplestory.party.MapleParty;
import maplestory.party.PartyOperationType;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.player.MapleNote;
import maplestory.player.MaplePetInstance;
import maplestory.player.monsterbook.MonsterCard;
import maplestory.player.ui.HiredMerchantInterface;
import maplestory.player.ui.HiredMerchantInterface.HiredMerchantItem;
import maplestory.player.ui.TradeInterface;
import maplestory.player.ui.UserInterface;
import maplestory.quest.MapleQuestInstance;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.server.MapleStory;
import maplestory.server.net.handlers.channel.PlayerInteractionHandler;
import maplestory.server.net.handlers.channel.AbstractDealDamageHandler.AttackInfo;
import maplestory.server.net.handlers.channel.GroupChatHandler.GroupChatType;
import maplestory.server.net.handlers.channel.SummonAttackHandler.SummonAttack;
import maplestory.shop.MapleShop;
import maplestory.shop.MapleShopItem;
import maplestory.skill.MonsterStatusEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillData;
import maplestory.skill.SkillFactory;
import maplestory.util.Hex;
import maplestory.util.Pair;
import maplestory.util.Randomizer;
import maplestory.util.StringUtil;
import maplestory.world.World;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketWriter;
import constants.MapleBuffStat;
import constants.MapleDisease;
import constants.MapleEmote;
import constants.MapleStat;
import constants.MessageType;
import constants.MonsterStatus;
import constants.SmegaType;
import constants.Song;
import constants.SpecialEffect;
import constants.skills.Bowmaster;
import constants.skills.Buccaneer;
import constants.skills.Corsair;
import constants.skills.Marksman;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;

public class PacketFactory {

	private final static long FT_UT_OFFSET = 116444592000000000L;
	private final static long DEFAULT_TIME = 150842304000000000L;
	public final static long ZERO_TIME = 94354848000000000L;
	private final static long PERMANENT = 150841440000000000L;
	private static final List<Pair<MapleStat, Integer>> EMPTY_STAT_LIST = new ArrayList<>();

    public static long getTime(long realTimestamp) {
        if (realTimestamp == -1) {
            return DEFAULT_TIME;//high number ll
        } else if (realTimestamp == -2) {
            return ZERO_TIME;
        } else if (realTimestamp == -3) {
            return PERMANENT;
        }
        return realTimestamp * 10000 + FT_UT_OFFSET;
    }
    
	private static void writeLongMask(final MaplePacketWriter mplew, List<Pair<MapleBuffStat, Integer>> statups) {
		long firstmask = 0;
		long secondmask = 0;
		for (Pair<MapleBuffStat, Integer> statup : statups) {
			if (statup.getLeft().isFirst()) {
				firstmask |= statup.getLeft().getValue();
			} else {
				secondmask |= statup.getLeft().getValue();
			}
		}
		mplew.writeLong(firstmask);
		mplew.writeLong(secondmask);
	}

	public static byte[] yellowPopupTip(String tip){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.SCRIPT_PROGRESS_MESSAGE.getValue());
		out.writeMapleAsciiString(tip);
		
		return out.getPacket();
	}
	
	public static byte[] yellowTip(String tip){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.SET_WEEK_EVENT_MESSAGE.getValue());
		out.write(0xFF);
		out.writeMapleAsciiString(tip);
		out.writeShort(0);
		
		return out.getPacket();
	}
    
    public static byte[] removePlayerFromMap(int cid) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);
        return mplew.getPacket();
    }
    
    public static byte[] removeNpc(MapleNPC npc){
    	MaplePacketWriter mplew = new MaplePacketWriter();
    	mplew.writeShort(SendOpcode.REMOVE_NPC.getValue());
    	mplew.writeInt(npc.getObjectId());
    	return mplew.getPacket();
    }
    
    /**
     * Gets a packet telling the client to change maps.
     *
     * @param to The <code>MapleMap</code> to warp to.
     * @param spawnPoint The spawn portal number to spawn at.
     * @param chr The character warping to <code>to</code>
     * @return The map change packet.
     */
    public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannelId());
        mplew.writeInt(0);//updated
        mplew.write(0);//updated
        mplew.writeInt(to.getMapId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getHp());
        mplew.write(0);
        mplew.writeLong(getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }
    
    /**
     * Gets a packet spawning a player as a mapobject to other clients.
     *
     * @param chr The character to spawn to other clients.
     * @return The spawn player packet.
     */
    public static byte[] spawnPlayerMapObject(MapleCharacter chr) {
        MaplePacketWriter writer = new MaplePacketWriter();
        writer.writeShort(SendOpcode.SPAWN_PLAYER.getValue());
        writer.writeInt(chr.getId());
        writer.write(chr.getLevel()); //v83
        writer.writeMapleAsciiString(chr.getName());
        MapleGuild guild = chr.getGuild();
        if (guild == null) {
            writer.writeMapleAsciiString("");
            writer.write(new byte[6]);
        } else {
        	writer.writeMapleAsciiString(guild.getName());
        	writer.writeGuildEmblem(guild.getEmblem());
        }
        writer.writeInt(0);
        writer.writeShort(0); //v83
        writer.write(0xFC);
        writer.write(1);
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != 0) {
            writer.writeInt(2);
        } else {
            writer.writeInt(0);
        }
        long buffmask = 0;
        int buffValue = -1;
        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != 0) {
            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != 0) {
            buffmask |= MapleBuffStat.COMBO.getValue();
            buffValue = chr.getBuffedValue(MapleBuffStat.COMBO);
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != 0) {
            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != 0) {
            buffmask |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != 0) {
            buffValue = chr.getBuffedValue(MapleBuffStat.MORPH);
        }
        if (chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE) != 0) {
            buffmask |= MapleBuffStat.ENERGY_CHARGE.getValue();
            buffValue = chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE);
        }//AREN'T THESE 
        writer.writeInt((int) ((buffmask >> 32) & 0xffffffffL));
        if (buffValue >= 0) {
            if (chr.getBuffedValue(MapleBuffStat.MORPH) != 0) { //TEST
                writer.writeShort(buffValue);
            } else {
                writer.write(buffValue);
            }
        }
        writer.writeInt((int) (buffmask & 0xffffffffL));
        int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        writer.skip(6);
        writer.writeInt(CHAR_MAGIC_SPAWN);
        writer.skip(11);
        writer.writeInt(CHAR_MAGIC_SPAWN);//v74
        writer.skip(11);
        writer.writeInt(CHAR_MAGIC_SPAWN);
        writer.writeShort(0);
        writer.write(0);
        Item mount = chr.getInventory(InventoryType.EQUIPPED).getItem(-18);
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != 0 && mount != null) {
            writer.writeInt(mount.getItemId());
            writer.writeInt(1004);
        } else {
            writer.writeLong(0);
        }
        writer.writeInt(CHAR_MAGIC_SPAWN);
        writer.skip(9);
        writer.writeInt(CHAR_MAGIC_SPAWN);
        writer.writeShort(0);
        writer.writeInt(0); // actually not 0, why is it 0 then?
        writer.skip(10);
        writer.writeInt(CHAR_MAGIC_SPAWN);
        writer.skip(13);
        writer.writeInt(CHAR_MAGIC_SPAWN);
        writer.writeShort(0);
        writer.write(0);
        writer.writeShort(chr.getJob().getId());
        addCharLook(writer, chr, false);
        writer.writeInt(chr.getInventory(InventoryType.CASH).countById(5110000));
        writer.writeInt(/*chr.getItemEffect()*/0);
        writer.writeInt(ItemType.CHAIR.isThis(chr.getActiveChair()) ? chr.getActiveChair() : 0);
        writer.writePos(chr.getPosition());
        writer.write(chr.getStance());
        writer.writeShort(chr.getFh());//chr.getFh()
        writer.write(0);
        
        for(MaplePetInstance pet : chr.getPets()){
        	if(pet != null){
        		writer.writePetInfo(pet, false);
        	}
        }
        
        writer.write(0);

        if (chr.getMount() == null) {
            writer.writeInt(1); // mob level
            writer.writeLong(0); // mob exp + tiredness
        } else {
            writer.writeInt(chr.getMount().getLevel());
            writer.writeInt(chr.getMount().getExp());
            writer.writeInt(chr.getMount().getTiredness());
        }
        /*if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr)) {
            if (chr.getPlayerShop().hasFreeSlot()) {
                addAnnounceBox(mplew, chr.getPlayerShop(), chr.getPlayerShop().getVisitors().length);
            } else {
                addAnnounceBox(mplew, chr.getPlayerShop(), 1);
            }
        } else if (chr.getMiniGame() != null && chr.getMiniGame().isOwner(chr)) {
            if (chr.getMiniGame().hasFreeSlot()) {
                addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 1, 0);
            } else {
                addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 2, 1);
            }
        } else {
            */writer.write(0);/*
        }*/
        if (chr.getChalkboardText() != null) {
            writer.write(1);
            writer.writeMapleAsciiString(chr.getChalkboardText());
        } else {
            writer.write(0);
        }
        addRingLook(writer, chr, true);
        addRingLook(writer, chr, false);
        addMarriageRingLook(writer, chr);
        writer.skip(3);
        writer.write(/*chr.getTeam()*/0);//only needed in specific fields
        return writer.getPacket();
    }
    

    private static void addRingLook(MaplePacketWriter mplew, MapleCharacter chr, boolean crush) {
        /*List<MapleRing> rings;
        if (crush) {
            rings = chr.getCrushRings();
        } else {
            rings = chr.getFriendshipRings();
        }*/
        boolean yes = false;
        /*for (MapleRing ring : rings) {
            if (ring.equipped()) {
                if (!yes) {
                    yes = true;
                    mplew.write(1);
                }
                mplew.writeInt(ring.getRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getPartnerRingId());
                mplew.writeInt(0);
                mplew.writeInt(ring.getItemId());
            }
        }*/
        if (!yes) {
            mplew.write(0);
        }
    }

    private static void addMarriageRingLook(MaplePacketWriter mplew, MapleCharacter chr) {
        /*if (chr.getMarriageRing() != null && !chr.getMarriageRing().equipped()) {
            mplew.write(0);
            return;
        }*/
        mplew.writeBool(/*chr.getMarriageRing() != null*/ false);
        /*if (chr.getMarriageRing() != null) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing().getPartnerChrId());
            mplew.writeInt(chr.getMarriageRing().getRingId());
        }*/
    }

	public static byte[] movePlayer(int cid, MovementPath path) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.MOVE_PLAYER.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(0);
		mplew.writeMovementPath(path);
		return mplew.getPacket();
	}
    
    public static byte[] addNewCharEntry(MapleCharacter chr) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(0);
        addCharEntry(mplew, chr, false);
        return mplew.getPacket();
    }
	
	public static byte[] getHandshakePacket(int version, byte[] recvIv, byte[] sendIv, int locale){
		MaplePacketWriter packet = new MaplePacketWriter();
		
        packet.writeShort(0x0E);
        packet.writeShort(version);
        packet.writeShort(1);
        packet.write(49);
        packet.write(recvIv);
        packet.write(sendIv);
        packet.write(locale);
		
		return packet.getPacket();

	}

    /**
     * Gets a packet detailing a server status message.
     *
     * Possible values for
     * <code>status</code>:<br> 0 - Normal<br> 1 - Highly populated<br> 2 - Full
     *
     * @param status The server status.
     * @return The server status packet.
     */
    public static byte[] getServerStatus(int status) {
        MaplePacketWriter mplew = new MaplePacketWriter(4);
        mplew.writeShort(SendOpcode.SERVERSTATUS.getValue());
        mplew.writeShort(status);
        return mplew.getPacket();
    }
	
    /**
     * Gets a packet saying that the server list is over.
     *
     * @return The end of server list packet.
     */
    public static byte[] getEndOfServerList() {
        MaplePacketWriter mplew = new MaplePacketWriter(3);
        mplew.writeShort(SendOpcode.SERVERLIST.getValue());
        mplew.write(0xFF);
        return mplew.getPacket();
    }
	
    /**
     * Gets a packet detailing a server and its channels.
     *
     * @param serverId
     * @param serverName The name of the server.
     * @param world 
     * @param channelLoad Load of the channel - 1200 seems to be max.
     * @return The server info packet.
     */
    public static byte[] getServerList(String eventMsg, World world) {
    	//i, world.getName(), world.getEventFlag(), world.getEventMessage(this), 
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SERVERLIST.getValue());
        mplew.write(world.getId());
        mplew.writeMapleAsciiString(world.getName());
        mplew.write(world.getEventFlag().ordinal());
        mplew.writeMapleAsciiString(eventMsg);
        mplew.write(100); // rate modifier, don't ask O.O!
        mplew.write(0); // event xp * 2.6 O.O!
        mplew.write(100); // rate modifier, don't ask O.O!
        mplew.write(0); // drop rate * 2.6
        mplew.write(0);
        mplew.write(world.getChannelCount());
        for (MapleChannel ch : world.getChannels()) {
            mplew.writeMapleAsciiString(world.getName() + "-" + (ch.getId() + 1));
            mplew.writeInt((ch.getConnectedPlayerCount() * 1200) / MapleStory.getServerConfig().getChannelLoad());
            mplew.write(1);
            mplew.writeShort(ch.getId());
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }
    
    /**
     * Gets a packet with a list of characters.
     *
     * @param c The MapleClient to load characters of.
     * @param worldId The ID of the server requested.
     * @return The character list packet.
     */
    public static byte[] getCharList(MapleClient c, int worldId) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.CHARLIST.getValue());
        mplew.write(0);
        List<MapleCharacter> chars = c.loadCharacters(worldId);
        mplew.write((byte) chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, false);
        }
        if (MapleStory.getServerConfig().isPicEnabled()) {
            mplew.write(c.isPicCreated() ? 1 : 0);
        } else {
            mplew.write(2);
        }
       
        mplew.writeInt(MapleStory.getServerConfig().getCharacterSlots());
        
        return mplew.getPacket();
    }
    
    public static byte[] showIntro(String path){
    	MaplePacketWriter out = new MaplePacketWriter();
    	out.writeShort(SendOpcode.FIELD_UPDATE.getValue());
    	out.write(0x12);
    	out.writeMapleAsciiString(path);
    	
    	return out.getPacket();
    }
    
    private static void addCharStats(MaplePacketWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId()); // character id
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));
        mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
        mplew.write(chr.getSkinColor()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.writeInt(chr.getHair()); // hair

        for (int i = 0; i < 3; i++) {
        	if(chr.getPets()[i] != null){
        		mplew.writeLong(chr.getPets()[i].getSource().getUniqueId());
        	}else{
           	 	mplew.writeLong(0);	
        	}
        }

        mplew.write(chr.getLevel()); // level
        mplew.writeShort(chr.getJob().getId()); // job
        mplew.writeShort(chr.getBaseStr()); // str
        mplew.writeShort(chr.getBaseDex()); // dex
        mplew.writeShort(chr.getBaseInt()); // int
        mplew.writeShort(chr.getBaseLuk()); // luk
        mplew.writeShort(chr.getHp()); // hp (?)
        mplew.writeShort(chr.getBaseMaxHp()); // maxhp
        mplew.writeShort(chr.getMp()); // mp (?)
        mplew.writeShort(chr.getBaseMaxMp()); // maxmp
        mplew.writeShort(chr.getRemainingAp()); // remaining ap
        mplew.writeShort(chr.getRemainingSp()); // remaining sp
        mplew.writeInt(chr.getExp()); // current exp
        mplew.writeShort(chr.getFame()); // fame
        mplew.writeInt(chr.getGachaExp()); //Gacha Exp
        mplew.writeInt(chr.getMapId()); // current map id
        mplew.write(chr.getInitialSpawnpointId()); // spawnpoint
        mplew.writeInt(0);
    }

    private static void addCharLook(MaplePacketWriter mplew, MapleCharacter chr, boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor()/*chr.getSkinColor().getId()*/); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair()); // hair
        addCharEquips(mplew, chr);
    }

    private static void addCharacterInfo(MaplePacketWriter mplew, MapleCharacter chr) {
        mplew.writeLong(-1);
        mplew.write(0);
        addCharStats(mplew, chr);
        mplew.write(/*chr.getBuddylist().getCapacity()*/ 20);

        if (chr.getLinkedName() == null) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getLinkedName());
        }

        mplew.writeInt(chr.getMeso());
        addInventoryInfo(mplew, chr);
        addSkillInfo(mplew, chr);
        addQuestInfo(mplew, chr);
        addMiniGameInfo(mplew, chr);
        addRingInfo(mplew, chr);
        addTeleportInfo(mplew, chr);
        addMonsterBookInfo(mplew, chr);
        addNewYearInfo(mplew, chr);//have fun!
        addAreaInfo(mplew, chr);//assuming it stayed here xd
        mplew.writeShort(0);
    }
    
    private static void addMonsterBookInfo(MaplePacketWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMonsterBook().hasCover() ? chr.getMonsterBook().getCover() : 0);//Book cover
        mplew.write(0);//Unknown
        List<MonsterCard> cards = chr.getMonsterBook().getCards();
        mplew.writeShort(cards.size());
        for(MonsterCard card : cards){
        	mplew.writeShort(card.getMonsterId() % 10000);
        	mplew.write(card.getLevel());
        }
    }
    

    private static void addAreaInfo(MaplePacketWriter mplew, MapleCharacter chr) {
        mplew.writeShort(0);
    }
    
    private static void addRingInfo(MaplePacketWriter mplew, MapleCharacter chr) {
		mplew.writeShort(0);
		mplew.writeShort(0);
		mplew.writeShort(0);
	}

	private static void addQuestInfo(MaplePacketWriter mplew, MapleCharacter chr) {
		Collection<MapleQuestInstance> started = chr.getQuests(MapleQuestStatus.STARTED);
		mplew.writeShort(started.size());
		for(MapleQuestInstance inst : started){
			mplew.writeShort(inst.getQuest().getId());
			mplew.writeMapleAsciiString(inst.getQuestData());
			if(inst.getQuest().getQuestInfo().getInfoNumber() > 0){
				mplew.writeShort(inst.getQuest().getQuestInfo().getInfoNumber());
				mplew.writeMapleAsciiString(inst.getQuestData());
			}
		}
		Collection<MapleQuestInstance> finished = chr.getQuests(MapleQuestStatus.COMPLETED);
		mplew.writeShort(finished.size());
		for(MapleQuestInstance inst : finished){
			mplew.writeShort(inst.getQuest().getId());
			mplew.writeLong(getTime(inst.getCompletionTime()));
		}
    }
    
    private static void addSkillInfo(MaplePacketWriter mplew, MapleCharacter chr) {
        mplew.write(0); // start of skills
        Map<Integer, SkillData> skills = chr.getSkills();
        mplew.writeShort(skills.size());
        
        for(int skillId : skills.keySet()){
        	SkillData data = skills.get(skillId);
            mplew.writeInt(skillId);
            mplew.writeInt(data.getLevel());
            addExpirationTime(mplew, 1000000);//What even is this?
            Skill skill = SkillFactory.getSkill(skillId);
            if (skill.isFourthJob()) {
                mplew.writeInt(data.getMasterLevel());
            }
        }
        
        mplew.writeShort(chr.getAllCooldowns().size());
        
        for(int skillId : chr.getAllCooldowns()){
        	
        	mplew.writeInt(skillId);
        	long timeLeft = chr.getCooldownTimeLeft(skillId);
        	
        	mplew.writeShort((short) (timeLeft / 1000));
        }
        
    }

    private static void addExpirationTime(MaplePacketWriter mplew, long time) {
        mplew.writeLong(getTime(time));
    }

    
    
    
    private static void addInventoryInfo(MaplePacketWriter mplew, MapleCharacter chr) {
        for (byte i = 1; i <= 5; i++) {
            mplew.write(chr.getInventory(InventoryType.getById(i)).getSize());
        }
        mplew.writeLong(getTime(-2));
        Inventory iv = chr.getInventory(InventoryType.EQUIPPED);
        List<Pair<Integer, Item>> equipped = new ArrayList<>();
        List<Pair<Integer, Item>> equippedCash = new ArrayList<>();
        for(int slot : iv.getItems().keySet()){
        	Item item = iv.getItems().get(slot);
        	if (slot <= -100) {
                equippedCash.add(new Pair<Integer, Item>(slot, item));
            } else {
                equipped.add(new Pair<Integer, Item>(slot, item));
            }
        }
        
        for (Pair<Integer, Item> item: equipped) {
            mplew.writeItemInfo(item.getRight(), item.getLeft());
        }
        mplew.writeShort(0); // start of equip cash
        for (Pair<Integer, Item> item : equippedCash) {
        	mplew.writeItemInfo(item.getRight(), item.getLeft());
        }
        mplew.writeShort(0); // start of equip inventory
        for(int slot : chr.getInventory(InventoryType.EQUIP).getItems().keySet()){
        	Item item = chr.getInventory(InventoryType.EQUIP).getItem(slot);
        	mplew.writeItemInfo(item, slot);
        }
        mplew.writeInt(0);
        for(int slot : chr.getInventory(InventoryType.USE).getItems().keySet()){
        	Item item = chr.getInventory(InventoryType.USE).getItem(slot);
        	mplew.writeItemInfo(item, slot);
        }
        mplew.write(0);
        for(int slot : chr.getInventory(InventoryType.SETUP).getItems().keySet()){
        	Item item = chr.getInventory(InventoryType.SETUP).getItem(slot);
        	mplew.writeItemInfo(item, slot);
        }
        mplew.write(0);
        for(int slot : chr.getInventory(InventoryType.ETC).getItems().keySet()){
        	Item item = chr.getInventory(InventoryType.ETC).getItem(slot);
        	mplew.writeItemInfo(item, slot);
        }
        mplew.write(0);
        for(int slot : chr.getInventory(InventoryType.CASH).getItems().keySet()){
        	Item item = chr.getInventory(InventoryType.CASH).getItem(slot);
        	mplew.writeItemInfo(item, slot);
        }
    }

    private static void addNewYearInfo(MaplePacketWriter mplew, MapleCharacter chr) {
        mplew.writeShort(0);
    }

   private static void addTeleportInfo(MaplePacketWriter mplew, MapleCharacter chr) {
        /*int[] tele = chr.getTrockMaps();
        int[] viptele = chr.getVipTrockMaps();*/
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(910000000);
        }
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(910000000);
        }
    }

    private static void addMiniGameInfo(MaplePacketWriter mplew, MapleCharacter chr) {
        mplew.writeShort(0);
        /*for (int m = size; m > 0; m--) {//nexon does this :P
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         }*/
    }

/*    private static void addAreaInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Map<Short, String> areaInfos = chr.getAreaInfos();
        mplew.writeShort(areaInfos.size());
        for (Short area : areaInfos.keySet()) {
            mplew.writeShort(area);
            mplew.writeMapleAsciiString(areaInfos.get(area));
        }
    }*/
    
    private static void addCharEquips(MaplePacketWriter mplew, MapleCharacter chr) {
        Inventory equip = chr.getInventory(InventoryType.EQUIPPED);
        Map<Integer, Integer> myEquip = new LinkedHashMap<>();
        Map<Integer, Integer> maskedEquip = new LinkedHashMap<>(); 
        for (int pos : equip.getItems().keySet()) {
        	Item item = equip.getItem(pos);
        	
        	int nonCashPos = pos + 100;
        	if(item instanceof CashItem){
        		if(!myEquip.containsKey(nonCashPos)){
        			myEquip.put(nonCashPos, item.getItemId());
        		}
        		else{
        			maskedEquip.put(pos, item.getItemId());
        		}
        	}else{
        		myEquip.put(pos, item.getItemId());
        	}
        }
        
        //For some reason nexon makes the positions for this stuff positive
        //In game it is negative so we send all the values as positive
    	 for (Entry<Integer, Integer> entry : myEquip.entrySet()) {
            mplew.write(-entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        for (Entry<Integer, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(-entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF);
        Item cWeapon = equip.getItems().get(-111);
        mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
        for (int i = 0; i < 3; i++) {
            /*if (chr.getPet(i) != null) {
                mplew.writeInt(chr.getPet(i).getItemId());
            } else {
                mplew.writeInt(0);
            }*/
            mplew.writeInt(0);
        }
        
    }
    
    private static void addCharEntry(MaplePacketWriter mplew, MapleCharacter chr, boolean viewall) {
        addCharStats(mplew, chr);
        addCharLook(mplew, chr, false);
        if (!viewall) {
            mplew.write(0);
        }
        if (chr.isGm()) {
            mplew.write(0);
            return;
        }
        mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled) Short??
        mplew.writeInt(chr.getRank()); // world rank
        mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
        mplew.writeInt(chr.getJobRank()); // job rank
        mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)
    }
	
    /**
     * Gets a login failed packet.
     *
     * Possible values for
     * <code>reason</code>:<br> 3: ID deleted or blocked<br> 4: Incorrect
     * password<br> 5: Not a registered id<br> 6: System error<br> 7: Already
     * logged in<br> 8: System error<br> 9: System error<br> 10: Cannot process
     * so many connections<br> 11: Only users older than 20 can use this
     * channel<br> 13: Unable to log on as master at this ip<br> 14: Wrong
     * gateway or personal info and weird korean button<br> 15: Processing
     * request with that korean button!<br> 16: Please verify your account
     * through email...<br> 17: Wrong gateway or personal info<br> 21: Please
     * verify your account through email...<br> 23: License agreement<br> 25:
     * Maple Europe notice =[ FUCK YOU NEXON<br> 27: Some weird full client
     * notice, probably for trial versions<br>
     *
     * @param reason The reason logging in failed.
     * @return The login failed packet.
     */
    public static byte[] getLoginFailed(int reason) {
    	MaplePacketWriter packet = new MaplePacketWriter();
        packet.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        packet.write(reason);
        packet.write(0);
        packet.writeInt(0);
        return packet.getPacket();
    }
    
    /**
     * Gets a successful authentication and PIN Request packet.
     *
     * @param c
     * @param account The account name.
     * @return The PIN request packet.
     */
    public static byte[] getAuthSuccess(MapleClient c) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(c.getId()); //user id
        mplew.write(0);//Gender, 0=male 1=female
        mplew.writeBool(c.isGM()); //admin byte
        short toWrite = (short) (0 * 32);
        //toWrite = toWrite |= 0x100; only in higher versions
        mplew.write(toWrite > 0x80 ? 0x80 : toWrite);//0x80 is admin, 0x20 and 0x40 = subgm
        mplew.writeBool(false);
        //mplew.writeShort(toWrite > 0x80 ? 0x80 : toWrite); only in higher versions...
        mplew.writeMapleAsciiString(c.getUsername());
        mplew.write(0);
        mplew.write(0); //isquietbanned
        mplew.writeLong(0);//isquietban time
        mplew.writeLong(0); //creation time
        mplew.writeInt(0);
        mplew.writeShort(2);//PIN

        return mplew.getPacket();
    }
    
    public static byte[] getPing(){
    	ByteBuf buf = Unpooled.buffer(2).order(ByteOrder.LITTLE_ENDIAN);
    	buf.writeShort(SendOpcode.PING.getValue());
    	return buf.array();
    }
	
	@SneakyThrows
	public static ByteBuf writeString(ByteBuf buf, String str){
		byte[] b = str.getBytes("UTF-8");
		return buf.writeChar(b.length).writeBytes(b);
	}

	public static byte[] getNameCheckResponse(String name, boolean inUse) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.CHAR_NAME_RESPONSE.getValue());
		mplew.writeMapleAsciiString(name);
		mplew.writeBool(inUse);
		return mplew.getPacket();
	}
	
    /**
     * Gets a packet telling the client the IP of the channel server.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @param clientId The ID of the client.
     * @return The server IP packet.
     */
	public static byte[] getChannelIP(InetAddress address, int port, int id2) {
		MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        byte[] addr = address.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        mplew.writeInt(id2);
        mplew.write(new byte[]{0, 0, 0, 0, 0});
        return mplew.getPacket();
	}
	
    /**
     * Gets a packet telling the client the IP of the new channel.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @return The server IP packet.
     */
    public static byte[] getChannelChange(InetAddress inetAddr, int port) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        return mplew.getPacket();
    }

    /**
     * Gets character info for a character.
     *
     * @param chr The character to get info about.
     * @return The character info packet.
     */
    public static byte[] getCharInfo(MapleCharacter chr) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel().getId());
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);
        mplew.writeInt((int) chr.getDamageNumberGenerator().getSeed1());
        mplew.writeInt((int) chr.getDamageNumberGenerator().getSeed2());
        mplew.writeInt((int) chr.getDamageNumberGenerator().getSeed3());
/*        for (int i = 0; i < 3; i++) {//These are the damage seeds
            mplew.writeInt(Randomizer.nextInt());
        }*/
        addCharacterInfo(mplew, chr);
        mplew.writeLong(getTime(System.currentTimeMillis()));
        
        return mplew.getPacket();
    }

	public static byte[] getWrongPic() {
		MaplePacketWriter mplew = new MaplePacketWriter(3);
		mplew.writeShort(SendOpcode.CHECK_SPW_RESULT.getValue());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] getGenderPacket(MapleCharacter chr) {
		MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SET_GENDER.getValue());
        mplew.writeInt(chr.getGender());
		return mplew.getPacket();
	}

	public static byte[] getKeybindings(Map<Integer, Pair<Integer, Integer>> keyBindings) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.KEYMAP.getValue());
		mplew.write(0);
		for (int x = 0; x < 90; x++) {
			Pair<Integer, Integer> key = keyBindings.get(Integer.valueOf(x));
			if (key != null) {
				mplew.write(key.getLeft());
				mplew.writeInt(key.getRight());
			} else {
				mplew.write(0);
				mplew.writeInt(0);
			}
		}
		return mplew.getPacket();
	}
	
	public static byte[] showNotes(List<MapleNote> notes) {
		MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
        mplew.write(3);
        mplew.write(notes.size());
        for(MapleNote note : notes){
			mplew.writeInt(note.getId());
			mplew.writeMapleAsciiString(note.getFrom()+ " ");// Stupid nexon forgot space
													// lol
			mplew.writeMapleAsciiString(note.getContent());
			mplew.writeLong(getTime(note.getCreationTime()));
			mplew.write(note.getFame());// FAME :D
        }
        return mplew.getPacket();
	}

	    /**
     * Gets a general chat packet.
     *
     * @param cidfrom The character ID who sent the chat.
     * @param text The text of the chat.
     * @param whiteBG
     * @param show
     * @return The general chat packet.
     */
    public static byte[] getGeneralChatPacket(int cidfrom, String text, boolean gm, int show) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.writeBool(gm);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);
        return mplew.getPacket();
    }

	public static byte[] getAllowActionsPacket() {
		return updatePlayerStats(EMPTY_STAT_LIST, true);
	}
	
	/**
	 * Gets an update for specified stats.
	 *
	 * @param stats The stats to update.
	 * @return The stat update packet.
	 */
	public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats, MapleCharacter chr) {
		return updatePlayerStats(stats, false, chr);
	}
	
	/**
	 * Gets an update for specified stats.
	 *
	 * @param stats The list of stats to update.
	 * @param itemReaction Result of an item reaction(?)
	 * @return The stat update packet.
	 */
	public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction, MapleCharacter chr) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.STAT_CHANGED.getValue());
		mplew.write(itemReaction ? 1 : 0);
		int updateMask = 0;
		for (Pair<MapleStat, Integer> statupdate : stats) {
			updateMask |= statupdate.getLeft().getValue();
		}
		List<Pair<MapleStat, Integer>> mystats = stats;
		if (mystats.size() > 1) {
			Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
				@Override
				public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
					int val1 = o1.getLeft().getValue();
					int val2 = o2.getLeft().getValue();
					return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
				}
			});
		}
		mplew.writeInt(updateMask);
		for (Pair<MapleStat, Integer> statupdate : mystats) {
			if (statupdate.getLeft().getValue() >= 1) {
				if (statupdate.getLeft().getValue() == 0x1) {
					mplew.writeShort(statupdate.getRight().shortValue());
				} else if (statupdate.getLeft().getValue() <= 0x4) {
					mplew.writeInt(statupdate.getRight());
				} else if (statupdate.getLeft().getValue() < 0x20) {
					mplew.write(statupdate.getRight().shortValue());
				} else if (statupdate.getLeft().getValue() == 0x8000) {
					/*if (GameConstants.hasSPTable(chr.getJob())) {
						mplew.write(chr.getRemainingSpSize());
						for (int i = 0; i < chr.getRemainingSps().length; i++) {
							if (chr.getRemainingSpBySkill(i) > 0) {
								mplew.write(i + 1);
								mplew.write(chr.getRemainingSpBySkill(i));
							}
						}
					} else {
						mplew.writeShort(statupdate.getRight().shortValue());
					}*/
					
					mplew.writeShort(statupdate.getRight().shortValue());
				} else if (statupdate.getLeft().getValue() < 0xFFFF) {
					mplew.writeShort(statupdate.getRight().shortValue());
				} else {
					mplew.writeInt(statupdate.getRight().intValue());
				}
			}
		}
		return mplew.getPacket();
	}
	
    /**
     * Gets an update for specified stats.
     *
     * @param stats The list of stats to update.
     * @param itemReaction Result of an item reaction(?)
     * @return The stat update packet.
     */
    public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.STAT_CHANGED.getValue());
        mplew.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
                @Override
                public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        mplew.writeInt(updateMask);
        for (Pair<MapleStat, Integer> statupdate : mystats) {
            if (statupdate.getLeft().getValue() >= 1) {
                if (statupdate.getLeft().getValue() == 0x1) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() <= 0x4) {
                    mplew.writeInt(statupdate.getRight());
                } else if (statupdate.getLeft().getValue() < 0x20) {
                    mplew.write(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else {
                    mplew.writeInt(statupdate.getRight().intValue());
                }
            }
        }
        return mplew.getPacket();
    }
	
    
    public static byte[] spawnNPC(MapleNPC life) {
        MaplePacketWriter mplew = new MaplePacketWriter(24);
        mplew.writeShort(SendOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        if (life.getF() == 1) {
            mplew.write(0);
        } else {
            mplew.write(1);
        }
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        MaplePacketWriter mplew = new MaplePacketWriter(23);
        mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        if (life.getF() == 1) {
            mplew.write(0);
        } else {
            mplew.write(1);
        }
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.writeBool(MiniMap);
        return mplew.getPacket();
    }

	public static byte[] getNpcTalkPacket(int id, short something) {
		MaplePacketWriter mplew = new MaplePacketWriter(23);
        mplew.writeShort(SendOpcode.NPC_ACTION.getValue());
        mplew.writeInt(id);
        mplew.writeShort(something);
        
		return mplew.getPacket();
	}
	
	public static byte[] getNpcActionPacket(byte[] data) {
		MaplePacketWriter mplew = new MaplePacketWriter(23);
        mplew.writeShort(SendOpcode.NPC_ACTION.getValue());
        mplew.write(data);
        
		return mplew.getPacket();
	}
	
	/**
	 * Handles monsters not being targettable, such as Zakum's first body.
	 *
	 * @param life The mob to spawn as non-targettable.
	 * @return The packet to spawn the mob as non-targettable.
	 */
	public static byte[] getSpawnUntargetableMonsterPacket(MapleMonster life) {
		final MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(1);
		mplew.writeInt(life.getObjectId());
		mplew.write(5);
		mplew.writeInt(life.getId());
		mplew.skip(15);
		mplew.write(0x88);
		mplew.skip(6);
		mplew.writePos(life.getPosition());
		mplew.write(life.getStance());
		mplew.writeShort(0);//life.getStartFh()
		mplew.writeShort(life.getFh());
		mplew.writeShort(-2);
		mplew.write(life.getTeam());
		mplew.writeInt(0);
		return mplew.getPacket();
	}
	
    public static byte[] getMonsterSpawnPacket(MapleMonster monster){
    	MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
        addMonsterInitData(monster, mplew);
        
        return mplew.getPacket();
    }
    
    public static byte[] getMonsterControlPacket(MapleMonster monster, boolean aggro){
    	MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(aggro ? 2 : 1);
        addMonsterInitData(monster, mplew);
        
        return mplew.getPacket();
    }
    
    private static void addMonsterInitData(MapleMonster monster, MaplePacketWriter out){
    	out.writeInt(monster.getObjectId());
        out.write(monster.getController() == null ? 5 : 1);
        out.writeInt(monster.getId());
        out.skip(15);
        out.write(0x88);
        out.skip(6);
        out.writePos(monster.getPosition());
        out.write(monster.getStance());
        out.writeShort(monster.getStartFh()); //Origin FH //life.getStartFh()
        out.writeShort(monster.getFh());

        int effect = monster.getSpawnEffect();
        
        if (effect > 0) {
            out.write(effect);
            out.write(0);
            out.writeShort(0);
            if (effect == 15) {
                out.write(0);
            }
        }
        
        out.write(monster.isSpawned() ? -1 : -2);
        out.write(monster.getTeam());
        out.writeInt(0);
    }
	
    public static byte[] killMonster(int oid, boolean animation) {
        return killMonster(oid, animation ? 1 : 0);
    }

    /**
     * Gets a packet telling the client that a monster was killed.
     *
     * @param oid The objectID of the killed monster.
     * @param animation 0 = dissapear, 1 = fade out, 2+ = special
     * @return The kill monster packet.
     */
    public static byte[] killMonster(int oid, int animation) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(animation);
        mplew.write(animation);
        return mplew.getPacket();
    }
    
    /**
     * Gets a response to a move monster packet.
     *
     * @param objectid The ObjectID of the monster being moved.
     * @param moveid The movement ID.
     * @param currentMp The current MP of the monster.
     * @param useSkills Can the monster use skills?
     * @return The move response packet.
     */
    public static byte[] getMonsterMoveResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
        return getMonsterMoveResponse(objectid, moveid, currentMp, useSkills, 0, 0);
    }

    /**
     * Gets a response to a move monster packet.
     *
     * @param objectid The ObjectID of the monster being moved.
     * @param moveid The movement ID.
     * @param currentMp The current MP of the monster.
     * @param useSkills Can the monster use skills?
     * @param skillId The skill ID for the monster to use.
     * @param skillLevel The level of the skill to use.
     * @return The move response packet.
     */
    public static byte[] getMonsterMoveResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        MaplePacketWriter mplew = new MaplePacketWriter(13);
        mplew.writeShort(SendOpcode.MOVE_MONSTER_RESPONSE.getValue());
        mplew.writeInt(objectid);
        mplew.writeShort(moveid);
        mplew.writeBool(useSkills);
        mplew.writeShort(currentMp);
        mplew.write(skillId);
        mplew.write(skillLevel);
        return mplew.getPacket();
    }
    
    public static byte[] getMoveMonsterPacket(int useskill, int skill, int skill_1, int skill_2, int skill_3, int skill_4, int oid, Point startPos, MovementPath path) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.write(useskill);
        mplew.write(skill);
        mplew.write(skill_1);
        mplew.write(skill_2);
        mplew.write(skill_3);
        mplew.write(skill_4);
        mplew.writePos(startPos);
        mplew.writeMovementPath(path);
        return mplew.getPacket();
    }
    
    /**
	 * state 0 = del ok state 12 = invalid bday state 14 = incorrect pic
	 *
	 * @param cid
	 * @param state
	 * @return
	 */
	public static byte[] getDeleteCharacterResponse(int cid, int state) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.DELETE_CHAR_RESPONSE.getValue());
		mplew.writeInt(cid);
		mplew.write(state);
		return mplew.getPacket();
	}

    public static byte[] closeRangeAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.CLOSE_RANGE_ATTACK.getValue());
        addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
        return mplew.getPacket();
    }

	private static boolean hasDifferentStanceRequirement(int skillId){
		return skillId == Bowmaster.HURRICANE ||
				skillId == Marksman.PIERCING_ARROW ||
				skillId == Corsair.RAPID_FIRE || 
				skillId == WindArcher.HURRICANE;
	}
    
    public static byte[] rangedAttack(MapleCharacter chr, AttackInfo ai, int projectile) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.RANGED_ATTACK.getValue());
        addAttackBody(mplew, chr, ai.skill, ai.skilllevel, hasDifferentStanceRequirement(ai.skill) ? ai.rangedirection : ai.stance, ai.numAttackedAndDamage, projectile, ai.allDamage, ai.speed, ai.direction, ai.display);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] magicAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, Map<Integer, List<Integer>> damage, int charge, int speed, int direction, int display) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.MAGIC_ATTACK.getValue());
        addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
        if (charge != -1) {
            mplew.writeInt(charge);
        }
        return mplew.getPacket();
    }

    private static void addAttackBody(LittleEndianWriter lew, MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
        lew.writeInt(chr.getId());
        lew.write(numAttackedAndDamage);
        lew.write(0x5B);//?
        lew.write(skilllevel);
        if (skilllevel > 0) {
            lew.writeInt(skill);
        }
        lew.write(display);
        lew.write(direction);
        lew.write(stance);
        lew.write(speed);
        lew.write(0x0A);
        lew.writeInt(projectile);
        for (Integer oned : damage.keySet()) {
            List<Integer> onedList = damage.get(oned);
            if (onedList != null) {
                lew.writeInt(oned.intValue());
                lew.write(0xFF);
                if (skill == 4211006) {
                    lew.write(onedList.size());
                }
                for (Integer eachd : onedList) {
                    lew.writeInt(eachd.intValue());
                }
            }
        }
    }

	public static byte[] getMonsterDamagePacket(int monster, int amount) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(monster);
        mplew.write(0);
        mplew.writeInt(amount);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
	}

	public static byte[] getShowMonsterHp(int objectId, int remainingPercent) {
		MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SHOW_MONSTER_HP.getValue());
        mplew.writeInt(objectId);
        mplew.write(remainingPercent);
        return mplew.getPacket();
	}
	
    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @return The meso gain packet.
     */
    public static byte[] getShowMesoGain(int gain) {
        return getShowMesoGain(gain, false);
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @param inChat Show in the chat window?
     * @return The meso gain packet.
     */
    public static byte[] getShowMesoGain(int gain, boolean inChat) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.writeShort(1); //v83
        } else {
            mplew.write(5);
        }
        mplew.writeInt(gain);
        mplew.writeShort(0);
        return mplew.getPacket();
    }
	
	public static byte[] getShowExpGain(int gain, int bonus, int equip, boolean inChat, boolean white) {
		return getShowSidebarInfo(gain, bonus, 3, equip, inChat, white);
	}
	
	/**
     * Gets a packet telling the client to show an EXP increase.
     *
     * @param gain The amount of EXP gained.
     * @param inChat In the chat box?
     * @param white White text or yellow?
     * @return The exp gained packet.
     */
    private static byte[] getShowSidebarInfo(int gain, int bonus, int type, int equip, boolean inChat, boolean white) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(type); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.writeBool(white);
        mplew.writeInt(gain);
        mplew.writeBool(inChat);
        mplew.writeInt(0); // monster book bonus (Bonus Event Exp)
        mplew.writeShort(0); //Weird stuff
        mplew.writeInt(0); //wedding bonus
        mplew.write(0); //0 = party bonus, 1 = Bonus Event party Exp () x0
        mplew.writeInt(bonus); // party bonus
        mplew.writeInt(equip); //equip bonus
        mplew.writeInt(0); //Internet Cafe Bonus
        mplew.writeInt(0); //Rainbow Week Bonus
        if (inChat) {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

	public static byte[] getDropItemPacket(MapleMapItem item, Point to, Point from, int mod) {
		MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod);
        mplew.writeInt(item.getObjectId());
        mplew.writeBool(item.isMesoDrop()); // 1 mesos, 0 item, 2 and above all item meso bag,
        mplew.writeInt(item.isMesoDrop() ? item.getItemId() / MapleStory.getServerConfig().getMesoRate() : item.getItemId()); // drop object ID
        mplew.writeInt(item.getOwner()); // owner charid/paryid :)
        mplew.write(item.getDropType().getId()); // 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
        mplew.writePos(to);
        mplew.writeInt(item.getDropType() == DropType.OWNER_ONLY ? item.getOwner() : 0); //test

        if (mod != 2) {
            mplew.writePos(from);
            mplew.writeShort(0);//Fh?
        }
        if (item.getMesos() == 0) {
        	if(item.getItem() instanceof CashItem){
        		addExpirationTime(mplew, ((CashItem)item.getItem()).getExpirationDate());
        	}else{
        		addExpirationTime(mplew, 0);
        	}            
        }
        mplew.write(item.isPlayerDrop() ? 0 : 1); //pet EQP pickup
        return mplew.getPacket();
	}
	
    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/> 4 -
     * explode<br/> cid is ignored for 0 and 1.<br /><br />Flagging pet as true
     * will make a pet pick up the item.
     *
     * @param oid
     * @param animation
     * @param cid
     * @param pet
     * @param petslot
     * @return
     */
    private static byte[] getRemoveItemPacket(MapleMapItem item, int animation, int cid, boolean pet, int petslot) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation); // expire
        mplew.writeInt(item.getObjectId());
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (pet) {
                mplew.write(petslot);
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] getDeleteDroppedItemPacket(MapleMapItem item){
    	return getRemoveItemPacket(item, 0, 0, false, 0);
    }
    
    public static byte[] getPickupDroppedItemPacket(MapleMapItem item, MapleCharacter who){
    	return getRemoveItemPacket(item, 2, who.getId(), false, 0);
    }
    
    public static byte[] getPickupDroppedItemPacket(MapleMapItem item, MapleCharacter who, MaplePetInstance pet){
    	return getRemoveItemPacket(item, 5, who.getId(), true, pet.getOwner().getPetSlot(pet.getSource().getItemId()));
    }
    
    public static byte[] getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

	public static byte[] getShowInventoryStatus(int mode) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(0);
		mplew.write(mode);
		mplew.writeInt(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] getInventoryNoOp(){
		return getInventoryOperationPacket(true, Collections.emptyList());
	}
	
	public static byte[] getInventoryOperationPacket(boolean updateTick, List<InventoryOperation> ops){
		MaplePacketWriter buf = new MaplePacketWriter();
		buf.writeShort(SendOpcode.INVENTORY_OPERATION.getValue());
		buf.writeBool(updateTick);//see http://forum.ragezone.com/f566/updatetick-modifyinventory-packet-1104793/
		buf.write(ops.size());
		
		int addMovement = -1;
		
		for(InventoryOperation op : ops){
			
			buf.write(op.getMode().getId());
			buf.write(InventoryType.getByItemId(op.getItem().getItemId()).getId());
			buf.writeShort(op.getMode() == OperationType.MOVE_ITEM ? op.getOldPosition() : op.getPosition());
			
			if(op.getMode() == OperationType.ADD_ITEM){
				buf.writeItemInfo(op.getItem());
			}else if(op.getMode() == OperationType.UPDATE_QUANTITY){
				buf.writeShort(op.getItem().getAmount());
			}else if(op.getMode() == OperationType.MOVE_ITEM){
				buf.writeShort(op.getPosition());
				if(op.getPosition() < 0 || op.getOldPosition() < 0){
					addMovement = op.getOldPosition() < 0 ? 1 : 2;
				}
			}else if(op.getMode() == OperationType.REMOVE_ITEM){
				if(op.getPosition() < 0){
					addMovement = 2;
				}
			}
			
		}
		
		if(addMovement > -1){
			buf.write(addMovement);
		}
		
		return buf.getPacket();
	}

	public static byte[] getServerMessagePacket(MessageType type, String text, int channel, boolean megaEar) {
		if(type == MessageType.YELLOW_TEXT){
			return yellowTip(text);
		}else if(type == MessageType.YELLOW_POPUP){
			return yellowPopupTip(text);
		}
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
        mplew.write(type.getId());
        if (type == MessageType.SCROLLING_TEXT) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(text);
        if (type == MessageType.SUPER_MEGAPHONE) {
            mplew.write(channel); // channel
            mplew.writeBool(megaEar);
        } else if (type == MessageType.LIGHT_BLUE_TEXT) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
	}

	    /**
     * Sends a Avatar Super Megaphone packet.
     *
     * @param chr The character name.
     * @param medal The medal text.
     * @param channel Which channel.
     * @param itemId Which item used.
     * @param message The message sent.
     * @param ear Whether or not the ear is shown for whisper.
     * @return
     */
    public static byte[] getSuperMegaphoneAvatar(MapleCharacter chr, String medal, int channel, SmegaType type, List<String> message, boolean ear) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SET_AVATAR_MEGAPHONE.getValue());
        mplew.writeInt(type.getItemId());
        mplew.writeMapleAsciiString(medal + chr.getName());
        for (String s : message) {
            mplew.writeMapleAsciiString(s);
        }
        mplew.writeInt(channel); // channel
        mplew.writeBool(ear);
        addCharLook(mplew, chr, true);
        return mplew.getPacket();
    }

    public static byte[] getUpdateSkillPacket(int skillid, int level, int masterlevel, long expiration) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        addExpirationTime(mplew, expiration);
        mplew.write(4);
        return mplew.getPacket();
    }

	public static byte[] showOwnBuffEffect(int skillid, int effectid) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FIELD_UPDATE.getValue());
		mplew.write(effectid);
		mplew.writeInt(skillid);
		mplew.write(0xA9);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] showBuffEffect(int cid, int skillid, int effectid) {
		return showBuffEffect(cid, skillid, effectid, (byte) 3);
	}

	public static byte[] showBuffEffect(int cid, int skillid, int effectid,
			byte direction) {
		MaplePacketWriter mplew = new MaplePacketWriter(12);
		mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
		mplew.writeInt(cid);
		mplew.write(effectid); // buff level
		mplew.writeInt(skillid);
		mplew.write(direction);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] getUpdatePlayerLookPacket(MapleCharacter chr) {
		MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        addCharLook(mplew, chr, false);
        addRingLook(mplew, chr, true);
        addRingLook(mplew, chr, false);
        addMarriageRingLook(mplew, chr);
        mplew.writeInt(0);
        return mplew.getPacket();
	}

    /**
     * Possible values for
     * <code>speaker</code>:<br> 0: Npc talking (left)<br> 1: Npc talking
     * (right)<br> 2: Player talking (left)<br> 3: Player talking (left)<br>
     *
     * @param npc Npcid
     * @param msgType
     * @param talk
     * @param endBytes
     * @param speaker
     * @return
     */
    public static byte[] getNPCTalk(MapleNPC npc, byte msgType, String talk, byte[] endBytes, byte speaker) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc.getId());
        mplew.write(msgType);
        mplew.write(speaker);
        mplew.writeMapleAsciiString(talk);
        mplew.write(endBytes);
        return mplew.getPacket();
    }
    
    /**
     * Makes any NPC in the game scriptable.
     * @param npcId - The NPC's ID, found in WZ files/MCDB
     * @param description - If the NPC has quests, this will be the text of the menu item
     * @return 
     */
    public static byte[] setNPCScriptable(int npcId, String description) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.getValue());
        mplew.write(1); // following structure is repeated n times
        mplew.writeInt(npcId);
        mplew.writeMapleAsciiString(description);
        mplew.writeInt(0); // start time
        mplew.writeInt(Integer.MAX_VALUE); // end time
        return mplew.getPacket();
    }

	public static byte[] getShowMagnetEffect(int mobId, byte success) {
		MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobId);
        mplew.write(success);
        mplew.skip(10); //Mmmk
        return mplew.getPacket();
	}
	
    public static byte[] getShowBuffEffect(int cid, int skillid, int effectid) {
        return getShowBuffEffect(cid, skillid, effectid, (byte) 3);
    }

    public static byte[] getShowBuffEffect(int cid, int skillid, int effectid, byte direction) {
        MaplePacketWriter mplew = new MaplePacketWriter(12);
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effectid); //buff level
        mplew.writeInt(skillid);
        mplew.write(direction);
        mplew.write(1);
        return mplew.getPacket();
    }
    
    public static byte[] getShowForeignEffect(int cid, int effect){
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        
        return mplew.getPacket();
    }

	public static byte[] getSpawnSummonPacket(MapleSummon summon, boolean animated) {
		MaplePacketWriter mplew = new MaplePacketWriter(25);
		mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
		mplew.writeInt(summon.getOwner().getId());
		mplew.writeInt(summon.getObjectId());
		mplew.writeInt(summon.getSkill());
		mplew.write(0x0A); //v83
		mplew.write(summon.getSkillLevel());
		mplew.writePos(summon.getPosition());
		mplew.skip(3);
		mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 = follow (4th mage summons?), 2/4 = only tele follow, 3 = bird follow
		mplew.write(summon.isPuppet() ? 0 : 1); // 0 and the summon can't attack - but puppets don't attack with 1 either ^.-
		mplew.write(animated ? 0 : 1);
		return mplew.getPacket();
	}

	public static byte[] getDestroySummonPacket(MapleSummon summon, boolean animated) {
		MaplePacketWriter mplew = new MaplePacketWriter(11);
		mplew.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
		mplew.writeInt(summon.getOwner().getId());
		mplew.writeInt(summon.getObjectId());
		mplew.write(animated ? 4 : 1); // ?
		return mplew.getPacket();
	}

	public static byte[] getSkillCooldown(int sid, int time) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.COOLDOWN.getValue());
		mplew.writeInt(sid);
		mplew.writeShort(time);// Int in v97
		return mplew.getPacket();
	}
	
	 /**
	  * It is important that statups is in the correct order (see decleration
	  * order in MapleBuffStat) since this method doesn't do automagical
	  * reordering.
	  *
	  * @param buffid
	  * @param bufflength
	  * @param statups
	  * @return
	  */
	 //1F 00 00 00 00 00 03 00 00 40 00 00 00 E0 00 00 00 00 00 00 00 00 E0 01 8E AA 4F 00 00 C2 EB 0B E0 01 8E AA 4F 00 00 C2 EB 0B 0C 00 8E AA 4F 00 00 C2 EB 0B 44 02 8E AA 4F 00 00 C2 EB 0B 44 02 8E AA 4F 00 00 C2 EB 0B 00 00 E0 7A 1D 00 8E AA 4F 00 00 00 00 00 00 00 00 03
	 public static byte[] getGiveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups) {
		 MaplePacketWriter mplew = new MaplePacketWriter();
		 mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		 boolean special = false;
		 writeLongMask(mplew, statups);
		 for (Pair<MapleBuffStat, Integer> statup : statups) {
			 if (statup.getLeft().equals(MapleBuffStat.MONSTER_RIDING) || statup.getLeft().equals(MapleBuffStat.HOMING_BEACON)) {
				 special = true;
			 }
			 mplew.writeShort(statup.getRight().shortValue());
			 mplew.writeInt(buffid);
			 mplew.writeInt(bufflength);
		 }
		 mplew.writeInt(0);
		 mplew.write(0);
		 mplew.writeInt(statups.get(0).getRight()); //Homing beacon ...

		 if (special) {
			 mplew.skip(3);
		 }
		 return mplew.getPacket();
	 }

	public static byte[] getUpdateMount(int ownerId, MapleMount mount, boolean levelup) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SET_TAMING_MOB_INFO.getValue());
		mplew.writeInt(ownerId);
		mplew.writeInt(mount.getLevel());
		mplew.writeInt(mount.getExp());
		mplew.writeInt(mount.getTiredness());
		mplew.write(levelup ? (byte) 1 : (byte) 0);
		return mplew.getPacket();
	}

	public static byte[] getPirateBuff(List<Pair<MapleBuffStat, Integer>> statups, int buffid, int duration) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		boolean infusion = buffid == Buccaneer.SPEED_INFUSION
				|| buffid == ThunderBreaker.SPEED_INFUSION
				|| buffid == Corsair.SPEED_INFUSION;
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		writeLongMask(mplew, statups);
		mplew.writeShort(0);
		for (Pair<MapleBuffStat, Integer> stat : statups) {
			mplew.writeInt(stat.getRight().shortValue());
			mplew.writeInt(buffid);
			mplew.skip(infusion ? 10 : 5);
			mplew.writeShort(duration);
		}
		mplew.skip(3);
		return mplew.getPacket();
	}

	public static byte[] getForeignDash(int cid, int buffid, int time, List<Pair<MapleBuffStat, Integer>> statups) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		boolean infusion = buffid == Buccaneer.SPEED_INFUSION
				|| buffid == ThunderBreaker.SPEED_INFUSION
				|| buffid == Corsair.SPEED_INFUSION;
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		writeLongMask(mplew, statups);
		mplew.writeShort(0);
		for (Pair<MapleBuffStat, Integer> statup : statups) {
			mplew.writeInt(statup.getRight().shortValue());
			mplew.writeInt(buffid);
			mplew.skip(infusion ? 10 : 5);
			mplew.writeShort(time);
		}
		mplew.writeShort(0);
		mplew.write(2);
		return mplew.getPacket();
	}

	public static byte[] getShowMonsterRiding(int cid, MapleMount mount) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		mplew.writeLong(MapleBuffStat.MONSTER_RIDING.getValue()); // Thanks?
		mplew.writeLong(0);
		mplew.writeShort(0);
		mplew.writeInt(mount.getItemId());
		mplew.writeInt(mount.getSkillId());
		mplew.writeInt(0); // Server Tick value.
		mplew.writeShort(0);
		mplew.write(0); // Times you have been buffed
		return mplew.getPacket();
	}

	public static byte[] giveForeignBuff(int cid, List<Pair<MapleBuffStat, Integer>> statups) {
		 MaplePacketWriter mplew = new MaplePacketWriter();
		 mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		 mplew.writeInt(cid);
		 writeLongMask(mplew, statups);
		 for (Pair<MapleBuffStat, Integer> statup : statups) {
			 mplew.writeShort(statup.getRight().shortValue());
		 }
		 mplew.writeInt(0);
		 mplew.writeShort(0);
		 return mplew.getPacket();
	 }

    public static byte[] giveForeignInfusion(int cid, int speed, int duration) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(MapleBuffStat.SPEED_INFUSION.getValue());
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(speed);
        mplew.writeInt(5121009);
        mplew.writeLong(0);
        mplew.writeInt(duration);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

	public static byte[] skillCancel(MapleCharacter from, int skillId) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.CANCEL_SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);
		return mplew.getPacket();
	}

	public static byte[] moveSummon(int cid, int oid, Point startPos, MovementPath path) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.MOVE_SUMMON.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(oid);
		mplew.writePos(startPos);
		mplew.writeMovementPath(path);
		return mplew.getPacket();
	}

	public static byte[] summonAttack(int cid, int skill, byte direction, List<SummonAttack> attacks) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(skill);
		mplew.write(direction);
		mplew.write(4);
		mplew.write(attacks.size());
		for (SummonAttack attackEntry : attacks) {
			mplew.writeInt(attackEntry.getMonsterOid()); // oid
			mplew.write(6); // who knows
			mplew.writeInt(attackEntry.getDamage()); // damage
		}
		return mplew.getPacket();
	}

	public static byte[] facialExpression(MapleCharacter mapleCharacter, MapleEmote emote) {
		MaplePacketWriter mplew = new MaplePacketWriter(10);
		mplew.writeShort(SendOpcode.FACIAL_EXPRESSION.getValue());
		mplew.writeInt(mapleCharacter.getId());
		mplew.writeInt(emote.getId());
		return mplew.getPacket();
	}
	
	public static byte[] musicChange(Song song) {
		return musicChange(song.getId());
	}
	
	public static byte[] musicChange(String song){
		return environmentChange(song, 6);
	}

	public static byte[] showEffect(String effect) {
		return environmentChange(effect, 3);
	}

	public static byte[] playSound(String sound) {
		return environmentChange(sound, 4);
	}

	public static byte[] environmentChange(String env, int mode) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(env);
		return mplew.getPacket();
	}
	
	 private static void writeLongMaskFromList(MaplePacketWriter mplew, List<MapleBuffStat> statups) {
		 long firstmask = 0;
		 long secondmask = 0;
		 for (MapleBuffStat statup : statups) {
			 if (statup.isFirst()) {
				 firstmask |= statup.getValue();
			 } else {
				 secondmask |= statup.getValue();
			 }
		 }
		 mplew.writeLong(firstmask);
		 mplew.writeLong(secondmask);
	 }

	public static byte[] cancelBuff(List<MapleBuffStat> buffstats) {
		 MaplePacketWriter mplew = new MaplePacketWriter();
		 mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
		 writeLongMaskFromList(mplew, buffstats);
		 mplew.write(1);//?
		 return mplew.getPacket();
	}

	public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
		 MaplePacketWriter mplew = new MaplePacketWriter();
		 mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
		 mplew.writeInt(cid);
		 writeLongMaskFromList(mplew, statups);
		 return mplew.getPacket();
	}

	public static byte[] skillEffect(MapleCharacter character, int skillId, int level, int flags, int speed, int direction) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SKILL_EFFECT.getValue());
		mplew.writeInt(character.getId());
		mplew.writeInt(skillId);
		mplew.write(level);
		mplew.write(flags);
		mplew.write(speed);
		mplew.write(direction); // Mmmk
		return mplew.getPacket();
	}

	public static byte[] spawnPortal(int townId, int targetId, Point pos) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
		mplew.writeInt(townId);
		mplew.writeInt(targetId);
		if (pos != null) {
			mplew.writePos(pos);
		}
		return mplew.getPacket();
	}
	
	public static byte[] spawnDoor(int oid, Point pos, boolean town) {
		MaplePacketWriter mplew = new MaplePacketWriter(11);
		mplew.writeShort(SendOpcode.SPAWN_DOOR.getValue());
		mplew.writeBool(town);
		mplew.writeInt(oid);
		mplew.writePos(pos);
		return mplew.getPacket();
	}
	

	public static byte[] spawnPartyPortal(int townId, int targetId, Point pos) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		mplew.writeShort(0x23);
		mplew.writeInt(townId);
		mplew.writeInt(targetId);
		mplew.writePos(pos);
		return mplew.getPacket();
	}

	public static byte[] removeDoor(int oid, boolean town) {
		MaplePacketWriter mplew = new MaplePacketWriter(10);
		if (town) {
			mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
			mplew.writeInt(999999999);
			mplew.writeInt(999999999);
		} else {
			mplew.writeShort(SendOpcode.REMOVE_DOOR.getValue());
			mplew.write(0);
			mplew.writeInt(oid);
		}
		return mplew.getPacket();
	}

	public static byte[] removeMist(int objectId) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.REMOVE_MIST.getValue());
		mplew.writeInt(objectId);
		return mplew.getPacket();
	}

	public static byte[] spawnMist(int oid, int ownerCid, int skill, int level, MapleMist mist) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SPAWN_MIST.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(mist.isMobMist() ? 0 : mist.isPoisonMist() ? 1 : 2); // mob mist = 0, player poison = 1,
											// smokescreen = 2, unknown = 3,
											// recovery = 4
		mplew.writeInt(ownerCid);
		mplew.writeInt(skill);
		mplew.write(level);
		mplew.writeShort(mist.getSkillDelay()); // Skill delay
		mplew.writeInt(mist.getBox().x);
		mplew.writeInt(mist.getBox().y);
		mplew.writeInt(mist.getBox().x + mist.getBox().width);
		mplew.writeInt(mist.getBox().y + mist.getBox().height);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] openCharInfo(MapleCharacter chr) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.CHAR_INFO.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getLevel());
		mplew.writeShort(chr.getJob().getId());
		mplew.writeShort(chr.getFame());
		mplew.write(/*chr.getMarriageRing() != null ? 1 : */0);
		MapleGuild guild = chr.getGuild();
		String guildName = guild != null ? guild.getName() : "";
		String allianceName = "";
		/*MapleGuildSummary gs = chr.getClient().getWorldServer()
				.getGuildSummary(chr.getGuildId(), chr.getWorld());
		if (chr.getGuildId() > 0 && gs != null) {
			guildName = gs.getName();
			MapleAlliance alliance = Server.getInstance().getAlliance(
					gs.getAllianceId());
			if (alliance != null) {
				allianceName = alliance.getName();
			}
		}*/
		mplew.writeMapleAsciiString(guildName);
		mplew.writeMapleAsciiString(allianceName);
		mplew.write(0);
		MaplePetInstance[] pets = chr.getPets();
		Item inv = chr.getInventory(InventoryType.EQUIPPED).getItem(
				(short) -114);
		for (int i = 0; i < 3; i++) {
			if (pets[i] != null) {
				mplew.write((int)pets[i].getSource().getUniqueId());
				mplew.writeInt(pets[i].getSource().getItemId()); // petid
				mplew.writeMapleAsciiString(pets[i].getSource().getPetName());
				mplew.write(pets[i].getSource().getPetLevel()); // pet level
				mplew.writeShort(pets[i].getSource().getCloseness()); // pet closeness
				mplew.write(pets[i].getSource().getFullness()); // pet fullness
				mplew.writeShort(0);
				mplew.writeInt(inv != null ? inv.getItemId() : 0);
			}
		}
		mplew.write(0); // end of pets
		if (chr.getMount() != null && chr.getInventory(InventoryType.EQUIPPED).getItem(-18) != null) {
			mplew.write(chr.getMount().getItemId()); // mount
			mplew.writeInt(chr.getMount().getLevel()); // level
			mplew.writeInt(chr.getMount().getExp()); // exp
			mplew.writeInt(chr.getMount().getTiredness()); // tiredness
		} else {
			mplew.write(0);
		}
		mplew.write(/*chr.getCashShop().getWishList().size()*/0);
		/*for (int sn : chr.getCashShop().getWishList()) {
			mplew.writeInt(sn);
		}*/
		mplew.writeInt(chr.getMonsterBook().getBookLevel());
		mplew.writeInt(chr.getMonsterBook().getNormalCards());
		mplew.writeInt(chr.getMonsterBook().getSpecialCards());
		mplew.writeInt(chr.getMonsterBook().getTotalCards());
		mplew.writeInt(0);//Book cover
		Item medal = chr.getInventory(InventoryType.EQUIPPED).getItem((byte) -49);
		if (medal != null) {
			mplew.writeInt(medal.getItemId());
		} else {
			mplew.writeInt(0);
		}
		ArrayList<Integer> medalQuests = new ArrayList<>();
		Collection<MapleQuestInstance> completed = chr.getQuests(MapleQuestStatus.COMPLETED);
		for (MapleQuestInstance q : completed) {
			if (q.getQuest().getId() >= 29000) { // && q.getQuest().getId() <=
													// 29923
				medalQuests.add(q.getQuest().getId());
			}
		}
		
		Collections.sort(medalQuests);
		mplew.writeShort(medalQuests.size());
		for (Integer s : medalQuests) {
			mplew.writeShort(s);
		}
		return mplew.getPacket();
	}

	public static byte[] updateMount(int cid, MapleMount mount, boolean levelup) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SET_TAMING_MOB_INFO.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(mount.getLevel());
		mplew.writeInt(mount.getExp());
		mplew.writeInt(mount.getTiredness());
		mplew.write(levelup ? (byte) 1 : (byte) 0);
		return mplew.getPacket();
	}

	public static byte[] cancelMonsterStatus(int objectId, List<Pair<MonsterStatus, Integer>> statusChanges) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.CANCEL_MONSTER_STATUS.getValue());
        mplew.writeInt(objectId);
        mplew.writeLong(0);
        mplew.writeInt(0);
        int mask = 0;
        for (Pair<MonsterStatus, Integer> stat : statusChanges) {
            mask |= stat.getLeft().getValue();
        }
        mplew.writeInt(mask);
        mplew.writeInt(0);
        return mplew.getPacket();
	}

	public static byte[] applyMonsterStatus(int objectId, MonsterStatusEffect effect) {
		return applyMonsterStatus(objectId, effect, null);
	}
	
    private static void writeIntMask(MaplePacketWriter mplew, List<Pair<MonsterStatus, Integer>> list) {
        int firstmask = 0;
        int secondmask = 0;
        for (Pair<MonsterStatus, Integer> stat : list) {
            /*if (stat.isFirst()) {
                firstmask |= stat.getValue();
            } else {
                secondmask |= stat.getValue();
            }*/
        	secondmask |= stat.getLeft().getValue();
        }
        mplew.writeInt(firstmask);
        mplew.writeInt(secondmask);
    }
	
	public static byte[] applyMonsterStatus(int oid, MonsterStatusEffect mse, List<Integer> reflection) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.APPLY_MONSTER_STATUS.getValue());
		mplew.writeInt(oid);
		mplew.writeLong(0);
		writeIntMask(mplew, mse.getStatusChanges());
		for (Pair<MonsterStatus, Integer> stat : mse.getStatusChanges()) {
			mplew.writeShort(stat.getRight());
			if (mse.isMonsterSkill()) {
				mplew.writeShort(mse.getMobSkill().getSkillId());
				mplew.writeShort(mse.getMobSkill().getLevel());
			} else {
				mplew.writeInt(mse.getSource().getId());
			}
			mplew.writeShort(-1); // might actually be the buffTime but it's not
									// displayed anywhere
		}

		int size = mse.getStatusChanges().size(); // size
		if (reflection != null) {
			for (Integer ref : reflection) {
				mplew.writeInt(ref);
			}
			if (reflection.size() > 0) {
				size /= 2; // This gives 2 buffs per reflection but it's really
							// one buff
			}
		}
		mplew.write(size); // size
		mplew.writeInt(0);
		return mplew.getPacket();
	}
	
	/*
	 * public static byte[] showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
		   final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		   mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		   mplew.write(5);
		   mplew.writeInt(oid);
		   mplew.writeInt(currHP);
		   mplew.writeInt(maxHP);
		   mplew.write(tagColor);
		   mplew.write(tagBgColor);
		   return mplew.getPacket();
	   }
	 */

	public static byte[] createBossHpBar(MapleMonster monster) {
		if(!monster.getStats().isBoss()){
			throw new IllegalArgumentException("Monster must be a boss! "+monster);
		}
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		mplew.write(5);
		mplew.writeInt(monster.getObjectId());
		mplew.writeInt(monster.getHp());
		mplew.writeInt(monster.getMaxHp());
		mplew.write(monster.getStats().getTagColor());
		mplew.write(monster.getStats().getTagBgColor());
		return mplew.getPacket();
	}

	public static byte[] skillCooldown(int sid, int time) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.COOLDOWN.getValue());
		mplew.writeInt(sid);
		mplew.writeShort(time);// Int in v97
		return mplew.getPacket();
	}

	public static byte[] getNPCTalkNum(MapleNPC npc, String talk, int def, int min, int max) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?
		mplew.writeInt(npc.getId());
		mplew.write(3);
		mplew.write(0); // speaker
		mplew.writeMapleAsciiString(talk);
		mplew.writeInt(def);
		mplew.writeInt(min);
		mplew.writeInt(max);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] getNPCTalkText(MapleNPC npc, String talk, String def) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // Doesn't matter
		mplew.writeInt(npc.getId());
		mplew.write(2);
		mplew.write(0); // speaker
		mplew.writeMapleAsciiString(talk);
		mplew.writeMapleAsciiString(def);// :D
		mplew.writeInt(0);
		return mplew.getPacket();
	}
    
	
	public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.DAMAGE_SUMMON.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(unkByte);
		mplew.writeInt(damage);
		mplew.writeInt(monsterIdFrom);
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * status can be: <br>
	 * 0: ok, use giveFameResponse<br>
	 * 1: the username is incorrectly entered<br>
	 * 2: users under level 15 are unable to toggle with fame.<br>
	 * 3: can't raise or drop fame anymore today.<br>
	 * 4: can't raise or drop fame for this character for this month anymore.<br>
	 * 5: received fame, use receiveFame()<br>
	 * 6: level of fame neither has been raised nor dropped due to an unexpected
	 * error
	 *
	 * @param status
	 * @return
	 */
	public static byte[] fameErrorResponse(int status) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(status);
		return mplew.getPacket();
	}

	public static byte[] receiveFame(byte mode, String name) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(5);
		mplew.writeMapleAsciiString(name);
		mplew.write(mode);
		return mplew.getPacket();
	}
	
	public static byte[] giveFameResponse(int mode, String name, int newfame) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(0);
		mplew.writeMapleAsciiString(name);
		mplew.write(mode);
		mplew.writeShort(newfame);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	public static byte[] showAllCharacters(int numCharacters, int unk) {
        MaplePacketWriter mplew = new MaplePacketWriter(11);
        mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.getValue());
        mplew.write(1);
        mplew.writeInt(numCharacters);
        mplew.writeInt(unk);
        return mplew.getPacket();
	}

    public static byte[] showAllCharacterInfo(int worldid, List<MapleCharacter> chars) {
        MaplePacketWriter mplew = new MaplePacketWriter();
        mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.getValue());
        mplew.write(0);
        mplew.write(worldid);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, true);
        }
        return mplew.getPacket();
    }

	public static byte[] cancelDebuff(MapleDisease disease) {
		 MaplePacketWriter mplew = new MaplePacketWriter(19);
		 mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
		 mplew.writeLong(0);
		 mplew.writeLong(disease.getValue());
		 mplew.write(0);
		 return mplew.getPacket();
	}

	public static byte[] cancelForeignDebuff(int cid, MapleDisease disease) {
		 MaplePacketWriter mplew = new MaplePacketWriter();
		 mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
		 mplew.writeInt(cid);
		 mplew.writeLong(0);
		 mplew.writeLong(disease.getValue());
		 return mplew.getPacket();
	}
	
	 private static void writeLongMaskD(MaplePacketWriter mplew, List<Pair<MapleDisease, Integer>> statups) {
		 long firstmask = 0;
		 long secondmask = 0;
		 for (Pair<MapleDisease, Integer> statup : statups) {
			 secondmask |= statup.getLeft().getValue();
		 }
		 mplew.writeLong(firstmask);
		 mplew.writeLong(secondmask);
	 }

	public static byte[] giveDebuff(MapleDisease disease, MobSkill skill) {
		
		List<Pair<MapleDisease, Integer>> statups = Collections.singletonList(new Pair<>(disease, Integer.valueOf(skill.getX())));

		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		writeLongMaskD(mplew, statups);
		for (Pair<MapleDisease, Integer> statup : statups) {
			mplew.writeShort(statup.getRight().shortValue());
			mplew.writeShort(skill.getSkillId());
			mplew.writeShort(skill.getLevel());
			mplew.writeInt((int) skill.getDuration());
		}
		mplew.writeShort(0); // ??? wk charges have 600 here o.o
		mplew.writeShort(900);// Delay
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] giveForeignDebuff(int cid, MapleDisease disease, MobSkill skill) {
		List<Pair<MapleDisease, Integer>> statups = Collections.singletonList(new Pair<>(disease, Integer.valueOf(skill.getX())));
		MaplePacketWriter mplew = new MaplePacketWriter();
		 mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		 mplew.writeInt(cid);
		 writeLongMaskD(mplew, statups);
		 for (int i = 0; i < statups.size(); i++) {
			 mplew.writeShort(skill.getSkillId());
			 mplew.writeShort(skill.getLevel());
		 }
		 mplew.writeShort(0); // same as give_buff
		 mplew.writeShort(900);//Delay
		 return mplew.getPacket();
	}

	public static byte[] skillBookSuccess(MapleCharacter chr, Skill skill, int maxlevel, boolean newSkill, boolean useable, boolean success) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SKILL_LEARN_ITEM_RESULT.getValue());
		mplew.writeInt(chr.getId());
		mplew.writeBool(!newSkill);
		mplew.writeInt(skill.getId());
		mplew.writeInt(maxlevel);
		mplew.writeBool(useable);
		mplew.writeBool(success);
		return mplew.getPacket();
	}
	
	public static byte[] sendHint(String hint, int width, int height) {
		if (width < 1) {
			width = hint.length() * 10;
			if (width < 40) {
				width = 40;
			}
		}
		if (height < 5) {
			height = 5;
		}
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.PLAYER_HINT.getValue());
		mplew.writeMapleAsciiString(hint);
		mplew.writeShort(width);
		mplew.writeShort(height);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] guideHint(int id, int timeMs) {
		MaplePacketWriter mplew = new MaplePacketWriter(11);
		mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
		mplew.write(1);
		mplew.writeInt(id);
		mplew.writeInt(timeMs);
		return mplew.getPacket();
	}
	
	public static byte[] guideTalk(String msg) {
		MaplePacketWriter mplew = new MaplePacketWriter(11);
		mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
		mplew.write(0);
		mplew.writeMapleAsciiString(msg);
		mplew.write(new byte[]{(byte) 0xC8, 0, 0, 0, (byte) 0xA0, (byte) 0x0F, 0, 0});
		return mplew.getPacket();
	}

	public static byte[] spawnGuide(boolean spawn) {
		MaplePacketWriter writer = new MaplePacketWriter(3);
		writer.writeShort(SendOpcode.SPAWN_GUIDE.getValue());
		writer.writeBool(spawn);
		return writer.getPacket();
	}

	public static byte[] showInfo(String path) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FIELD_UPDATE.getValue());
		mplew.write(0x17);
		mplew.writeMapleAsciiString(path);
		mplew.writeInt(1);
		return mplew.getPacket();
	}

	public static byte[] updateQuest(MapleQuestInstance quest, boolean infoUpdate) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(infoUpdate ? quest.getQuest().getQuestInfo().getInfoNumber() : quest.getQuest().getId());
		if (infoUpdate) {
			mplew.write(1);
			mplew.writeMapleAsciiString(quest.getInfo());
		} else {
			mplew.write(quest.getStatus().getId());
			mplew.writeMapleAsciiString(quest.getQuestData());
		}

		return mplew.getPacket();
	}

	public static byte[] updateQuestInfo(MapleQuestInstance quest) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(8); // 0x0A in v95
		mplew.writeShort(quest.getQuest().getId());
		mplew.writeInt(quest.getNpc());
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] completeQuest(MapleQuestInstance quest) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		mplew.write(1);
		mplew.writeShort(quest.getQuest().getId());
		mplew.write(2);
		mplew.writeLong(getTime(quest.getCompletionTime()));
		return mplew.getPacket();
	}
	
	public static byte[] getShowItemGain(int itemId, int quantity, boolean inChat) {
		final MaplePacketWriter mplew = new MaplePacketWriter();
		if (inChat) {
			mplew.writeShort(SendOpcode.FIELD_UPDATE.getValue());
			mplew.write(3);
			mplew.write(1);
			mplew.writeInt(itemId);
			mplew.writeInt(quantity);
		} else {
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.writeShort(0);
			mplew.writeInt(itemId);
			mplew.writeInt(quantity);
			mplew.writeInt(0);
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}
	
	public static byte[] updateQuestFinish(int quest, int npc, int nextquest) { // Check
		final MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(8);
		mplew.writeShort(quest);
		mplew.writeInt(npc);
		mplew.writeShort(nextquest);
		return mplew.getPacket();
	}
	
	public static byte[] updateQuest(int quest, String status) {
	    MaplePacketWriter mplew = new MaplePacketWriter();
	    mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
	    mplew.write(1);
	    mplew.writeShort(quest);//20022
	    mplew.write(1);
	    mplew.writeAsciiString(status);//"1"
	    return mplew.getPacket();
	}

	/*
	 * MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(items.size()); // item count
        for (MapleShopItem item : items) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
            mplew.writeInt(item.getPrice() == 0 ? item.getPitch() : 0); //Perfect Pitch
            mplew.writeInt(0); //Can be used x minutes after purchase
            mplew.writeInt(0); //Hmm
            if (!ItemConstants.isRechargable(item.getItemId())) {
                mplew.writeShort(1); // stacksize o.o
                mplew.writeShort(item.getBuyable());
            } else {
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.writeShort(doubleToShortBits(ii.getPrice(item.getItemId())));
                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
            }
        }
        return mplew.getPacket();
	 */
	
    private static int doubleToShortBits(double d) {
        return (int) (Double.doubleToLongBits(d) >> 48);
    }
	
	public static byte[] openShop(MapleClient client, MapleShop shop, int npc) {
		MaplePacketWriter buf = new MaplePacketWriter();
		buf.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
		buf.writeInt(npc);
		buf.writeShort(shop.getItems().size());
		for(MapleShopItem item : shop.getItems()){
			buf.writeInt(item.getItemId());
			buf.writeInt(item.getMesoPrice());
			buf.writeInt(item.getPitchPrice());
			buf.writeInt(0);//Can be used x minutes after purchase
			buf.writeInt(0);
			if(!ItemType.RECHARGABLE.isThis(item.getItemId())){
				buf.writeShort(1);//Stack size?
				buf.writeShort(item.getQuantity());
			}else{
				buf.writeShort(0);
				buf.writeInt(0);
				buf.writeShort(doubleToShortBits(ItemInfoProvider.getPrice(item.getItemId())));
				buf.writeShort(client.getCharacter().getMaxSlotForItem(item.getItemId()));
			}
		}
		return buf.getPacket();
	}
	
	/*
	 *     public static byte[] showInfoText(String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }
	 */

	public static byte[] showInfoText(String text) {
		MaplePacketWriter buf = new MaplePacketWriter();
		buf.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		buf.write(9);
		buf.writeMapleAsciiString(text);
		return buf.getPacket();
	}

	/** 00 = /<br>
	 * 01 = You don't have enough in stock<br>
	 * 02 = You do not have enough mesos<br>
	 * 03 = Please check if your inventory is full or not<br>
	 * 05 = You don't have enough in stock<br>
	 * 06 = Due to an error, the trade did not happen<br>
	 * 07 = Due to an error, the trade did not happen<br>
	 * 08 = /<br>
	 * 0D = You need more items<br>
	 * 0E = CRASH; LENGTH NEEDS TO BE LONGER :O<br>
	 */
	
	public static byte[] shopTransactionResult(int code) {
		final MaplePacketWriter mplew = new MaplePacketWriter(3);
		mplew.writeShort(SendOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
		mplew.write(code);
		return mplew.getPacket();
	}
	
	
	public static byte[] partyCreate(MapleCharacter leader){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		writer.write(PartyOperationType.CREATE.getCode());
		writer.write(0x8B);//UNKNOWN
		writer.writeShort(1);//UNKNOWN
		if(leader.getMagicDoors().size() > 0){
			for(MapleMagicDoor door : leader.getMagicDoors()){
				writer.writeInt(door.getTown().getMapId());
				writer.writeInt(door.getTarget().getMapId());
				writer.writeInt(door.getPosition().x);
				writer.writeInt(door.getPosition().y);
			}
		}else{
			writer.writeInt(999999999);
			writer.writeInt(999999999);
			writer.writeInt(0);
			writer.writeInt(0);
		}
		return writer.getPacket();
	}
	
	public static byte[] updatePartyMemberHp(MapleCharacter target){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.UPDATE_PARTYMEMBER_HP.getValue());
		writer.writeInt(target.getId());
		writer.writeInt(target.getHp());
		writer.writeInt(target.getMaxHp());
		return writer.getPacket();
	}
	
	public static byte[] partyStatus(PartyOperationType type){
		if(!type.isMessage()){
			throw new IllegalArgumentException("PartyOperationType must be a message!");
		}
		
		if(type == PartyOperationType.DENY_INVITE){
			throw new IllegalArgumentException("STATUS_MESSAGE_DENY should be used in its appropriate method.");
		}
		
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		writer.write(type.getCode());
		
		return writer.getPacket();
	}
	
	public static byte[] partyDenyInvite(MapleCharacter personWhoDenied){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		writer.write(PartyOperationType.DENY_INVITE.getCode());
		writer.writeMapleAsciiString(personWhoDenied.getName());
		
		return writer.getPacket();
	}
	
	public static byte[] partyInvite(MapleCharacter from){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		writer.write(PartyOperationType.INVITE.getCode());
		writer.writeInt(from.getParty().getPartyId());
		writer.writeMapleAsciiString(from.getName());
		writer.write(0);
		return writer.getPacket();
	}
	
	public static byte[] partyUpdate(int channel, MapleParty party, PartyOperationType op, MapleCharacterSnapshot target){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.PARTY_OPERATION.getValue());
		writer.write(op.getCode());
		
		if(op.isDestroyOperation()){
			writer.writeInt(40546);//Unknown data
			writer.writeInt(target.getId());
			if(op == PartyOperationType.DISBAND){
				writer.write(0);
				writer.writeInt(party.getPartyId());
			}else{
				writer.write(1);
				if(op == PartyOperationType.EXPEL){
					writer.write(1);
				}else{
					writer.write(0);
				}
				writer.writeMapleAsciiString(target.getName());
				writer.writePartyStatus(channel, party);
			}
		}else if(op == PartyOperationType.JOIN){
			writer.writeInt(40546);//Unknown data
			writer.writeMapleAsciiString(target.getName());
			writer.writePartyStatus(channel, party);
		}else if(op == PartyOperationType.SILENT_UPDATE){
			writer.writeInt(party.getPartyId());
			writer.writePartyStatus(channel, party);
		}else if(op ==PartyOperationType.CHANGE_LEADER){
			writer.writeInt(target.getId());
			writer.write(0);
		}
		
		return writer.getPacket();
	}
	
	public static byte[] guildUpdateInfo(MapleCharacter chr, MapleGuild guild){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		
		writer.write(GuildOperationType.SHOW_GUILD_INFO.getCode());
		
		if(chr == null || guild == null){
			writer.write(0);//show empty guild (used for leaving, expelled)
			return writer.getPacket();
		}
		
		writer.writeBool(guild.isMember(chr));
		writer.writeInt(guild.getGuildId());
		writer.writeMapleAsciiString(guild.getName());
		
		for(MapleGuildRankLevel level : MapleGuildRankLevel.getPacketOrder()){
			writer.writeMapleAsciiString(guild.getRank(level).getName());
		}
		
		List<GuildEntry> members = guild.getMembers();
		
		writer.write(members.size());
		for(GuildEntry member : members){
			member.updateSnapshot();
			writer.writeInt(member.getSnapshot().getId());
		}
		
		for(GuildEntry member : members){
			MapleCharacterSnapshot snap = member.getSnapshot();
			writer.writeAsciiString(StringUtil.getRightPaddedStr(snap.getName(), '\0', 13));
			writer.writeInt(snap.getJob());
			writer.writeInt(snap.getLevel());
			MapleGuildRankLevel rank = guild.getRankLevel(member);
			writer.writeInt(rank.getPacketId());
			writer.writeInt(snap.getLiveCharacter().isPresent() ? 1 : 0);
			writer.writeInt(0);//GP Contribution from this player. Doesn't do anything pre-big bang
			writer.writeInt(0 /* Alliance Rank */);
		}
		
		writer.writeInt(guild.getCapacity());
		writer.writeGuildEmblem(guild.getEmblem());
		writer.writeMapleAsciiString(guild.getNotice());
		writer.writeInt(guild.getGuildPoints());
		writer.writeInt(0 /* Alliance Id */);
		
		return writer.getPacket();
	}

	public static byte[] guildUpdateNotice(MapleGuild guild) {
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(GuildOperationType.UPDATE_NOTICE.getCode());
		writer.writeInt(guild.getGuildId());
		writer.writeMapleAsciiString(guild.getNotice());
		
		return writer.getPacket();
	}

	public static byte[] guildInviteError(MapleGuildInviteResponse response){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(response.getCode());
		
		return writer.getPacket();
	}
	
	public static byte[] guildInvite(MapleGuild guild, MapleCharacter from){
		
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(GuildOperationType.INVITE.getCode());
		writer.writeInt(guild.getGuildId());
		writer.writeMapleAsciiString(from.getName());
		
		return writer.getPacket();
	}
	
	public static byte[] guildUpdateRankTitle(MapleGuild guild){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(GuildOperationType.UPDATE_RANK_NAMES.getCode());
		writer.writeInt(guild.getGuildId());
		
		for(MapleGuildRankLevel level : MapleGuildRankLevel.getPacketOrder()){
			writer.writeMapleAsciiString(guild.getRank(level).getName());
		}
		
		return writer.getPacket();
	}

	public static byte[] guildUpdateEmblem(MapleGuild guild) {
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(GuildOperationType.EMBLEM_CHANGE.getCode());
		writer.writeInt(guild.getGuildId());
		writer.writeGuildEmblem(guild.getEmblem());
		
		return writer.getPacket();
	}
	
	public static byte[] guildChangeRank(GuildEntry entry, MapleGuildRankLevel rank){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(GuildOperationType.CHANGE_RANK.getCode());
		writer.writeInt(entry.getGuildId());
		writer.writeInt(entry.getSnapshot().getId());
		writer.write(rank.getPacketId());
		
		return writer.getPacket();
	}
	
	public static byte[] guildUpdateMember(GuildEntry entry){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(GuildOperationType.UPDATE_MEMBER.getCode());
		writer.writeInt(entry.getGuildId());
		writer.writeInt(entry.getSnapshot().getId());
		writer.writeInt(entry.getSnapshot().getLevel());
		writer.writeInt(entry.getSnapshot().getJob());
		
		return writer.getPacket();
	}

	public static byte[] guildMemberLeft(GuildEntry entry, boolean expelled) {
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(GuildOperationType.getLeaveCode(expelled).getCode());
		writer.writeInt(entry.getGuildId());
		writer.writeInt(entry.getSnapshot().getId());
		writer.writeMapleAsciiString(entry.getSnapshot().getName());
		
		return writer.getPacket();
	}

	public static byte[] groupChat(String name, String text, GroupChatType type) {
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.MULTICHAT.getValue());
		writer.write(type.getCode());
		writer.writeMapleAsciiString(name);
		writer.writeMapleAsciiString(text);
		return writer.getPacket();
	}
	
	/*
	 * public static byte[] showCouponRedeemedItem(MapleClient c, Map<Integer, IItem> items, Map<Integer, IItem> items2) { // OnCashItemResUseCouponDone // Use coupon for self 
        PacketWriter pw = new PacketWriter(); 
         
        pw.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue()); 
        pw.writeByte(0x65); 
        pw.writeByte(items.size()); //cash items size 
        for (Entry<Integer, IItem> item : items.entrySet()) { 
            addCashItemInformation(pw, item.getValue(), c.getAccID()); // Size 55
        } 
        pw.writeInt(0); // Maple Points 
        pw.writeInt(items2.size()); // Normal items size 
        for (Entry<Integer, IItem> item : items2.entrySet()) { 
            pw.writeInt(item.getKey().intValue()); // Count 
            pw.writeInt(item.getValue().getItemId());  // Item ID 
        } 
        pw.writeInt(0); // Mesos 

        return pw.getBytes(); 
    } 
	 */
	
	public static byte[] couponShowRedeemedItem(MapleClient client, List<CashItem> cashItems, List<Item> normalItems, int maplePoints, int mesos){
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		out.write(0x49);
		out.write(cashItems.size());
		for(CashItem item : cashItems){
			out.writeCashItemInformation(item, client.getId());
		}
		out.writeInt(maplePoints);
		out.writeInt(normalItems.size());
		for(Item item : normalItems){
			out.writeInt(item.getAmount());
			out.writeInt(item.getItemId());
		}
		out.writeInt(mesos);
		
		return out.getPacket();
	}
	
/*	public static byte[] showCouponRedeemedItem(int itemid) {
		final MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		mplew.writeShort(0x49); // v72
		mplew.writeInt(0);
		mplew.writeInt(1);
		mplew.writeShort(1);
		mplew.writeShort(0x1A);
		mplew.writeInt(itemid);
		mplew.writeInt(0);
		return mplew.getPacket();
	}*/

	public static byte[] openCashshop(MapleCharacter chr) {
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.SET_CASH_SHOP.getValue());
		
		addCharacterInfo(writer, chr);
		
		writer.write(1);
		writer.writeMapleAsciiString(chr.getClient().getUsername());
		writer.skip(4);
		writer.writeShort(0);//Custom item size
		//Custom items here
		//4Bytes SN
		//4Bytes Modifier
		//1Byte Info
		writer.skip(121);
		for (int i = 1; i <= 8; i++) {
			for (int j = 0; j < 2; j++) {
				writer.writeInt(i);
				writer.writeInt(j);
				writer.writeInt(50200004);

				writer.writeInt(i);
				writer.writeInt(j);
				writer.writeInt(50200069);

				writer.writeInt(i);
				writer.writeInt(j);
				writer.writeInt(50200117);

				writer.writeInt(i);
				writer.writeInt(j);
				writer.writeInt(50100008);

				writer.writeInt(i);
				writer.writeInt(j);
				writer.writeInt(50000047);
			}
		}
		
		writer.skip(4);
		writer.skip(2);
		writer.skip(1);
		writer.writeInt(75);
		
		return writer.getPacket();
	}

	public static byte[] updateCashshopCash(CashShopWallet wallet) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.QUERY_CASH_RESULT.getValue());
		
		out.writeInt(wallet.getCash(CashShopCurrency.NX_CASH));
		out.writeInt(wallet.getCash(CashShopCurrency.MAPLE_POINTS));
		out.writeInt(wallet.getCash(CashShopCurrency.PREPAID));
		
		return out.getPacket();
	}
	
	public static byte[] storageOpen(int npc, MapleStorageBox storage) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.STORAGE.getValue());
		
		out.write(StoragePacketType.OPEN.getCode());
		out.writeInt(npc);
		out.write(storage.getSize());
		out.writeShort(0x7E);
		out.writeShort(0);
		out.writeInt(0);
		out.writeInt(storage.getMesos());
		out.writeShort(0);
		out.write(storage.sumOfItems());
		for(Item item : storage.getItems()){
			out.writeItemInfo(item);
		}
		out.writeShort(0);
		out.write(0);
		
		return out.getPacket();
	}
	
	public static byte[] storageUpdateMeso(MapleStorageBox box){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.STORAGE.getValue());
		
		out.write(StoragePacketType.MESO_UPDATE.getCode());
		out.write(box.getSize());
		out.writeShort(2);
		out.writeShort(0);
		out.writeInt(0);
		out.writeInt(box.getMesos());
		
		return out.getPacket();
	}

	public static byte[] storageError(StoragePacketType code) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.STORAGE.getValue());
		out.write(code.getCode());
		return out.getPacket();
	}

	public static byte[] storageAddItem(InventoryType type, MapleStorageBox storage) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.STORAGE.getValue());
		out.write(StoragePacketType.ADD_ITEM.getCode());
		out.write(storage.getSize());
		out.writeShort(2 << type.getId());
		out.writeShort(0);
		out.writeInt(0);
		
		Collection<Item> items = storage.getItems(type);

		out.write(items.size());
		for (Item item : items) {
			out.writeItemInfo(item);
		}
		return out.getPacket();
	}

	public static byte[] storageRemoveItem(InventoryType type, MapleStorageBox storage) {
		  final MaplePacketWriter mplew = new MaplePacketWriter();
		  mplew.writeShort(SendOpcode.STORAGE.getValue());
		  mplew.write(StoragePacketType.TAKE_OUT_ITEM.getCode());
		  mplew.write(storage.getSize());
		  mplew.writeShort(2 << type.getId());
		  mplew.writeShort(0);
		  mplew.writeInt(0);
		  
		  Collection<Item> items = storage.getItems(type);
		  
		  mplew.write(items.size());
		  for (Item item : items) {
			 mplew.writeItemInfo(item);
		  }
		  return mplew.getPacket();
	}

	public static byte[] couponInvalid() {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		out.write(0x5C);
		out.write(0x9F);
		return out.getPacket();
	}

	public static byte[] cashShopItemBought(CashItem item, int accountId) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		out.write(0x57);
		out.writeCashItemInformation(item, accountId);
		
		return out.getPacket();
	}

	public static byte[] cashShopPackageBought(CashShopPackage cashPackage, int accountId){
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		out.write(0x89);
		List<CashShopItemData> items = cashPackage.getItems();
		out.write(items.size());
		
		for(CashShopItemData data : items){
			out.writeCashItemInformation((CashItem) data.createItem(), accountId);
		}
		
		out.skip(2);
		
		return out.getPacket();
	}
	
	public static byte[] createClock(int seconds){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CLOCK.getValue());
		out.write(2);
		out.writeInt(seconds);
		return out.getPacket();
	}
	
	public static byte[] createClock(int hour, int min, int sec){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CLOCK.getValue());
		out.write(1);
		out.write(hour);
		out.write(min);
		out.write(sec);
		return out.getPacket();
	}

	public static byte[] removeClock(){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.STOP_CLOCK.getValue());
		out.write(0);
		return out.getPacket();
	}

	public static byte[] spawnReactor(MapleReactor reactor) {
		MaplePacketWriter mplew = new MaplePacketWriter();
		Point pos = reactor.getPosition();
		mplew.writeShort(SendOpcode.REACTOR_SPAWN.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.writeInt(reactor.getId());
		mplew.write(reactor.getReactorData().getState());
		mplew.writePos(pos);
		mplew.writeShort(0);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] destroyReactor(MapleReactor mapleReactor) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.REACTOR_DESTROY.getValue());
		out.writeInt(mapleReactor.getObjectId());
		out.write(mapleReactor.getReactorData().getState());
		out.writePos(mapleReactor.getPosition());
		return out.getPacket();
	}

	public static byte[] triggerReactor(MapleReactor reactor, int stance) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.REACTOR_HIT.getValue());
		out.writeInt(reactor.getObjectId());
		out.write(reactor.getReactorData().getState());
		out.writePos(reactor.getPosition());
		out.writeShort(stance);
		out.write(0);
		out.write(5); // frame delay, set to 5 since there doesn't appear to
						// be a fixed formula for it
		
		return out.getPacket();
	}

	public static byte[] openDuey(List<DueyParcel> items) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PARCEL.getValue());
		out.write(8);
		
		out.write(0);
		out.write(items.size());
		
		for(DueyParcel dueyPackage : items){
			out.writeInt(dueyPackage.getParcelId());
			out.writeAsciiString(StringUtil.getRightPaddedStr(dueyPackage.getSender(), '\0', 13));
			out.writeInt(dueyPackage.getMesos());
			out.writeLong(getTime(dueyPackage.getExpirationTime()));
			out.writeBool(dueyPackage.hasMessage());
			for(int i = 0; i < 7;i++){
				out.write(1);
			}
			out.writeAsciiString(StringUtil.getRightPaddedStr(dueyPackage.getMessage() == null ? "" : dueyPackage.getMessage(), '\0', 192));
			out.writeInt(0);
			out.write(0);
			if(dueyPackage.getGift() != null){
				out.write(1);
				out.writeItemInfo(dueyPackage.getGift());
			}else{
				out.write(0);
			}
		}
		out.write(0);
		
		return out.getPacket();
	}

	public static byte[] dueyResponse(int response) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PARCEL.getValue());
		out.write(response);
		
		return out.getPacket();
	}

	public static byte[] dueyRemoveItem(boolean isRemoved, int parcelId) {
	
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PARCEL.getValue());
		out.write(0x17);
		
		out.writeInt(parcelId);
		out.write(isRemoved ? 3 : 4);
		
		return out.getPacket();
	}
	
	public static byte[] showSpecialEffect(SpecialEffect effect) {
		final MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FIELD_UPDATE.getValue());
		mplew.write(effect.getPacketId());
		return mplew.getPacket();
	}

	public static byte[] playPortalSound() {
		return showSpecialEffect(SpecialEffect.PORTAL_SOUND);
	}

	public static byte[] mobDamageMobFriendly(MapleMonster mob, int damage) {
		   final MaplePacketWriter mplew = new MaplePacketWriter();
		   mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
		   mplew.writeInt(mob.getObjectId());
		   mplew.write(1); // direction ?
		   mplew.writeInt(damage);
		   mplew.writeInt(mob.getHp());
		   mplew.writeInt(mob.getMaxHp());
		   return mplew.getPacket();
	}

	public static byte[] getNPCTalkStyle(MapleNPC npc, String text, int[] styles) {

		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.NPC_TALK.getValue());
		out.write(4);
		out.writeInt(npc.getId());
		out.write(7);
		out.write(0);
		out.writeMapleAsciiString(text);
		out.write(styles.length);
		for(int i = 0; i < styles.length;i++){
			out.writeInt(styles[i]);
		}
		return out.getPacket();
	}

	public static byte[] showFameGain(int gain){
		MaplePacketWriter out = new MaplePacketWriter(7);
		
		out.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
		out.write(4);
		out.writeInt(gain);
		
		return out.getPacket();
	}
	
	public static byte[] scrollEffect(MapleCharacter character, ScrollResult result, boolean legendarySpirit) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.SHOW_SCROLL_EFFECT.getValue());
		out.writeInt(character.getId());
		if(result.isSuccess()){
			out.writeShort(1);
		}else if(result.isDestroyed()){
			out.write(0);
			out.write(1);
		}else{
			out.writeShort(0);
		}
		
		out.writeShort(legendarySpirit ? 1 : 0);
		
		return out.getPacket();
	}
	

	public static byte[] spawnPet(MapleCharacter mapleCharacter, MaplePetInstance maplePetInstance, int petSlot) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.SPAWN_PET.getValue());
		
		out.writeInt(mapleCharacter.getId());
		out.write(petSlot);
		out.writePetInfo(maplePetInstance, true);
		
		return out.getPacket();
	}

	public static byte[] destroyPet(MapleCharacter mapleCharacter, MaplePetInstance maplePetInstance, int slot, boolean hunger) {
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.SPAWN_PET.getValue());
		
		out.writeInt(mapleCharacter.getId());
		out.write(slot);
		out.write(0);
		out.writeBool(hunger);
		
		return out.getPacket();
	}

	public static byte[] movePet(MapleCharacter chr, MaplePetInstance maplePetInstance, int slot, MovementPath path) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.MOVE_PET.getValue());
		out.writeInt(chr.getId());
		out.write(slot);
		out.writeInt(maplePetInstance.getSource().getItemId());
		out.writeMovementPath(path);
		
		return out.getPacket();
	}

	public static byte[] itemMegaphone(String text, boolean whisper, int channelId, int slot, Item item) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		out.write(8);
		out.writeMapleAsciiString(text);
		out.write(channelId);
		out.writeBool(whisper);
		if(item == null){
			out.write(0);
		}else{
			out.write(slot);
			out.writeItemInfo(item);
		}
		
		return out.getPacket();
	}

	public static byte[] getAvatarMegaphone(MapleCharacter chr, int channelId, int itemId, String[] lines, boolean whisper) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.SET_AVATAR_MEGAPHONE.getValue());
		out.writeInt(itemId);
		out.writeMapleAsciiString(chr.getFullName());
		for(String line : lines){
			out.writeMapleAsciiString(line);
		}
		out.writeInt(channelId);
		out.writeBool(whisper);
		addCharLook(out, chr, true);
		
		return out.getPacket();
	}

	public static byte[] petStatUpdate(MapleCharacter mapleCharacter) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.STAT_CHANGED.getValue());
		int mask = MapleStat.PET.getValue();
		out.write(0);
		out.writeInt(mask);
		MaplePetInstance[] pets = mapleCharacter.getPets();
		for(int i = 0; i < pets.length;i++){
			if(pets[i] != null){
				out.writeInt((int) pets[i].getSource().getUniqueId());
				out.writeInt(0);
			}else{
				out.writeLong(0);
			}
		}
		out.write(0);
		return out.getPacket();
	}

	public static byte[] changePetName(MapleCharacter character, String name) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PET_NAMECHANGE.getValue());
		out.writeInt(character.getId());
		out.write(0);
		out.writeMapleAsciiString(name);
		out.write(0);
		return out.getPacket();
	}

	public static byte[] whisper(String sender, int channelId, String text) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.WHISPER.getValue());
		out.write(0x12);
		out.writeMapleAsciiString(sender);
		out.writeShort(channelId);
		out.writeMapleAsciiString(text);
		return out.getPacket();
	}

	public static byte[] whisperReply(String name, int reply) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.WHISPER.getValue());
		out.write(0x0A);
		out.writeMapleAsciiString(name);
		out.write(reply);
		
		return out.getPacket();
	}

	public static byte[] portalChairEffect(MapleCharacter character) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.SHOW_CHAIR.getValue());
		out.writeInt(character.getId());
		out.writeInt(character.getActiveChair());
		
		return out.getPacket();
	}

	public static byte[] chairSitResponse(short chair) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CHAIR_RESPONSE.getValue());
		if(chair == -1){
			out.write(0);
		}else{
			out.write(1);
			out.writeShort(chair);
		}

		return out.getPacket();
	}
	
	public static byte[] messengerAddPlayer(String from, MapleCharacter chr, int position, int channel){
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.MESSENGER.getValue());
		out.write(0x00);
		out.write(position);
		addCharLook(out, chr, true);
		out.writeMapleAsciiString(from);
		out.write(channel);
		out.write(0x00);
		
		return out.getPacket();
	}

	public static byte[] messengerInvite(String from, int uniqueId) {
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.MESSENGER.getValue());
		
		out.write(0x03);
		out.writeMapleAsciiString(from);
		out.write(0);
		out.writeInt(uniqueId);
		out.write(0);
		
		return out.getPacket();
	}
	
	public static byte[] messengerJoin(int position){
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.MESSENGER.getValue());
		
		out.write(0x01);
		out.write(position);
		
		return out.getPacket();
	}
	
	public static byte[] messengerChat(String text){
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.MESSENGER.getValue());
		
		out.write(0x06);
		out.writeMapleAsciiString(text);
		
		return out.getPacket();
	}
	
	public static byte[] messengerNote(String text, int mode, int mode2){
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.MESSENGER.getValue());
		
		out.write(mode);
		out.writeMapleAsciiString(text);
		out.write(mode2);
		
		return out.getPacket();
	}

	public static byte[] messengerUpdatePlayer(MapleCharacter chr, int position, int channel) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.MESSENGER.getValue());
		
		out.write(0x07);
		out.write(position);
		addCharLook(out, chr, true);
		out.writeMapleAsciiString(chr.getName());
		out.write(channel);
		out.write(0);
		
		return out.getPacket();
	}
	
	public static byte[] messengerRemovePlayer(int position){
		
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.MESSENGER.getValue());
		
		out.write(0x02);
		
		out.write(position);
		
		return out.getPacket();
		
	}

	public static byte[] showCombo(int combo) {
		MaplePacketWriter out = new MaplePacketWriter(6);
		out.writeShort(SendOpcode.SHOW_COMBO.getValue());
		out.writeInt(combo);
		
		return out.getPacket();
	}
	
	public static byte[] questCompleteEffect(){
		return showSpecialEffect(SpecialEffect.QUEST_COMPLETE);
	}
	
	public static byte[] aranGodlyStats() {
		final MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FORCED_STAT_SET.getValue());
		mplew.write(new byte[] { (byte) 0x1F, (byte) 0x0F, 0, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xFF, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0x78, (byte) 0x8C });
		return mplew.getPacket();
	}

	public static byte[] resetAranGodStats() {
		MaplePacketWriter mplew = new MaplePacketWriter();
		mplew.writeShort(SendOpcode.FORCED_STAT_RESET.getValue());
		return mplew.getPacket();
	}
	
	public static byte[] lockUI(boolean enable){
		MaplePacketWriter out = new MaplePacketWriter(3);
		out.writeShort(SendOpcode.LOCK_UI.getValue());
		out.writeBool(enable);
		return out.getPacket();
	}
	
	public static byte[] disableUI(boolean disabled){
		MaplePacketWriter out = new MaplePacketWriter(3);
		out.writeShort(SendOpcode.DISABLE_UI.getValue());
		out.writeBool(disabled);
		return out.getPacket();
	}

	public static byte[] monsterBookCover(int coverId){
		MaplePacketWriter out = new MaplePacketWriter(6);
		out.writeShort(SendOpcode.MONSTER_BOOK_SET_COVER.getValue());
		out.writeInt(coverId);
		return out.getPacket();
	}

	public static byte[] monsterBookCardEffect() {
		MaplePacketWriter out = new MaplePacketWriter(3);
		out.writeShort(SendOpcode.FIELD_UPDATE.getValue());
		out.write(0x0D);
		return out.getPacket();
	}

	public static byte[] monsterBookAddCard(boolean success, int cardId, int level) {
		MaplePacketWriter out = new MaplePacketWriter(11);
		out.writeShort(SendOpcode.MONSTER_BOOK_SET_CARD.getValue());
		out.writeBool(success);
		out.writeInt(cardId);
		out.writeInt(level);
		return out.getPacket();
	}

	public static byte[] monsterBookForeignCardEffect(int id) {
		MaplePacketWriter out = new MaplePacketWriter(7);
		out.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
		out.writeInt(id);
		out.write(0x0D);
		return out.getPacket();
	}

	public static byte[] showCashShopInventory(MapleCharacter mapleCharacter) {
		MaplePacketWriter out = new MaplePacketWriter();
		
		CashShopInventory inventory = CashShopInventory.getCashInventory(mapleCharacter.getAccountId());
		
		out.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		out.write(0x4B);
		out.writeShort(inventory.numItems());
		
		for(CashItem item : inventory.getItems()){
			out.writeCashItemInformation(item, mapleCharacter.getAccountId());
		}
		
		out.writeShort(10);//storage slots
		out.writeShort(3);//chr slots
		
		return out.getPacket();
	}

	public static byte[] cashShopTakeItem(Item item, int position) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		
		out.write(0x68);
		out.writeShort(position);
		out.writeItemInfo(item);
		
		return out.getPacket();
	}

	public static byte[] chalkboard(MapleCharacter chr, String board, boolean open) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CHALKBOARD.getValue());
		out.writeInt(chr.getId());
		if(!open){
			out.write(0);
		}else{
			out.write(1);
			out.writeMapleAsciiString(board);
		}
		return out.getPacket();
	}

	public static byte[] guildBBS(GuildBulletin bulletin) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
		
		out.write(0x06);
		
		Collection<BulletinPost> posts = bulletin.getPosts();
		BulletinPost notice = bulletin.getNotice();
		
		if(notice != null){
			out.write(1);
			out.writeBulletinPost(notice);
		}else{
			out.write(0);
		}
		
		int numShown = posts.size();
		
		out.writeInt(posts.size());
		out.writeInt(numShown);
		
		for(BulletinPost post : posts){
			out.writeBulletinPost(post);
		}
		
		
		return out.getPacket();
	}

	public static byte[] guildBBSThread(BulletinPost post) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
		
		out.write(0x07);
		
		out.writeInt(post.getPostId());
		out.writeInt(post.getAuthor().getId());
		out.writeLong(getTime(post.getPostTime()));
		out.writeMapleAsciiString(post.getSubject());
		out.writeMapleAsciiString(post.getContent());
		out.writeInt(post.getEmote().getId());
		
		List<BulletinReply> replies = post.getReplies();
		
		out.writeInt(replies.size());
		
		for(BulletinReply reply : replies){
			out.writeInt(reply.getReplyId());
			out.writeInt(reply.getAuthor());
			out.writeLong(getTime(reply.getPostTime()));
			out.writeMapleAsciiString(reply.getContent());
		}
		
		return out.getPacket();
	}

	public static byte[] cashShopAddItem(CashItem targetItem, int id) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
		
		out.write(0x6A);
		out.writeCashItemInformation(targetItem, id);
		
		return out.getPacket();
	}
	
	public static byte[] hiredMerchantSpawn(MapleHiredMerchant merchant){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.SPAWN_HIRED_MERCHANT.getValue());
		
		out.writeInt(merchant.getOwnerId());
		out.writeInt(merchant.getMerchantType());
		out.writePos(merchant.getPosition());
		out.writeShort(0);
		out.writeMapleAsciiString(merchant.getOwnerName());
		out.write(0x05);
		out.writeInt(merchant.getObjectId());
		out.writeMapleAsciiString(merchant.getDescription());
		out.write(merchant.getMerchantType() % 10);
		out.write(1);
		out.writeBool(merchant.isOpen());
		
		return out.getPacket();
	}
	
	public static byte[] hiredMerchantRemove(MapleHiredMerchant merchant){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.getValue());
		
		out.writeInt(merchant.getOwnerId());
		
		return out.getPacket();
	}
	
	public static byte[] hiredMerchantVisitorAdd(MapleCharacter chr, int slot){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(0x04);
		out.write(slot);
		addCharLook(out, chr, false);
		out.writeMapleAsciiString(chr.getName());
		
		return out.getPacket();
	}
	
	public static byte[] hiredMerchantLeave(int slot, int status){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(0xA);
		out.write(slot);
		out.write(status);
		
		return out.getPacket();
	}

	public static byte[] createTradeRoom(TradeInterface ui) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.ROOM.getCode());
		out.write(3);
		out.write(2);
		out.write(ui.getPlayers().size()-1);
		int id = 0;
		for(MapleCharacter player : ui.getPlayers()){
			out.write(id);
			addCharLook(out, player, false);
			out.writeMapleAsciiString(player.getName());
			id++;
		}
		out.write(0xFF);
		
		
		return out.getPacket();
	}

	public static byte[] tradeInvite(String name, int id) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.INVITE.getCode());
		out.write(3);
		out.writeMapleAsciiString(name);
		out.writeInt(id);
		return out.getPacket();
	}

	public static byte[] tradePartner(MapleCharacter chr) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.VISIT.getCode());
		out.write(1);
		addCharLook(out, chr, false);
		out.writeMapleAsciiString(chr.getName());
		return out.getPacket();
	}

	public static byte[] tradeChat(String source, String msg, boolean owner) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.CHAT.getCode());
		out.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
		out.writeBool(!owner);
		out.writeMapleAsciiString(source+" : "+msg);
		
		return out.getPacket();
	}

	public static byte[] tradeSetMeso(boolean self, int amount) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.SET_MESO.getCode());
		out.write(self ? 0 : 1);
		out.writeInt(amount);
		
		return out.getPacket();
	}

	public static byte[] tradeConfirm() {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.CONFIRM.getCode());
		return out.getPacket();
	}

	public static byte[] tradeCancel(boolean creatorCancelled) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.EXIT.getCode());
		out.write(creatorCancelled ? 0 : 1);
		out.write(2);
		
		return out.getPacket();
	}

	public static byte[] tradeComplete(boolean creatorPerspective) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.EXIT.getCode());
		out.write(creatorPerspective ? 0 : 1);
		out.write(6);
		
		return out.getPacket();
	}

	public static byte[] tradeSetItem(Item item, int slot, boolean creatorPerspective) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.PUT_ITEM_TRADE.getCode());
		out.write(creatorPerspective ? 0 : 1);
		out.write(slot+1);
		out.writeItemInfo(item);
		return out.getPacket();
	}

	public static byte[] hiredMerchantResult(int id) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue());
		out.write(id);
		return out.getPacket();
	}

	public static byte[] hiredMerchantOpen(MapleCharacter chr, HiredMerchantInterface merchant, boolean firstTime) {
		
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.ROOM.getCode());
		
		out.write(0x05);
		out.write(0x04);
		
		out.writeShort(merchant.getVisitorSlot(chr) + 1);
		out.writeInt(merchant.getMerchantItemId());
		out.writeMapleAsciiString("Merchant");
		
		for(MapleCharacter visitor : merchant.getVisitors()){
			out.write(merchant.getVisitorSlot(visitor) + 1);
			addCharLook(out, visitor, false);
			out.writeMapleAsciiString(visitor.getName());
		}
		
		out.write(-1);
		if(merchant.isOwner(chr)){
			out.writeShort(0);//Messages?
		}else{
			out.writeShort(0);
		}
		out.writeMapleAsciiString(merchant.getOwnerName());
		if(merchant.isOwner(chr)){
			out.writeInt(0);//Time left?
			out.writeBool(firstTime);
			out.write(0);//Sold items
			out.writeInt(merchant.getMesos());
		}
		
		out.writeMapleAsciiString(merchant.getDescription());
		out.write(merchant.getCapacity());//Slots in the store (16)
		out.writeInt(chr.getMeso());
		out.write(merchant.getItems().size());
		if(merchant.getItems().isEmpty()){
			out.write(0);
		}else{
			for(HiredMerchantItem item : merchant.getItems()){
				out.writeShort(item.getAmountLeft());
				out.writeShort(item.getItem().getAmount());
				out.writeInt(item.getPrice());
				out.writeItemInfo(item.getItem());
			}
		}
		
		return out.getPacket();
	}

	public static byte[] hiredMerchantUpdateForOwner(HiredMerchantInterface merchant, MapleCharacter viewer) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
		out.writeInt(viewer.getMeso());
		out.write(merchant.getItems().size());
		for(HiredMerchantItem item : merchant.getItems()){
			out.writeShort(item.getAmountLeft());
			out.writeShort(item.getItem().getAmount());
			out.writeInt(item.getPrice());
			out.writeItemInfo(item.getItem());
		}
		
		return out.getPacket();
	}

	public static byte[] hiredMerchantChat(String source, String msg, int visitorSlot) {
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(PlayerInteractionHandler.Action.CHAT.getCode());
		out.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
		out.write(visitorSlot);
		/*if(visitorSlot > 0){
			out.writeMapleAsciiString(source+" : "+msg);
		}else{
			out.writeMapleAsciiString(source+" (Owner) : "+msg);
		}*/
		out.writeMapleAsciiString(source+" : "+msg);
		
		return out.getPacket();
	}
	
	public static byte[] sendTv(MapleCharacter star, List<String> messages, int type, MapleCharacter partner){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.SEND_TV.getValue());
		
		out.write(partner != null ? 3 : 1);
		out.write(type); //Heart = 2  Star = 1  Normal = 0
		addCharLook(out, star, false);
		out.writeMapleAsciiString(star.getName());
		
		if(partner == null){
			out.writeShort(0);
		}else{
			out.writeMapleAsciiString(partner.getName());
		}
		
		for (int i = 0; i < messages.size(); i++) {
			if (i == 4 && messages.get(4).length() > 15) {
				out.writeMapleAsciiString(messages.get(4).substring(0, 15));
			} else {
				out.writeMapleAsciiString(messages.get(i));
			}
		}
		
		out.writeInt(1337);//Time limit
		
		if(partner != null){
			addCharLook(out, partner, false);
		}
		
		return out.getPacket();
	}
	
	public static byte[] enabledTv(){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.ENABLE_TV.getValue());
		
		out.writeInt(0);
		out.write(0);
		
		return out.getPacket();
	}
	
	public static byte[] removeTv(){
		MaplePacketWriter out = new MaplePacketWriter();
		out.writeShort(SendOpcode.REMOVE_TV.getValue());
		return out.getPacket();
	}
	
/*	public static byte[] hiredMerchantOpen(MapleCharacter chr, MapleHiredMerchant merchant, boolean firstTime){
		MaplePacketWriter out = new MaplePacketWriter();
		
		out.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		out.write(0x05);
		out.write(0x05);
		out.write(0x04);
	}*/
	
}
