package maplestory.player.monsterbook;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleStory;
import maplestory.server.net.PacketFactory;
import me.tyler.mdf.MapleFile;
import me.tyler.mdf.Node;

public class MonsterBook {
	
	@Getter
	private int cover;
	
	private Map<Integer, Integer> cards;
	
	private WeakReference<MapleCharacter> owner;
	
	public MonsterBook(MapleCharacter owner) {
		cards = new HashMap<>();
		cover = -1;
		this.owner = new WeakReference<MapleCharacter>(owner);
	}
	
	public List<MonsterCard> getCards(){
		return cards.entrySet().stream().map(entry -> new MonsterCard(entry.getKey(), entry.getValue())).collect(Collectors.toList());
	}
	
	public int getCardLevel(int cardId){
		return cards.getOrDefault(cardId, 0);
	}
	
	public int getBookLevel(){
		return (int) Math.max(1, Math.sqrt((getNormalCards() + getSpecialCards()) / 5));
	}

	public int getTotalCards() {
		return cards.size();
	}
	
	public void increaseLevel(int cardId){
		if(getMonsterId(cardId) < 0){
			throw new IllegalArgumentException(cardId+" is not a valid card id");
		}
		int nextLevel = cards.getOrDefault(cardId, 0) + 1;
		boolean full = nextLevel > 5;
		nextLevel = Math.min(5, nextLevel);
		
		cards.put(cardId, nextLevel);
		
		MapleCharacter owner = this.owner.get();
		
		if(owner != null){
			MapleClient client = owner.getClient();
			
			client.sendPacket(PacketFactory.monsterBookAddCard(!full, ItemInfoProvider.getMonsterBookItemId(cardId), nextLevel));
			if(!full){
				client.sendPacket(PacketFactory.monsterBookCardEffect());
				owner.getMap().broadcastPacket(PacketFactory.monsterBookForeignCardEffect(owner.getId()), owner.getId());
			}
			
			
		}
	}

	public boolean changeCover(int cover) {
		int monsterId = getMonsterId(cover);
		
		if(monsterId == -1){
			return false;
		}
		
		this.cover = cover;
		return true;
	}
	
	public static List<Integer> getAllBookIds(){
		List<Integer> list = new ArrayList<>();
		
		MapleFile data = MapleStory.getDataFile("MonsterBookData.mdf");
		
		Node root = data.getRootNode();
		
		for(Node child : root){
			list.add(Integer.valueOf(child.getName()));
		}
		
		return list;
	}
	
	public static int getMonsterId(int bookId){
		MapleFile data = MapleStory.getDataFile("MonsterBookData.mdf");
		
		Node root = data.getRootNode();
		
		if(root.hasChild(String.valueOf(bookId))){
			return root.readInt(String.valueOf(bookId));
		}else{
			return -1;
		}
	}
	
	public static int getMonsterCardId(int monsterId){
		MapleFile data = MapleStory.getDataFile("MonsterBookData.mdf");
		
		Node root = data.getRootNode();
		
		for(Node child : root){
			if(child.intValue() == monsterId){
				return Integer.valueOf(child.getName());
			}
		}
		
		return -1;
	}

	public static int getCardDropChance(int monsterId) {
		return 1000000 / 10;//1 in 10
	}

	public boolean hasCover() {
		return cover >= 0;
	}

	public int getNormalCards() {
		return cards.size() - getSpecialCards();
	}
	
	public int getSpecialCards(){
		return (int) cards.keySet().stream().filter(id -> ItemInfoProvider.getMonsterBookItemId(id) / 1000 >= 2388).count();
	}
	
}
