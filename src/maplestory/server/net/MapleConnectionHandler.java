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
import constants.ServerConstants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import maplestory.client.MapleClient;
import maplestory.server.MapleServer;
import maplestory.server.net.handlers.DefaultMaplePacketHandler;
import maplestory.server.net.handlers.NoOpHandler;
import maplestory.server.net.handlers.PongHandler;
import maplestory.server.net.handlers.channel.AranComboHandler;
import maplestory.server.net.handlers.channel.AutoAggroHandler;
import maplestory.server.net.handlers.channel.AutoAssignAPHandler;
import maplestory.server.net.handlers.channel.CancelBuffHandler;
import maplestory.server.net.handlers.channel.CashShopCouponHandler;
import maplestory.server.net.handlers.channel.CashShopOperationHandler;
import maplestory.server.net.handlers.channel.ChairSitRequestHandler;
import maplestory.server.net.handlers.channel.ChangeChannelHandler;
import maplestory.server.net.handlers.channel.ChangeFameHandler;
import maplestory.server.net.handlers.channel.ChangeMapHandler;
import maplestory.server.net.handlers.channel.ChangeMapSpecialHandler;
import maplestory.server.net.handlers.channel.ChangeMonsterBookCoverHandler;
import maplestory.server.net.handlers.channel.CharInfoRequestHandler;
import maplestory.server.net.handlers.channel.ClickGuideHandler;
import maplestory.server.net.handlers.channel.CloseRangeDamageHandler;
import maplestory.server.net.handlers.channel.DamageReactorHandler;
import maplestory.server.net.handlers.channel.DamageSummonHandler;
import maplestory.server.net.handlers.channel.DenyPartyInviteHandler;
import maplestory.server.net.handlers.channel.DistributeApHandler;
import maplestory.server.net.handlers.channel.DistributeSpHandler;
import maplestory.server.net.handlers.channel.DueyActionHandler;
import maplestory.server.net.handlers.channel.EnterCashshopHandler;
import maplestory.server.net.handlers.channel.EnterMTSHandler;
import maplestory.server.net.handlers.channel.FaceExpressionHandler;
import maplestory.server.net.handlers.channel.GeneralChatHandler;
import maplestory.server.net.handlers.channel.GroupChatHandler;
import maplestory.server.net.handlers.channel.GuildOperationHandler;
import maplestory.server.net.handlers.channel.HealOverTimeHandler;
import maplestory.server.net.handlers.channel.CancelItemEffectHandler;
import maplestory.server.net.handlers.channel.ItemMoveHandler;
import maplestory.server.net.handlers.channel.ItemPickupHandler;
import maplestory.server.net.handlers.channel.KeymapChangeHandler;
import maplestory.server.net.handlers.channel.MagicDamageHandler;
import maplestory.server.net.handlers.channel.MesoDropHandler;
import maplestory.server.net.handlers.channel.MessengerHandler;
import maplestory.server.net.handlers.channel.MonsterBombHandler;
import maplestory.server.net.handlers.channel.MonsterDamageMonsterHandler;
import maplestory.server.net.handlers.channel.MoveMonsterHandler;
import maplestory.server.net.handlers.channel.MovePetHandler;
import maplestory.server.net.handlers.channel.MovePlayerHandler;
import maplestory.server.net.handlers.channel.MoveSummonHandler;
import maplestory.server.net.handlers.channel.NpcAnimationHandler;
import maplestory.server.net.handlers.channel.NpcMoreTalkHandler;
import maplestory.server.net.handlers.channel.NpcShopHandler;
import maplestory.server.net.handlers.channel.NpcTalkHandler;
import maplestory.server.net.handlers.channel.PartyOperationHandler;
import maplestory.server.net.handlers.channel.PetLootHandler;
import maplestory.server.net.handlers.channel.PlayerLoggedInHandler;
import maplestory.server.net.handlers.channel.QuestActionHandler;
import maplestory.server.net.handlers.channel.RangedAttackHandler;
import maplestory.server.net.handlers.channel.SitRequestHandler;
import maplestory.server.net.handlers.channel.SkillEffectHandler;
import maplestory.server.net.handlers.channel.SpawnPetHandler;
import maplestory.server.net.handlers.channel.SpecialMoveHandler;
import maplestory.server.net.handlers.channel.StorageHandler;
import maplestory.server.net.handlers.channel.SummonAttackHandler;
import maplestory.server.net.handlers.channel.TakeDamageHandler;
import maplestory.server.net.handlers.channel.UpdateCashShopCurrencyHandler;
import maplestory.server.net.handlers.channel.UseCashItemHandler;
import maplestory.server.net.handlers.channel.UseInnerPortalHandler;
import maplestory.server.net.handlers.channel.UseItemHandler;
import maplestory.server.net.handlers.channel.UseMagicDoorHandler;
import maplestory.server.net.handlers.channel.UseMountFoodHandler;
import maplestory.server.net.handlers.channel.UseScrollHandler;
import maplestory.server.net.handlers.channel.UseSkillBookHandler;
import maplestory.server.net.handlers.channel.UseSummonBagHandler;
import maplestory.server.net.handlers.channel.WhisperHandler;
import maplestory.server.net.handlers.login.CharListRequestHandler;
import maplestory.server.net.handlers.login.CharSelectWithPicHandler;
import maplestory.server.net.handlers.login.CheckCharNameHandler;
import maplestory.server.net.handlers.login.ClientStartErrorHandler;
import maplestory.server.net.handlers.login.CreateCharHandler;
import maplestory.server.net.handlers.login.DeleteCharHandler;
import maplestory.server.net.handlers.login.LoginPasswordHandler;
import maplestory.server.net.handlers.login.RegisterPicHandler;
import maplestory.server.net.handlers.login.ServerListRequestHandler;
import maplestory.server.net.handlers.login.ServerStatusRequestHandler;
import maplestory.server.net.handlers.login.ViewAllCharactersHandler;
import maplestory.server.security.MapleAESOFB;
import maplestory.world.World;

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
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		byte key[] = {0x13, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, (byte) 0xB4, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00, 0x33, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00};
        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};

        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        
        MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend, (short) (0xFFFF - ServerConstants.MAPLE_VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv, (short) ServerConstants.MAPLE_VERSION);
		
		ctx.pipeline().addFirst(new MaplePacketDecoder(this), new MaplePacketEncoder(this), new SendPingOnIdle(5, 1, 5, this));
		
		ctx.channel().writeAndFlush(PacketFactory.getHandshakePacket(ServerConstants.MAPLE_VERSION, ivRecv, ivSend, 8));

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
		

		
		if(ServerConstants.KICK_ON_ERROR){
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
