package maplestory.player.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import constants.MessageType;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MapleTradeInterface extends MapleUserInterface implements TradeInterface, InvitationJoinable {

	private Set<Integer> invited;
	
	private Map<Integer, TradeOffer> offers;
	
	private int creatorId;
	
	private boolean cancelled;
	
	private boolean finished;
	
	public MapleTradeInterface(MapleCharacter initial) {
		super(2);
		cancelled = false;
		finished = false;
		creatorId = initial.getId();
		invited = new HashSet<>();
		offers = new HashMap<>();
		addPlayer(initial);
	}
	
	private boolean isCreator(MapleCharacter chr){
		return chr.getId() == creatorId;
	}
	
	@Override
	public void removePlayer(MapleCharacter chr) {
		super.removePlayer(chr);
		if(!cancelled && !finished){
			cancelTrade(chr);
		}
	}
	
	private TradeOffer getOffer(MapleCharacter chr){
		return offers.get(chr.getId());
	}
	
	@Override
	public void invitePlayer(MapleCharacter chr) {
		invited.add(chr.getId());
		sendInvitePacket(chr);
	}
	
	@Override
	public boolean isInvited(MapleCharacter chr) {
		return invited.contains(chr.getId());
	}
	
	@Override
	public void addPlayer(MapleCharacter chr) {
		if(getPlayers().size() > 0 && !isInvited(chr)){
			chr.sendMessage(MessageType.PINK_TEXT, "You are not invited to join that trade.");
			return;
		}
		chat(chr.getName()+" has joined the trade.");
		for(MapleCharacter other : getPlayers()){
			other.getClient().sendPacket(PacketFactory.tradePartner(chr));
		}
		
		super.addPlayer(chr);
		
		chr.getClient().sendPacket(PacketFactory.createTradeRoom(this));
		invited.remove(chr.getId());
		offers.put(chr.getId(), new TradeOffer());
	}
	
	@Override
	public void putItem(Item item, int slot, MapleCharacter chr) {
		getOffer(chr).items[slot] = item;
		for(MapleCharacter other : getPlayers()){
			other.getClient().sendPacket(PacketFactory.tradeSetItem(item, slot, other.equals(chr)));
		}
	}

	@Override
	public void confirmTrade(MapleCharacter chr) {
		getOffer(chr).locked = true;
		
		for(MapleCharacter other : getPlayers()){
			if(!other.equals(chr)){
				other.getClient().sendPacket(PacketFactory.tradeConfirm());
			}
		}
		
		chat(chr.getName()+" has locked in.");
		
		if(allLocked()){
			trade();
		}
	}
	
	private TradeOffer getReceived(MapleCharacter chr){
		for(MapleCharacter other : getPlayers()){
			if(!other.equals(chr)){
				return getOffer(other);
			}
		}
		throw new IllegalStateException("No trade offer found for "+chr.getName());
	}
	
	private boolean fitsInInventories(){
		for(MapleCharacter chr : getPlayers()){
			if(!fitsInInventory(chr, getReceived(chr).items)){
				return false;
			}
		}
		return true;
	}
	
	private boolean fitsInInventory(MapleCharacter chr, Item[] items){
		Map<InventoryType, Integer> neededSlots = new HashMap<>();
		for(Item item : items){
			if(item == null){
				continue;
			}
			InventoryType type = InventoryType.getByItemId(item.getItemId());
			neededSlots.put(type, neededSlots.getOrDefault(type, 0)+1);
		}
		for(Entry<InventoryType, Integer> entry : neededSlots.entrySet()){
			if(!chr.getInventory(entry.getKey()).hasSpace(entry.getValue())){
				return false;
			}
		}
		
		return true;
	}
	
	private void cancelTrade(MapleCharacter chr){
		cancelled = true;
		for(MapleCharacter pl : getPlayers()){
			pl.getClient().sendPacket(PacketFactory.tradeCancel(isCreator(chr)));
			pl.exitUserInteface();
		}
	}
	
	private void trade(){
		finished = true;
		if(fitsInInventories()){
			
			for(MapleCharacter chr : getPlayers()){
				TradeOffer other = getReceived(chr);
				TradeOffer giving = getOffer(chr);
				
				for(Item item : other.items){
					if(item == null){
						continue;
					}
					chr.getInventory(item.getItemId()).addItem(item);
				}
				
				for(Item item : giving.items){
					if(item == null){
						continue;
					}
					chr.getInventory(item.getItemId()).removeItem(item);
				}
				
				chr.giveMesos(other.mesos, false, false);
				chr.giveMesos(-giving.mesos, false, false);
				chr.getClient().sendPacket(PacketFactory.tradeComplete(isCreator(chr)));
			}
			
			for(MapleCharacter chr : getPlayers()){
				chr.exitUserInteface();//Exit after
			}
			
		}else{
			for(MapleCharacter chr : getPlayers()){
				chr.sendMessage(MessageType.PINK_TEXT, "There is not enough inventory space to complete the trade");
			}
			cancelTrade(getPlayers().iterator().next());
		}
	}
	
	private boolean allLocked(){
		for(MapleCharacter chr : getPlayers()){
			if(!getOffer(chr).locked){
				return false;
			}
		}
		return true;
	}

	@Override
	public void offerMesos(int amount, MapleCharacter chr) {
		if(getOffer(chr).locked){
			chr.sendMessage(MessageType.POPUP, "You have already locked in.");
			return;
		}
		getOffer(chr).mesos = amount;
		chr.getClient().sendPacket(PacketFactory.tradeSetMeso(true, amount));
		for(MapleCharacter other : getPlayers()){
			if(other.equals(chr)){
				continue;
			}
			other.getClient().sendPacket(PacketFactory.tradeSetMeso(false, amount));
		}
	}

	@Override
	protected void sendChatPacket(MapleCharacter to, String source, String msg) {
		to.getClient().sendPacket(PacketFactory.tradeChat(source, msg, to.getName().equals(source)));
	}
	
	protected void sendInvitePacket(MapleCharacter chr) {
		MapleCharacter other = getPlayers().iterator().next();
		chr.getClient().sendPacket(PacketFactory.tradeInvite(other.getName(), other.getId()));
	}
	
	

	static class TradeOffer {
		
		private int mesos;
		private boolean locked;
		private Item[] items;
		
		public TradeOffer() {
			mesos = 0;
			items = new Item[9];
			locked = false;
		}
		
	}

}
