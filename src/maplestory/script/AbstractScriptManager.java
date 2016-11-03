package maplestory.script;

import java.awt.Point;

import tools.data.output.MaplePacketWriter;
import constants.MessageType;
import constants.PopupInfo;
import lombok.Getter;
import maplestory.client.MapleClient;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.life.MapleLifeFactory;
import maplestory.life.MapleMonster;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapItemPresent;
import maplestory.map.MapleMapObject;
import maplestory.map.MapleMapItem.DropType;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.quest.MapleQuest;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.server.net.PacketFactory;
import maplestory.server.net.SendOpcode;
import maplestory.skill.SkillFactory;

public class AbstractScriptManager {

	@Getter
	private final MapleCharacter character;
	
	public AbstractScriptManager(MapleCharacter chr) {
		character = chr;
	}
	
	public MapleClient getClient(){
		return character.getClient();
	}
	
	public MapleMap getMap(){
		return character.getMap();
	}
	
	public boolean hasItemBuff(int itemId){
		return character.hasBuff(ItemInfoProvider.getItemEffect(itemId));
	}
	
	public void openDuey(){
		character.openDuey();
	}
	
	public void showIntro(String path){
		character.getClient().sendPacket(PacketFactory.showIntro(path));
	}
	
	public void giveExp(int amount){
		character.giveExp(amount);
	}
	
	public void startQuestScript(int questId, int npcId){
		character.openQuestNpc(new MapleScript("scripts/quest/"+questId+".js"), questId, npcId, false);
	}
	
	public void endQuestScript(int questId, int npcId){
		character.openQuestNpc(new MapleScript("scripts/quest/"+questId+".js"), questId, npcId, true);
	}
	
	public void openGuildCreateMenu(){
		MaplePacketWriter writer = new MaplePacketWriter();
		writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		writer.write(1);
		getClient().sendPacket(writer.getPacket());
	}
	
	public void useItem(int itemId){
		Item item = ItemFactory.getItem(itemId, 1);
		
		if(item.isA(ItemType.USE)){
			
			ItemInfoProvider.getItemEffect(itemId).applyTo(character);
			
		}else{
			throw new IllegalArgumentException(itemId+" is not a consumable.");
		}
	}
	
	public void playSound(String path){
		getClient().sendPacket(PacketFactory.playSound(path));
	}
	
	public void changeJob(MapleJob job){
		character.changeJob(job);
	}
	
	public void playSkillEffect(int id){
		character.playSkillEffect(id);
	}
	
	public void sendGuideHint(int id, int time){
		getClient().sendPacket(PacketFactory.guideHint(id, time));
	}
	
	public void spawnGuide(){
		getClient().sendPacket(PacketFactory.spawnGuide(true));
	}
	
	public void destroyGuide(){
		getClient().sendPacket(PacketFactory.spawnGuide(false));
	}
	
	public void updateQuest(int questId, String status){
		character.getClient().sendPacket(PacketFactory.updateQuest(questId, status));
	}
	
	public void guideTalk(String message){
		getClient().sendPacket(PacketFactory.guideTalk(message));
		
		int guideNpc = getCharacter().getJob() == MapleJob.NOBLESSE ? 1101008 : 1202000;
		
		String guideName = MapleLifeFactory.getNpcName(guideNpc);
		
		getClient().getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, guideName+": "+message);
	}
	
	public void dropItemPresent(Point position, int... items){
		Item[] it = new Item[items.length];
		for(int i = 0; i < items.length;i++){
			it[i] = ItemFactory.getItem(items[i], 1);
		}
		MapleMapItemPresent present = new MapleMapItemPresent(it, getClient().getCharacter().getId(), position, DropType.OWNER_ONLY, getClient().getCharacter().getMap(), getClient().getCharacter());
		
		getMap().addMapObject(present, false);
		present.broadcastDropPacket();
	}

	public void showInfo(String path){
		try{
			getClient().getCharacter().showInfo(PopupInfo.valueOf(path));
		}catch(IllegalArgumentException e){
			getClient().getLogger().error("No PopupInfo value for "+path);
		}

	}
	
	public void showEffect(String path){
		getClient().sendPacket(PacketFactory.showInfo(path));
	}
	
	public void showInfoText(String text){
		getClient().getCharacter().showInfoText(text);
	}
	
	public void changeMusic(String music){
		getMap().broadcastPacket(PacketFactory.musicChange(music));
	}
	
	public MapleMonster spawnMonster(int monsterId, Point pos){
		MapleMonster monster = MapleLifeFactory.getMonster(monsterId);
		monster.setPosition(pos);
		
		getMap().spawnMonster(monster);
		
		return monster;
	}
	
	public MapleMonster spawnUntargetableMonster(int monsterId, Point pos){
		MapleMonster monster = MapleLifeFactory.getMonster(monsterId);
		monster.setUntargetable(true);
		monster.setPosition(pos);
		getMap().spawnMonster(monster);
		
		return monster;
	}
	
	public void dropMesos(MapleMapObject source, int amount){
		dropMesos(source, source.getPosition(), amount);
	}
	
	public void dropMesos(MapleMapObject source, Point pos, int amount){
		getMap().dropMesos(amount, pos, source);
	}
	
	public void dropItem(MapleMapObject source, int itemId, int amount){
		getMap().dropItem(ItemFactory.getItem(itemId, amount), source.getPosition(), source);
	}
	
	public Point dropItems(MapleMapObject source, int[][] items){
		
		Point p = new Point(source.getPosition());
		
		p.x -= 35;
		
		for(int i = 0; i < items.length;i++){
			int[] item = items[i];
			
			if(item == null){
				continue;
			}
			
			getMap().dropItem(ItemFactory.getItem(item[0], item[1]), p, source);
			
			p.x += 35;
		}
		
		return p;
		
	}
	
	public void openNpc(int id){
		getCharacter().openNpc(id);
	}
	
	public void openNpc(String script, int id){
		MapleScript ms = new MapleScript("scripts/npc/"+script+".js", "scripts/npc/fallback.js");
		
		getCharacter().openNpc(ms, id);
	}
	
	public void openNpc(int id, String path){
		getCharacter().openNpc(new MapleScript(path, "scripts/npc/fallback.js"), MapleLifeFactory.getNPC(id));
	}
	
	public void dropMesoPresent(Point position, int amount){
		
		MapleMapItemPresent present = new MapleMapItemPresent(amount, getClient().getCharacter().getId(), position, DropType.OWNER_ONLY, getClient().getCharacter().getMap(), getClient().getCharacter());
		
		getMap().addMapObject(present, false);
		present.broadcastDropPacket();
	}
	
	public void warp(int mapId){
		getClient().getCharacter().changeMap(mapId);
	}
	
	public void warp(int mapId, String pid){
		MapleMap map = getClient().getChannel().getMapFactory().getMap(mapId);
		getClient().getCharacter().changeMap(map, map.getPortal(pid));
	}
	
	public void warp(int mapId, int pid){
		MapleMap map = getClient().getChannel().getMapFactory().getMap(mapId);
		
		getClient().getCharacter().changeMap(map, map.getPortal(pid));
	}
	
	public boolean hasItem(int itemId){
		return itemAmount(itemId) >= 1;
	}
	
	public boolean hasItem(int itemId, int amount){
		return itemAmount(itemId) >= amount;
	}
	
	public int itemAmount(int itemId){
		return getClient().getCharacter().getInventory(itemId).countById(itemId);
	}
	
	public boolean isQuestStarted(int quest){
		return getClient().getCharacter().getQuest(quest).getStatus() == MapleQuestStatus.STARTED;
	}
	
	public boolean isQuestCompleted(int quest){
		return getClient().getCharacter().getQuest(quest).getStatus() == MapleQuestStatus.COMPLETED;
	}
	
	public void startQuest(int questId, int npc){
		MapleQuest.getQuest(questId).start(getCharacter(), npc);
	}
	
	public void updateQuest(int questId, int data){
		getCharacter().getQuest(questId).setProgress(0, data);
	}
	
	public boolean canHold(int itemId){
		return getClient().getCharacter().getInventory(itemId).getFreeSlot() > -1;
	}
	
	public void teachSkill(int id, int level, int masterLevel){
		getCharacter().changeSkillLevel(SkillFactory.getSkill(id), level, masterLevel);
	}
	
	public void sendMessage(String type, String msg){
		getClient().getCharacter().sendMessage(MessageType.valueOf(type), msg);
	}
	
	public boolean giveItem(int id, int amount) {
		
		if(amount > 0){
			return character.getInventory(id).addItem(ItemFactory.getItem(id, amount));
		}else{
			return character.getInventory(id).removeItem(id, -amount);
		}
		
	}
	
	public void displayAranIntro() {
		String intro = "";
		switch (getCharacter().getMapId()) {
		case 914090010:
			intro = "Effect/Direction1.img/aranTutorial/Scene0";
			break;
		case 914090011:
			intro = "Effect/Direction1.img/aranTutorial/Scene1" + (getCharacter().getGender() == 0 ? "0" : "1");
			break;
		case 914090012:
			intro = "Effect/Direction1.img/aranTutorial/Scene2" + (getCharacter().getGender() == 0 ? "0" : "1");
			break;
		case 914090013:
			intro = "Effect/Direction1.img/aranTutorial/Scene3";
			break;
		case 914090100:
			intro = "Effect/Direction1.img/aranTutorial/HandedPoleArm" + (getCharacter().getGender() == 0 ? "0" : "1");
			break;
		case 914090200:
			intro = "Effect/Direction1.img/aranTutorial/Maha";
			break;
		}
		if(intro.isEmpty()){
			getClient().getLogger().error("Can't display aran intro on map "+getCharacter().getMapId());
			return;
		}
		showIntro(intro);
	}
	
	public String itemName(int id){
		return ItemInfoProvider.getItemName(id);
	}
	
	public boolean giveItem(int id){
		return giveItem(id, 1);
	}
	
	public void giveMesos(int amount){
		getClient().getCharacter().giveMesos(amount);
	}
	
	public void spawnMonster(int id, int amount, MapleMapObject obj){
		spawnMonster(id, amount, obj.getPosition().x, obj.getPosition().y);
	}
	
	public void spawnMonster(int id, int amount, int x, int y){
		for(int i = 0; i < amount;i++){
			MapleMonster monster = MapleLifeFactory.getMonster(id);
			
			monster.setPosition(new Point(x, y));
			
			getCharacter().getMap().spawnMonster(monster);
		}
	}
	
	public void sendHint(String hint, int w, int h){
		getCharacter().sendHint(hint, w, h);
	}
	
}
