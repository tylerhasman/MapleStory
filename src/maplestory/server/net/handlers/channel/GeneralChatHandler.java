package maplestory.server.net.handlers.channel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import tools.data.output.MaplePacketWriter;
import constants.ExpTable;
import constants.ItemLetterFont;
import constants.LoginStatus;
import constants.MessageType;
import constants.Song;
import constants.SpecialEffect;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.guild.MapleGuild;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryOperation;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.life.MapleLifeFactory;
import maplestory.life.MapleMonster;
import maplestory.life.MapleMount;
import maplestory.life.MapleNPC;
import maplestory.map.MapleMagicDoor;
import maplestory.map.MapleMap;
import maplestory.map.MapleMapItem;
import maplestory.map.MapleMapObject;
import maplestory.map.MapleMapObjectType;
import maplestory.map.MaplePortal;
import maplestory.map.MapleReactor;
import maplestory.map.MapleMap.SpawnPoint;
import maplestory.map.MapleReactor.ReactorData;
import maplestory.party.MapleParty;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.quest.MapleQuest;
import maplestory.quest.MapleQuestInstance;
import maplestory.quest.MapleQuestInstance.MapleQuestStatus;
import maplestory.script.MapleScript;
import maplestory.script.NpcConversationManager;
import maplestory.server.MapleServer;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.server.net.SendOpcode;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;
import maplestory.util.StringUtil;

public class GeneralChatHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		String text = readMapleAsciiString(buf);
		
	
		
		try{
			if(text.startsWith("!")){
				if(!client.isGM()){
					client.getCharacter().sendMessage(MessageType.PINK_TEXT, "Press the trade button for your commands");
					return;
				}
				String[] args = text.split(" ");
				
				if(args[0].equalsIgnoreCase("!ping")){
					String name = client.getCharacter().getName();
					if(args.length > 1){
						name = args[1];
					}
					
					MapleCharacter victim = client.getWorld().getPlayerStorage().getByName(name);
					
					if(victim != null){
						client.getCharacter().sendMessage(MessageType.PINK_TEXT, name+"'s ping is "+victim.getClient().getPing()+"ms");
					}else{
						client.getCharacter().sendMessage(MessageType.POPUP, "No player named "+args[1]);
					}
				}else if(args[0].equalsIgnoreCase("!purgemaps")){
					
					for(MapleMap map : client.getChannel().getMapFactory().getLoadedMaps()){
						if(map.getPlayers().size() == 0){
							client.getChannel().getMapFactory().unloadMap(map.getMapId());
						}
					}
					
				}else if(args[0].equalsIgnoreCase("!unloadmap")){
					
					int id = Integer.parseInt(args[1]);
					
					client.getChannel().getMapFactory().unloadMap(id);
				}else if(args[0].equalsIgnoreCase("!effect")){
					client.sendPacket(PacketFactory.showSpecialEffect(SpecialEffect.valueOf(args[1])));
				}else if(args[0].equalsIgnoreCase("!reloadmapscript")){
					client.getCharacter().getMap().reloadMapScript();
				}else if(args[0].equalsIgnoreCase("!textdrop")){
					
					String msg = "";
					
					for(int i = 1; i < args.length;i++){
						msg += args[i] + " ";
					}
					
					msg = msg.substring(0, msg.length() - 1);
					
					client.getCharacter().getMap().dropText(msg, ItemLetterFont.GREEN, client.getCharacter().getPosition().x, client.getCharacter().getPosition().y, true);
					
				}else if(args[0].equalsIgnoreCase("!skill")){
					int id = Integer.parseInt(args[1]);
					int level = Integer.parseInt(args[2]);
					client.getCharacter().changeSkillLevel(SkillFactory.getSkill(id), level, 0);
				}else if(args[0].equalsIgnoreCase("!reactorstate")){
					String name = args[1];
					int state = Integer.parseInt(args[2]);
					
					MapleReactor reactor = client.getCharacter().getMap().getReactorByName(name);
					reactor.changeState(state);
					System.out.println(reactor.getReactorData());
				}else if(args[0].equalsIgnoreCase("!warpto")){
					String name = args[1];
					
					MapleCharacter victim = client.getWorld().getPlayerStorage().getByName(name);
					
					if(victim != null){
						client.getCharacter().changeMap(victim.getMap(), victim.getMap().getClosestPortal(victim.getPosition()));
					}else{
						client.getCharacter().sendMessage(MessageType.POPUP, "No player named "+args[1]);
					}
				}else if(args[0].equalsIgnoreCase("!fame")){
					client.getCharacter().gainFame(200);
				}else if(args[0].equalsIgnoreCase("!vacmonstersto")){
					String name = args[1];
					
					MapleCharacter victim = client.getWorld().getPlayerStorage().getByName(name);
					
					if(victim != null){
						for(MapleMonster monster : victim.getMap().getMonsters()){
							monster.teleport(victim.getPosition());
						}
					}else{
						client.getCharacter().sendMessage(MessageType.POPUP, "No player named "+args[1]);
					}
				}else if(args[0].equalsIgnoreCase("!bring")){
					String name = args[1];
					MapleCharacter victim = client.getWorld().getPlayerStorage().getByName(name);
					
					if(victim != null){
						victim.changeMap(client.getCharacter().getMap(), client.getCharacter().getMap().getClosestPortal(client.getCharacter().getPosition()));
					}else{
						client.getCharacter().sendMessage(MessageType.POPUP, "No player named "+args[1]);
					}
				}else if(args[0].equalsIgnoreCase("!tip")){
					client.sendPacket(PacketFactory.yellowPopupTip("It worked, thank you :)"));
				}else if(args[0].equalsIgnoreCase("!spawnreactor")){
					int id = Integer.parseInt(args[1]);
					
					MapleMap map = client.getCharacter().getMap();
					
					MapleReactor reactor = new MapleReactor(id, new ReactorData(5000, "none"), map);
					
					reactor.setPosition(client.getCharacter().getPosition());
					
					map.spawnReactor(reactor);
				}else if(args[0].equalsIgnoreCase("!spawnpoints")){
					for(SpawnPoint sp : client.getCharacter().getMap().getMonsterSpawnPoints()){
						if(client.getCharacter().distance(sp.getLocation().x, sp.getLocation().y) < 50){
							
						}
						sp.spawnMonster(client.getCharacter().getMap(), true);	
					}
					
				}else if(args[0].equalsIgnoreCase("!cleardrops")){
					for(MapleMapObject obj : client.getCharacter().getMap().getObjects()){
						if(obj instanceof MapleMapItem){
							((MapleMapItem)obj).destroy();
						}
					}
				}else if(args[0].equalsIgnoreCase("!dump")){
					
					File file = new File("thread_dump_"+System.currentTimeMillis()+".txt");
					file.createNewFile();
					
					BufferedWriter out = new BufferedWriter(new FileWriter(file));
					
					for(Entry<Thread, StackTraceElement[]> data : Thread.getAllStackTraces().entrySet()){
						
						Thread thread = data.getKey();
						StackTraceElement[] stack = data.getValue();
						
						out.write(thread.toString());
						out.newLine();
						out.newLine();
						
						for(StackTraceElement ele : stack){
							out.write(ele.toString());
							out.newLine();
						}
						
						out.newLine();
						out.newLine();
						out.newLine();
						out.newLine();
					}
					
					out.flush();
					out.close();
					
				}else if(args[0].equalsIgnoreCase("!deadlocks")){
					ThreadMXBean bean = ManagementFactory.getThreadMXBean();
					
					long[] threadIds = bean.findDeadlockedThreads();
					
					if(threadIds != null){
						
						ThreadInfo[] info = bean.getThreadInfo(threadIds);
						
						System.out.println(threadIds.length+" threads are deadlocked!");
						
						for(ThreadInfo i : info){
							System.out.println("THREAD "+i.getThreadId()+" "+i.getThreadName());
							for(StackTraceElement ele : i.getStackTrace()){
								System.out.println(ele);
							}
						}
					}else{
						System.out.println("No deadlocked!");
					}
				}else if(args[0].equalsIgnoreCase("!spawnreactoritems")){
					MapleMap map = client.getCharacter().getMap();
					for(MapleMapObject obj : map.getObjects()){
						if(obj instanceof MapleReactor){
							MapleReactor reactor = (MapleReactor) obj;
							
							if(reactor.getReactorData().getCurrentStage().getItemId() > 0){
								map.dropItem(ItemFactory.getItem(reactor.getReactorData().getCurrentStage().getItemId(), 1), reactor.getPosition(), client.getCharacter());
							}
						}
					}
				}else if(args[0].equalsIgnoreCase("!npcs")){
					for(MapleMapObject mapObj : client.getCharacter().getMap().getObjects()){
						if(mapObj instanceof MapleNPC){
							MapleNPC npc = (MapleNPC) mapObj;
							
							client.getCharacter().sendMessage(MessageType.NOTICE, npc.getName()+" "+npc.getId());
						}
					}
				}else if(args[0].equalsIgnoreCase("!forfeit")){
					int id = Integer.parseInt(args[1]);
					
					MapleQuestInstance inst = client.getCharacter().getQuest(id);
					
					inst.setStatus(MapleQuestInstance.MapleQuestStatus.STARTED.getId());
					
					MapleQuest q = MapleQuest.getQuest(id);
					
					q.forfeit(client.getCharacter());
				}else if(args[0].equalsIgnoreCase("!restartquest")){
					int id = Integer.parseInt(args[1]);
					
					MapleQuest q = MapleQuest.getQuest(id);
					
					q.start(client.getCharacter(), 0);
				}else if(args[0].equalsIgnoreCase("!test")){
					
					
				}else if(args[0].equalsIgnoreCase("!emblem")){
					MaplePacketWriter writer = new MaplePacketWriter();
					writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
					writer.write(17);
					client.sendPacket(writer.getPacket());
				}else if(args[0].equalsIgnoreCase("!unblockportals")){
					for(MaplePortal portal : client.getCharacter().getMap().getPortals()){
						portal.unblockUsage(client.getCharacter());
					}
				}else if(args[0].equalsIgnoreCase("!quests")){
					for(MapleQuestInstance quest : client.getCharacter().getQuests(MapleQuestStatus.STARTED)){
						
						client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, String.valueOf(quest.getQuest().getId()));
						
					}
				}else if(args[0].equalsIgnoreCase("!currentmap")){
					client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, client.getCharacter().getMapId()+" is your current map id");
				}else if(args[0].equalsIgnoreCase("!unloadmap")){
					int id = Integer.parseInt(args[1]);
					
					client.getChannel().getMapFactory().unloadMap(id);
				}else if(args[0].equalsIgnoreCase("!dispose")){
					client.getCharacter().disposeOpenNpc();
				}else if(args[0].equalsIgnoreCase("!fixinventory")){
					List<InventoryOperation> ops = new ArrayList<>();
					for(InventoryType type : InventoryType.values()){
						Inventory inv = client.getCharacter().getInventory(type);
						if(type == InventoryType.EQUIP){
							for(int i = 1; i < inv.getSize();i++){
								ops.add(InventoryOperation.addItem(ItemFactory.getItem(1000000, 1), i));
								ops.add(InventoryOperation.removeItem(ItemFactory.getItem(1000000, 1), i));
							}
						}
						for(int i : inv.getItems().keySet()){
							Item item = inv.getItem(i);
							ops.add(InventoryOperation.addItem(item, i));
						}
					}
					client.sendPacket(PacketFactory.getInventoryOperationPacket(false, ops));
					client.getCharacter().sendMessage(MessageType.NOTICE, "Done");
				}else if(args[0].equalsIgnoreCase("!clearquests")){
					MapleQuest.clearCache();
				}else if(args[0].equalsIgnoreCase("!createguild")){
					MaplePacketWriter writer = new MaplePacketWriter();
					writer.writeShort(SendOpcode.GUILD_OPERATION.getValue());
					writer.write(1);
					client.sendPacket(writer.getPacket());

				}else if(args[0].equalsIgnoreCase("!disbandguild")){
					client.getCharacter().getGuild().disbandGuild();
				}else if(args[0].equalsIgnoreCase("!meso")){
					int amount = Integer.parseInt(args[1]);
					
					client.getCharacter().giveMesos(amount);
				}else if(args[0].equalsIgnoreCase("!guild")){
					MapleGuild guild = client.getCharacter().getGuild();
					
					if(guild == null){
						client.getCharacter().sendMessage(MessageType.PINK_TEXT, "Your not in a guild");
					}else{
						client.getCharacter().sendMessage(MessageType.PINK_TEXT, "Your guild is named "+guild.getName());

						client.sendPacket(PacketFactory.guildUpdateInfo(client.getCharacter(), guild));
					}
				}else if(args[0].equalsIgnoreCase("!party")){
					
					MapleCharacter chr = client.getCharacter();
					MapleParty party = chr.getParty();
					
					if(party == null){
						chr.sendMessage(MessageType.PINK_TEXT, "Your not in a party.");
					}else{
						chr.sendMessage(MessageType.PINK_TEXT, "Party: "+party.getMembers().toString());
						party.updateParty();
					}
				}else if(args[0].equalsIgnoreCase("!fixall")){
					for(MapleCharacter chr : MapleServer.getWorld(0).getPlayerStorage().getAllPlayers()){
						if(chr.getClient().getWorldId() == -1){
							chr.getClient().setLoggedInStatus(LoginStatus.OFFLINE);
						}
					}
				}else if(args[0].equalsIgnoreCase("!mobs")){
					for(MapleMonster monster : client.getCharacter().getMap().getMonsters()){
						client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, monster.getStats().getName()+" "+monster.getId());
					}
				}else if(args[0].equalsIgnoreCase("!topoff")){
					client.getCharacter().setExp(ExpTable.getExpNeededForLevel(client.getCharacter().getLevel()) - 1);
				}else if(args[0].equalsIgnoreCase("!fix")){
					client.sendReallowActions();
				}else if(args[0].equalsIgnoreCase("!god")){
					client.getCharacter().toggleGodMode();
				}else if(args[0].equalsIgnoreCase("!cleardrops")){
					
					client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "Destroyed "+client.getCharacter().getMap().countObjectsOfType(MapleMapObjectType.ITEM)+" items.");
					
					client.getCharacter().getMap().getObjects().stream().filter(obj -> obj.getType() == MapleMapObjectType.ITEM).forEach(obj -> {
						
						if(obj instanceof MapleMapItem){
							((MapleMapItem)obj).destroy();
						}
						
					});
				}else if(args[0].equalsIgnoreCase("!magicdoors")){
					client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "You have "+client.getCharacter().getMagicDoors().size()+" doors open right now.");
					
					int i = 0;
					
					for(MapleMagicDoor door : client.getCharacter().getMagicDoors()){
						client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, (i++)+" - "+door);
					}
				}else if(args[0].equalsIgnoreCase("!clearcooldowns")){
					
					client.getCharacter().clearAllCooldowns();
					client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "Cooldowns cleared");
					
				}else if(args[0].equalsIgnoreCase("!levelup")){
					int amount = Integer.parseInt(args[1]);
					
					if(amount > 0){
						
						for(int i = 0; i < amount;i++){
							client.getCharacter().giveExp(ExpTable.getExpNeededForLevel(client.getCharacter().getLevel()));
						}
						
					}
					
				}else if(args[0].equalsIgnoreCase("!music")){
					String name = args[1];
					
					Song song = Song.getById(name);
					
					if(song == null){
						client.getCharacter().sendMessage(MessageType.PINK_TEXT, "No song exists with id "+name);
					}else{
						client.getCharacter().getMap().broadcastPacket(PacketFactory.musicChange(song));
						client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "Music changed to "+name);
					}
					
				}else if(args[0].equalsIgnoreCase("!maxall")){
					for(Skill skill : SkillFactory.getAllSkills()){
						if(client.getCharacter().getSkillLevel(skill) == skill.getMaxLevel()){
							continue;
						}
						
						client.getCharacter().changeSkillLevel(skill, skill.getMaxLevel(), skill.getMaxLevel());
						client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "Maxed "+SkillFactory.getSkillName(skill.getId()));
					}
				}else if(args[0].equalsIgnoreCase("!reactors")){
					
					MapleCharacter chr = client.getCharacter();

					for(MapleMapObject obj : chr.getMap().getObjects()){
						if(obj instanceof MapleReactor){
							MapleReactor reactor = (MapleReactor) obj;
							chr.sendMessage(MessageType.LIGHT_BLUE_TEXT, reactor.getId()+" "+reactor.getPosition()+" "+reactor.getReactorData()+"\r\n\r\n");
						}
					}
				}else if(args[0].equalsIgnoreCase("!pos")){
					MapleCharacter chr = client.getCharacter();
					
					chr.sendMessage(MessageType.NOTICE, chr.getPosition().x+" / "+chr.getPosition().y);
				}else if(args[0].equalsIgnoreCase("!lookup")){
					String term = "";
					
					for(int i = 1; i < args.length;i++){
						term += args[i] + " ";
					}
					
					term = term.substring(0, term.length() - 1).toLowerCase();
					
					List<Integer> itemIds = ItemInfoProvider.getAllItemIds();
					
					Map<Integer, String> items = new HashMap<>();
					
					for(int id : itemIds){
						items.put(id, ItemInfoProvider.getItemName(id));
					}
					
					Map<Integer, String> matches = new HashMap<>();
					
					for(Entry<Integer, String> item : items.entrySet()){
						if(item.getValue() == null){
							continue;
						}
						if(item.getValue().toLowerCase().contains(term)){
							if(ItemInfoProvider.getSlotMax(item.getKey()) == 0){
								continue;
							}
							
							matches.put(item.getKey(), item.getValue());
						}
					}
					
					MapleNPC npc = MapleLifeFactory.getNPC(2080005);
					
					NpcConversationManager cm = new NpcConversationManager(client.getCharacter(), npc);
					
					MapleScript script = new MapleScript("scripts/npc/lookup_command_npc.js", "scripts/npc/error.js");
					
					Bindings bindings = new SimpleBindings();
					bindings.put("search_term", term);
					bindings.put("results", matches);
					bindings.put("cm", cm);
					
					client.getCharacter().openNpc(script, bindings, npc);
					
				}else if(args[0].equalsIgnoreCase("!level")){
					client.getCharacter().setExp(0);
					client.getCharacter().setLevel(Integer.parseInt(args[1]));
				}else if(args[0].equalsIgnoreCase("!map")){
					int id = Integer.parseInt(args[1]);
					
					if(client.getChannel().getMapFactory().getMap(id) != null){
						client.getCharacter().changeMap(id);
					}else{
						client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "No map with id "+id);
					}
					
				}else if(args[0].equalsIgnoreCase("!spawn")){
					int id = Integer.parseInt(args[1]);
					int amount = Integer.parseInt(args[2]);
					
					for(int i = 0; i < amount;i++){
						MapleMonster monster = MapleLifeFactory.getMonster(id);
						monster.setPosition(client.getCharacter().getPosition());
						client.getCharacter().getMap().spawnMonster(monster);
					}
				}else if(args[0].equalsIgnoreCase("!shutdown")){
					MapleServer.getInstance().shutdown();
				}else if(args[0].equalsIgnoreCase("!damage")){
					client.getCharacter().damage(Integer.parseInt(args[1]));
				}else if(args[0].equalsIgnoreCase("!revive")){
					client.getCharacter().setHp(client.getCharacter().getMaxHp());
				}else if(args[0].equalsIgnoreCase("!saveguilds")){
					MapleServer.getWorlds().forEach(world -> world.getGuilds().forEach(guild -> guild.saveGuild()));
				}else if(args[0].equalsIgnoreCase("!killall")){
					for(MapleMapObject obj : client.getCharacter().getMap().getObjects()){
						if(obj instanceof MapleMonster){
							MapleMonster monster = (MapleMonster) obj;
							monster.kill(client.getCharacter());
						}
					}
					/*
					MapleScript script = new MapleScript("scripts/npc/animal_rights_activist.js");
					
					client.getCharacter().openNpc(script, 1012111);
					*/
				}else if(args[0].equalsIgnoreCase("!npcscript")){
					String name = args[1];
					
					MapleScript script = new MapleScript("scripts/npc/"+name+".js");
					
					client.getCharacter().openNpc(script, 9010000);
					
				}else if(args[0].equalsIgnoreCase("!drop")){
					int id = Integer.parseInt(args[1]);
					int amount = Integer.parseInt(args[2]);
					
					Item item = ItemFactory.getItem(id, amount, client.getCharacter().getName());
					
					client.getCharacter().getMap().dropItem(item, client.getCharacter().getPosition(), null);	
				}else if(args[0].equalsIgnoreCase("!vac")){
					for(MapleMapObject obj : client.getCharacter().getMap().getObjects()){
						if(obj instanceof MapleMapItem){
							MapleMapItem monster = (MapleMapItem) obj;
							monster.pickup(client.getCharacter());
						}
					}
				}else if(args[0].equalsIgnoreCase("!buff")){
					
					Skill skill = SkillFactory.getSkill(Integer.parseInt(args[1]));
					
					if(skill != null){
						skill.getEffect(skill.getMaxLevel()).applyTo(client.getCharacter());
						client.getCharacter().sendMessage(MessageType.NOTICE, "Applied buff "+SkillFactory.getSkillName(Integer.parseInt(args[1])));
					}else{
						client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "Unknown buff id "+args[1]);
					}
				}else if(args[0].equalsIgnoreCase("!mountinfo")){
					MapleMount mount = client.getCharacter().getMount();
					if(mount == null){
						client.getCharacter().sendMessage(MessageType.POPUP, "You have no mount!");
					}else{
						client.getCharacter().sendMessage(MessageType.LIGHT_BLUE_TEXT, "Your mount type is "+mount.getMountType().name());
					}
				}else if(args[0].equalsIgnoreCase("!saveme")){
					
					client.getCharacter().saveToDatabase(false);
					client.getCharacter().sendMessage(MessageType.NOTICE, "Done!");
				}else if(args[0].equalsIgnoreCase("!note")){
					String note = StringUtil.joinStringFrom(args, 1);
					
				}else{
					client.getCharacter().sendMessage(MessageType.PINK_TEXT, "Unknown command!");
				}
				client.sendReallowActions();
				return;
			}
		}catch(Exception e){
			client.getCharacter().sendMessage(MessageType.NOTICE, e.toString());
			e.printStackTrace();
		}
		
		client.getCharacter().chat(text, buf.readByte());
	}

}
