package maplestory.player.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import constants.MessageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.life.MapleHiredMerchant;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MapleHiredMerchantInterface extends MapleUserInterface implements HiredMerchantInterface {

	private MapleHiredMerchant merchant;
	
	private Set<Integer> pastVisitors;
	
	private List<HiredMerchantItem> listings;
	
	private int earnedMesos;
	
	private boolean open;
	
	public MapleHiredMerchantInterface(MapleHiredMerchant merchant) {
		super(3);
		this.merchant = merchant;
		pastVisitors = new HashSet<>();
		listings = new ArrayList<>();
		earnedMesos = 0;
		close();
	}
	
	@Override
	public boolean isOpen() {
		return open;
	}

	public void open(){
		open = true;
	}
	
	public void close(){
		open = false;
		for(MapleCharacter visitor : getVisitors()){
			kickOut(visitor, "");
		}
	}
	
	private void kickOut(MapleCharacter visitor, String reason) {
		
		visitor.getClient().sendPacket(PacketFactory.hiredMerchantLeave(getVisitorSlot(visitor)+1, 0x11));
		
		if(!reason.isEmpty()){
			visitor.sendMessage(MessageType.POPUP, reason);
		}
		
	}

	@Override
	protected void sendChatPacket(MapleCharacter to, String source, String msg) {
		to.getClient().sendPacket(PacketFactory.hiredMerchantChat(source, msg, getVisitorSlot(source)));
	}

	@Override
	public void addPlayer(MapleCharacter chr) {
		super.addPlayer(chr);
		if(isOwner(chr)){
			close();
		}
		chr.getClient().sendPacket(PacketFactory.hiredMerchantOpen(chr, this, !pastVisitors.contains(chr.getId())));
		if(!pastVisitors.contains(chr.getId())){
			sendChatPacket(chr, getDefaultChatSource(), "Welcome to "+merchant.getDescription());
		}
		pastVisitors.add(chr.getId());
	}
	
	private int getVisitorSlot(String name){
		int slot = 0;
		for(MapleCharacter other : getPlayers()){
			if(getOwnerName().equalsIgnoreCase(name)){
				continue;
			}
			if(other.getName().equalsIgnoreCase(name)){
				return slot;
			}
			slot++;
		}
		return 0;
	}
	
	@Override
	public int getVisitorSlot(MapleCharacter chr) {
		if(isOwner(chr)){
			return -1;
		}
		int slot = 0;
		for(MapleCharacter other : getPlayers()){
			if(isOwner(other)){
				continue;
			}
			if(other.equals(chr)){
				return slot;
			}
			slot++;
		}
		throw new IllegalArgumentException(chr.getName()+" is not in this interface");
	}
	
	@Override
	public int getMerchantItemId() {
		return merchant.getMerchantType();
	}
	
	@Override
	public boolean isOwner(MapleCharacter chr) {
		return merchant.isOwner(chr);
	}
	
	@Override
	public String getOwnerName() {
		return merchant.getOwnerName();
	}

	@Override
	public int getMesos() {
		return earnedMesos;
	}
	
	@Override
	public String getDescription() {
		return merchant.getDescription();
	}
	
	@Override
	public int getCapacity() {
		return 16;
	}
	
	@Override
	public List<HiredMerchantItem> getItems() {
		return listings;
	}
	
	@Override
	public List<MapleCharacter> getVisitors() {
		return getPlayers().stream().filter(chr -> !isOwner(chr)).collect(Collectors.toList());
	}
	
	@Override
	public void addItem(Item clone, short amount, int price) {
		ItemListing listing = new ItemListing(clone, price, amount);
		
		listings.add(listing);
		
		updateShopForViewers();
	}
	
	@Override
	public Item removeItem(int slot) {
		HiredMerchantItem item = listings.remove(slot);
		
		updateShopForViewers();
		
		return item.getItem().copyOf(item.getAmountLeft());
	}
	
	@Override
	protected String getDefaultChatSource() {
		return "[Merchant]";
	}
	
	@Override
	public void buyItem(MapleCharacter visitor, int itemSlot, int amount) {
		HiredMerchantItem listing = listings.get(itemSlot);
		Item desired = listing.getItem().copyOf(amount * listing.getItem().getAmount());
		System.out.println(amount+" "+listing.getItem().getAmount());
		int price = listing.getPrice() * amount;
		
		if(visitor.getMeso() >= price){
			
			if(visitor.getInventory(desired.getItemId()).addItem(desired)){
				visitor.giveMesos(-price, false, false);
				listing.setAmountLeft(listing.getAmountLeft() - amount);
				if(listing.getAmountLeft() == 0){
					listings.remove(itemSlot);
				}
				earnedMesos += price;
				
				updateShopForViewers();
				
			}else{
				visitor.sendMessage(MessageType.POPUP, "Your inventory is full.");
			}

		}else{
			visitor.sendMessage(MessageType.POPUP, "You don't have enough mesos.");
		}
	}

	@Override
	public void removeStore() {
		merchant.remove();
	}
	
	private void updateShopForViewers(){
		for(MapleCharacter viewer : getPlayers()){
			viewer.getClient().sendPacket(PacketFactory.hiredMerchantUpdateForOwner(this, viewer));
		}
	}
	
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	@Getter
	static class ItemListing implements HiredMerchantItem {
		
		private final Item item;
		
		private final int price;
		
		@Setter
		private int amountLeft;
		
	}
	
}
