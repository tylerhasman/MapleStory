package maplestory.server.net.handlers.channel;

import constants.LoginStatus;
import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.life.MapleHiredMerchant;
import maplestory.map.MapleMapObject;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.player.ui.HiredMerchantInterface;
import maplestory.player.ui.InvitationJoinable;
import maplestory.player.ui.TradeInterface;
import maplestory.server.net.MaplePacketHandler;
import maplestory.util.Hex;

public class PlayerInteractionHandler extends MaplePacketHandler {

    public enum Action {
        CREATE(0),
        INVITE(2),
        DECLINE(3),
        VISIT(4),
        ROOM(5),
        CHAT(6),
        CHAT_THING(8),
        EXIT(0xA),
        OPEN(0xB),
        TRADE_BIRTHDAY(0x0E),
        PUT_ITEM_TRADE(0xF),
        SET_MESO(0x10),
        CONFIRM(0x11),
        TRANSACTION(0x14),
        ADD_ITEM(0x16),
        BUY(0x17),
        UPDATE_MERCHANT(0x19),
        REMOVE_ITEM(0x1B),
        BAN_PLAYER(0x1C),
        MERCHANT_THING(0x1D),
        OPEN_STORE(0x1E),
        PUT_ITEM_MERCHANT(0x21),
        MERCHANT_BUY(0x22),
        TAKE_ITEM_BACK(0x26),
        MAINTENANCE_OFF(0x27),
        MERCHANT_ORGANIZE(0x28),
        CLOSE_MERCHANT(0x29),
        REAL_CLOSE_MERCHANT(0x2A),
        MERCHANT_MESO(0x2B),
        SOMETHING(0x2D),
        VIEW_VISITORS(0x2E),
        BLACKLIST(0x2F),
        REQUEST_TIE(0x32),
        ANSWER_TIE(0x33),
        GIVE_UP(0x34),
        EXIT_AFTER_GAME(0x38),
        CANCEL_EXIT(0x39),
        READY(0x3A),
        UN_READY(0x3B),
        START(0x3D),
        GET_RESULT(0x3E),
        SKIP(0x3F),
        MOVE_OMOK(0x40),
        SELECT_CARD(0x44);
        final byte code;

        private Action(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }

		public static Action byId(byte code) {
			for(Action a : values()){
				if(a.code == code){
					return a;
				}
			}
			return null;
		}
    }
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) throws Exception {
		Action action = Action.byId(buf.readByte());
		
		if(action == Action.CREATE){
			
			byte creationType = buf.readByte();
			
			if(creationType == 3){
				client.getCharacter().createTrade();
			}else if(creationType == 5){
				
				String name = readMapleAsciiString(buf);
				buf.skipBytes(3);
				int itemId = buf.readInt();
				
				MapleHiredMerchant check = client.getCharacter().getWorld().getMerchantByOwner(client.getCharacter().getId());
				
				if(check != null) {
					client.getCharacter().getWorld().unregisterMerchant(check);
				}
				
				if(client.getCharacter().getItemQuantity(itemId, false) == 0){
					return;
				}
				
				MapleHiredMerchant merchant = new MapleHiredMerchant(client.getCharacter(), itemId, name);
				
				
				client.getCharacter().getWorld().registerMerchant(merchant);
				merchant.setPosition(client.getCharacter().getPosition());
				
				client.getCharacter().getMap().addMapObject(merchant, true);
				
				merchant.openFor(client.getCharacter());
				
			}else{
				client.getLogger().warn("Unknown creation type "+creationType);
			}
		}else if(action == Action.INVITE){
			if(!(client.getCharacter().getOpenInterface() instanceof InvitationJoinable)){
				client.sendReallowActions();
				return;
			}
			
			InvitationJoinable ij = (InvitationJoinable) client.getCharacter().getOpenInterface();
			
			int id = buf.readInt();
			
			MapleCharacter invited = client.getWorld().getPlayerStorage().getById(id);
			
			if(invited == null){
				client.sendReallowActions();
				client.getCharacter().sendMessage(MessageType.PINK_TEXT, "An error occured.");
				return;
			}
			
			if(invited.getClient().getLoginStatus() != LoginStatus.IN_GAME){
				client.getCharacter().sendMessage(MessageType.PINK_TEXT, "Could not invite "+invited.getName()+" they appear to be offline.");
			}else if(invited.getOpenInterface() != null){
				client.getCharacter().sendMessage(MessageType.PINK_TEXT, invited.getName()+" is currently busy, try again later.");
			}else{
				ij.invitePlayer(invited);
				client.getCharacter().getOpenInterface().chat("Invited '"+invited.getName()+"'");
			}
		}else if(action == Action.VISIT){
			int oid = buf.readInt();
			MapleMapObject obj = client.getCharacter().getMap().getObject(oid);
			if(obj == null){
				obj = client.getCharacter().getMap().getPlayerById(oid);
			}
			if(obj instanceof MapleCharacter){
				MapleCharacter other = (MapleCharacter) obj;
				
				if(other.getOpenInterface() instanceof TradeInterface){
					client.getCharacter().openInterface(other.getOpenInterface());
				}else{
					client.getCharacter().sendMessage(MessageType.PINK_TEXT, "That trade no longer exists.");
					client.sendReallowActions();
				}
			}else if(obj instanceof MapleHiredMerchant){
				
				MapleHiredMerchant merchant = (MapleHiredMerchant) obj;
				
				merchant.openFor(client.getCharacter());
				
			}else{
				client.getCharacter().sendMessage(MessageType.PINK_TEXT, "Could not join room");
				client.sendReallowActions();
			}
		}else if(action == Action.CHAT){
			String msg = readMapleAsciiString(buf);
			
			client.getCharacter().getOpenInterface().chat(msg, client.getCharacter());
		}else if(action == Action.SET_MESO){
			int amount = buf.readInt();
			
			if(amount <= 0){
				client.sendReallowActions();
				return;
			}
			
			TradeInterface ti = (TradeInterface) client.getCharacter().getOpenInterface();
			ti.offerMesos(amount, client.getCharacter());
		}else if(action == Action.CONFIRM){
			
			TradeInterface ti = (TradeInterface) client.getCharacter().getOpenInterface();
			ti.confirmTrade(client.getCharacter());
		}else if(action == Action.MERCHANT_BUY){
			
			if(!(client.getCharacter().getOpenInterface() instanceof HiredMerchantInterface)){
				return;
			}
			
			int item = buf.readByte();
			int amount = buf.readShort();
			
			HiredMerchantInterface merchant = (HiredMerchantInterface) client.getCharacter().getOpenInterface();
			
			merchant.buyItem(client.getCharacter(), item, amount);
			
		}else if(action == Action.PUT_ITEM_MERCHANT){
			
			MapleCharacter chr = client.getCharacter();
			
			if(!(chr.getOpenInterface() instanceof HiredMerchantInterface)){
				return;
			}
			
			HiredMerchantInterface merchant = (HiredMerchantInterface) chr.getOpenInterface();
			
			if(!merchant.isOwner(chr)){
				return;
			}
			
			byte inventory = buf.readByte();
			short slot = buf.readShort();
			short amount = buf.readShort();
			short perBundle = buf.readShort();
			int price = buf.readInt();
			
			Item item = chr.getInventory(InventoryType.getById(inventory)).getItem(slot);
			
			Item clone = item.copyOf(perBundle);
			
			chr.getInventory(clone.getItemId()).removeItem(clone.getItemId(), amount);
			
			merchant.addItem(clone, amount, price);
		}else if(action == Action.TAKE_ITEM_BACK){
			
			MapleCharacter chr = client.getCharacter();
			
			short slot = buf.readShort();
			
			if(!(chr.getOpenInterface() instanceof HiredMerchantInterface)){
				return;
			}
			
			HiredMerchantInterface merchant = (HiredMerchantInterface) chr.getOpenInterface();
			
			Item item = merchant.removeItem(slot);
			
			chr.getInventory(item.getItemId()).addItem(item);
		}else if(action == Action.CLOSE_MERCHANT){
			MapleCharacter chr = client.getCharacter();
			
			if(!(chr.getOpenInterface() instanceof HiredMerchantInterface)){
				return;
			}
			
			HiredMerchantInterface merchant = (HiredMerchantInterface) chr.getOpenInterface();
			
			if(!merchant.isOwner(chr)){
				return;
			}
			
			merchant.removeStore();
			
		}else if(action == Action.PUT_ITEM_TRADE){
			
			if(!(client.getCharacter().getOpenInterface() instanceof TradeInterface)){
				return;
			}
			
			TradeInterface ti = (TradeInterface) client.getCharacter().getOpenInterface();
			
			byte inventory = buf.readByte();
			short slot = buf.readShort();
			short amount = buf.readShort();
			byte target = (byte) (buf.readByte()-1);//Wtf nexon, 0 indexing are you retarded
			
			Inventory inv = client.getCharacter().getInventory(InventoryType.getById(inventory));
			
			Item item = inv.getItem(slot);
			
			if(item == null){
				client.getLogger().warn("Attempted to trade null item "+slot+" in "+inv.getType());
				return;
			}
			
			if(item.getAmount() < amount){
				client.getLogger().warn("Attempted to trade more than they have. Tried "+amount+" against "+item);
				return;
			}
			
			int inInventory = inv.countById(item.getItemId());
			int alreadyTrading = ti.countTradedItem(item.getItemId(), client.getCharacter());
			
			if(inInventory - alreadyTrading <= 0){
				client.sendReallowActions();
				return;
			}else if(alreadyTrading + amount > inInventory){
				client.getCharacter().sendMessage(MessageType.POPUP, "Please enter a number between 1 and "+(inInventory - alreadyTrading));
				client.sendReallowActions();
				return;
			}
			
			item = item.copyOf(amount);
			
			ti.putItem(item, target, client.getCharacter());
		}else if(action == Action.MAINTENANCE_OFF){
			
			if(!(client.getCharacter().getOpenInterface() instanceof HiredMerchantInterface)){
				return;
			}
			
			HiredMerchantInterface merchant = (HiredMerchantInterface) client.getCharacter().getOpenInterface();
			
			merchant.open();
			
		}else if(action == Action.EXIT){
			client.getCharacter().exitUserInteface();
		}
		
		byte[] remaining = new byte[buf.readableBytes()];
		
		
		if(remaining.length > 0){

			buf.readBytes(remaining);
			
			client.getLogger().warn(action+": "+Hex.toHex(remaining));
		}

		client.sendReallowActions();

	}

}
