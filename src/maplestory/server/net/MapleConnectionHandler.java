package maplestory.server.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.TimerManager;
import tools.TimerManager.MapleTask;
import constants.LoginStatus;
import constants.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import maplestory.client.MapleClient;
import maplestory.server.MapleStory;
import maplestory.server.net.handlers.DefaultMaplePacketHandler;
import maplestory.server.net.handlers.NoOpHandler;
import maplestory.server.net.handlers.PongHandler;
import maplestory.server.net.handlers.channel.*;
import maplestory.server.net.handlers.login.*;
import maplestory.server.security.MapleAESOFB;

public class MapleConnectionHandler extends ChannelInboundHandlerAdapter {
	
	private MapleClient client;
	private Map<Integer, MaplePacketHandler> handlers;
	//private MaplePacketHandler[] handlers;
	private int world, channel;
	
	private Logger logger;
	
	private MapleTask pingTask;
	
	public MapleConnectionHandler(int world, int channel) {
        handlers = new HashMap<>();
		this.world = world;
		this.channel = channel;
	}
	
	public void resetHandlers(){
		handlers.put(RecvOpcode.PONG.getValue(), new PongHandler());
		handlers.put(RecvOpcode.PLAYER_UPDATE.getValue(), new NoOpHandler());
		
		if(client.getChannelId() == -1){
			handlers.put(RecvOpcode.LOGIN_PASSWORD.getValue(), new LoginPasswordHandler());
			handlers.put(RecvOpcode.SERVERLIST_REQUEST.getValue(), new ServerListRequestHandler());
			handlers.put(RecvOpcode.SERVERLIST_REREQUEST.getValue(), new ServerListRequestHandler());
			handlers.put(RecvOpcode.SERVERSTATUS_REQUEST.getValue(), new ServerStatusRequestHandler());
			handlers.put(RecvOpcode.CHARLIST_REQUEST.getValue(), new CharListRequestHandler());
			handlers.put(RecvOpcode.CHECK_CHAR_NAME.getValue(), new CheckCharNameHandler());
			handlers.put(RecvOpcode.CREATE_CHAR.getValue(), new CreateCharHandler());
			handlers.put(RecvOpcode.CHAR_SELECT_WITH_PIC.getValue(), new CharSelectWithPicHandler());
			handlers.put(RecvOpcode.REGISTER_PIC.getValue(), new RegisterPicHandler());
			handlers.put(RecvOpcode.DELETE_CHAR.getValue(), new DeleteCharHandler());
			handlers.put(RecvOpcode.CLIENT_START_ERROR.getValue(), new ClientStartErrorHandler());
			handlers.put(RecvOpcode.VIEW_ALL_CHAR.getValue(), new ViewAllCharactersHandler());
			handlers.put(RecvOpcode.VIEW_ALL_PIC_REGISTER.getValue(), new ViewAllPicRegisterHandler());
			handlers.put(RecvOpcode.VIEW_ALL_WITH_PIC.getValue(), new ViewAllSelectCharacterWithPicHandler());
		}else{
			handlers.put(RecvOpcode.PLAYER_LOGGEDIN.getValue(), new PlayerLoggedInHandler());
			handlers.put(RecvOpcode.MOVE_PLAYER.getValue(), new MovePlayerHandler());
			handlers.put(RecvOpcode.CHANGE_MAP.getValue(), new ChangeMapHandler());
			handlers.put(RecvOpcode.CHANGE_CHANNEL.getValue(), new ChangeChannelHandler());
			handlers.put(RecvOpcode.GENERAL_CHAT.getValue(), new GeneralChatHandler());
			handlers.put(RecvOpcode.NPC_ACTION.getValue(), new NpcAnimationHandler());
			handlers.put(RecvOpcode.MOVE_LIFE.getValue(), new MoveMonsterHandler());
			handlers.put(RecvOpcode.CLOSE_RANGE_ATTACK.getValue(), new CloseRangeDamageHandler());
			handlers.put(RecvOpcode.TAKE_DAMAGE.getValue(), new TakeDamageHandler());
			handlers.put(RecvOpcode.HEAL_OVER_TIME.getValue(), new HealOverTimeHandler());
			handlers.put(RecvOpcode.ITEM_PICKUP.getValue(), new ItemPickupHandler());
			handlers.put(RecvOpcode.DISTRIBUTE_AP.getValue(), new DistributeApHandler());
			handlers.put(RecvOpcode.AUTO_DISTRIBUTE_AP.getValue(), new AutoAssignAPHandler());
			handlers.put(RecvOpcode.DISTRIBUTE_SP.getValue(), new DistributeSpHandler());
			handlers.put(RecvOpcode.CHANGE_KEYMAP.getValue(), new KeymapChangeHandler());
			handlers.put(RecvOpcode.ENTER_MTS.getValue(), new EnterMTSHandler());
			handlers.put(RecvOpcode.MAGIC_ATTACK.getValue(), new MagicDamageHandler());
			handlers.put(RecvOpcode.CHANGE_MAP_SPECIAL.getValue(), new ChangeMapSpecialHandler());
			handlers.put(RecvOpcode.ITEM_MOVE.getValue(), new ItemMoveHandler());
			handlers.put(RecvOpcode.QUEST_ACTION.getValue(), new QuestActionHandler());
			handlers.put(RecvOpcode.PLAYER_UPDATE.getValue(), new NoOpHandler());
			handlers.put(RecvOpcode.TEMP_SKILL.getValue(), new NoOpHandler());
			handlers.put(RecvOpcode.NPC_TALK.getValue(), new NpcTalkHandler());
			handlers.put(RecvOpcode.NPC_TALK_MORE.getValue(), new NpcMoreTalkHandler());
			handlers.put(RecvOpcode.SPECIAL_MOVE.getValue(), new SpecialMoveHandler());
			handlers.put(RecvOpcode.CANCEL_BUFF.getValue(), new CancelBuffHandler());
			handlers.put(RecvOpcode.MOVE_SUMMON.getValue(), new MoveSummonHandler());
			handlers.put(RecvOpcode.SUMMON_ATTACK.getValue(), new SummonAttackHandler());
			handlers.put(RecvOpcode.FACE_EXPRESSION.getValue(), new FaceExpressionHandler());
			handlers.put(RecvOpcode.USE_ITEM.getValue(), new UseItemHandler());
			handlers.put(RecvOpcode.SKILL_EFFECT.getValue(), new SkillEffectHandler());
			handlers.put(RecvOpcode.CHAR_INFO_REQUEST.getValue(), new CharInfoRequestHandler());
			handlers.put(RecvOpcode.USE_MOUNT_FOOD.getValue(), new UseMountFoodHandler());
			handlers.put(RecvOpcode.USE_DOOR.getValue(), new UseMagicDoorHandler());
			handlers.put(RecvOpcode.RANGED_ATTACK.getValue(), new RangedAttackHandler());
			handlers.put(RecvOpcode.CANCEL_ITEM_EFFECT.getValue(), new CancelItemEffectHandler());
			handlers.put(RecvOpcode.DAMAGE_SUMMON.getValue(), new DamageSummonHandler());
			handlers.put(RecvOpcode.GIVE_FAME.getValue(), new ChangeFameHandler());
			handlers.put(RecvOpcode.USE_SUMMON_BAG.getValue(), new UseSummonBagHandler());
			handlers.put(RecvOpcode.AUTO_AGGRO.getValue(), new AutoAggroHandler());
			handlers.put(RecvOpcode.USE_SKILL_BOOK.getValue(), new UseSkillBookHandler());
			handlers.put(RecvOpcode.CLICK_GUIDE.getValue(), new ClickGuideHandler());
			handlers.put(RecvOpcode.NPC_SHOP.getValue(), new NpcShopHandler());
			handlers.put(RecvOpcode.PARTY_OPERATION.getValue(), new PartyOperationHandler());
			handlers.put(RecvOpcode.DENY_PARTY_REQUEST.getValue(), new DenyPartyInviteHandler());
			handlers.put(RecvOpcode.GUILD_OPERATION.getValue(), new GuildOperationHandler());
			handlers.put(RecvOpcode.PARTYCHAT.getValue(), new GroupChatHandler());
			handlers.put(RecvOpcode.MESO_DROP.getValue(), new MesoDropHandler());
			handlers.put(RecvOpcode.ENTER_CASHSHOP.getValue(), new EnterCashshopHandler());
			handlers.put(RecvOpcode.CHECK_CASH.getValue(), new UpdateCashShopCurrencyHandler());
			handlers.put(RecvOpcode.COUPON_CODE.getValue(), new CashShopCouponHandler());
			handlers.put(RecvOpcode.STORAGE.getValue(), new StorageHandler());
			handlers.put(RecvOpcode.USE_INNER_PORTAL.getValue(), new UseInnerPortalHandler());
			handlers.put(RecvOpcode.DAMAGE_REACTOR.getValue(), new DamageReactorHandler());
			handlers.put(RecvOpcode.DUEY_ACTION.getValue(), new DueyActionHandler());
			handlers.put(RecvOpcode.MOB_DAMAGE_MOB_FRIENDLY.getValue(), new MonsterDamageMonsterHandler());
			handlers.put(RecvOpcode.USE_UPGRADE_SCROLL.getValue(), new UseScrollHandler());
			handlers.put(RecvOpcode.SPAWN_PET.getValue(), new SpawnPetHandler());
			handlers.put(RecvOpcode.MOVE_PET.getValue(), new MovePetHandler());
			handlers.put(RecvOpcode.USE_CASH_ITEM.getValue(), new UseCashItemHandler());
			handlers.put(RecvOpcode.PET_LOOT.getValue(), new PetLootHandler());
			handlers.put(RecvOpcode.CASHSHOP_OPERATION.getValue(), new CashShopOperationHandler());
			handlers.put(RecvOpcode.MONSTER_BOMB.getValue(), new MonsterBombHandler());
			handlers.put(RecvOpcode.WHISPER.getValue(), new WhisperHandler());
			handlers.put(RecvOpcode.USE_CHAIR.getValue(), new ChairSitRequestHandler());
			handlers.put(RecvOpcode.CHAIR_ACTION.getValue(), new SitRequestHandler());
			handlers.put(RecvOpcode.MESSENGER.getValue(), new MessengerHandler());
			handlers.put(RecvOpcode.ARAN_COMBO_COUNTER.getValue(), new AranComboHandler());
			handlers.put(RecvOpcode.MONSTER_BOOK_COVER.getValue(), new ChangeMonsterBookCoverHandler());
			handlers.put(RecvOpcode.CLOSE_CHALKBOARD.getValue(), new CloseChalkboardHandler());
			handlers.put(RecvOpcode.BBS_OPERATION.getValue(), new GuildBBSHandler());
			handlers.put(RecvOpcode.NOTE_ACTION.getValue(), new NoteActionHandler());
			handlers.put(RecvOpcode.USE_BOX_ITEM.getValue(), new UseBoxItemHandler());
			handlers.put(RecvOpcode.PLAYER_INTERACTION.getValue(), new PlayerInteractionHandler());
			handlers.put(RecvOpcode.HIRED_MERCHANT_REQUEST.getValue(), new HiredMerchantRequestHandler());
			handlers.put(RecvOpcode.BUDDYLIST_MODIFY.getValue(), new BuddyListModifyHandler());
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};
        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};

        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        
        MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend, (short) (0xFFFF - MapleStory.getServerConfig().getMapleVersion()));
        MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv, (short) MapleStory.getServerConfig().getMapleVersion());
		
		ctx.pipeline().addFirst(new MaplePacketDecoder(this), new MaplePacketEncoder(this), new SendPingOnIdle(5, 1, 5, this));
		
		ctx.channel().writeAndFlush(PacketFactory.getHandshakePacket(MapleStory.getServerConfig().getMapleVersion(), ivRecv, ivSend, 8));

		logger = LoggerFactory.getLogger("["+ctx.channel().remoteAddress()+"]");
		
		client = new MapleClient(ctx.channel(), logger, sendCypher, recvCypher);
		client.setWorldId(world);
		client.setChannelId(channel);

		logger.info("Client "+ctx.channel().remoteAddress()+" connected");
		
		resetHandlers();
		
		pingTask = TimerManager.scheduleRepeatingTask(new Runnable() {
			
			@Override
			public void run() {
				if(client != null){
					client.sendPing();
				}
			}
			
		}, 0, 5000);
	}
	
	public MapleClient getClient() {
		return client;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		MaplePacket packet = (MaplePacket) msg;
		
		MaplePacketHandler handler = handlers.getOrDefault((int) packet.getType(), new DefaultMaplePacketHandler(packet.getType()));
		
		handler.handle(packet.getBuffer(), client);
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		
		if(!(cause instanceof IOException)){
			logger.error(cause.toString());
			
			for(StackTraceElement e : cause.getStackTrace()){
				logger.error(e.toString());
			}
		}else{
			logger.error(cause.toString());
		}
		

		
		if(MapleStory.getServerConfig().isPlayerKickedOnError()){
			ctx.channel().close();
		}else{
			if(getClient() != null){
				if(getClient().getCharacter() != null){
					getClient().getCharacter().sendMessage(MessageType.POPUP, "An error has occured! Check the console...");
				}
			}
		}
		

		
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Client "+ctx.channel().remoteAddress()+" disconnected!");
		
		if(client.getId() >= 0){
			if(client.getCharacter() != null){
				if(client.getCharacter().getMap() != null){
					client.getCharacter().getMap().removePlayer(client.getCharacter());
				}
				if(!client.isChangingChannels()){
					client.getWorld().getPlayerStorage().removePlayer(client.getCharacter().getId());
					client.setLoggedInStatus(LoginStatus.OFFLINE);
				}
			}else{
				client.setLoggedInStatus(LoginStatus.OFFLINE);
			}
			
			if(world != -1 && channel != -1 && client.getCharacter() != null){
				client.setChannelId(-1);
				client.setWorldId(-1);
				
				client.getCharacter().cleanup();
				client.getCharacter().saveToDatabase(false);
			}	
		}else{
			if(client.getCharacter() != null){
				client.getCharacter().getMap().removePlayer(client.getCharacter());
			}
		}
		
		if(pingTask != null){
			pingTask.cancel(true);
		}
		
	}
	
	static class SendPingOnIdle extends IdleStateHandler{

		private MapleConnectionHandler mch;
		
		public SendPingOnIdle(int readerIdleTimeSeconds,
				int writerIdleTimeSeconds, int allIdleTimeSeconds, MapleConnectionHandler mch) {
			super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
			this.mch = mch;
		}
		
		@Override
		protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
				throws Exception {
			
			mch.getClient().sendPing();
			
			super.channelIdle(ctx, evt);
		}
		
	}
	
}
