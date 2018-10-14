package maplestory.script;

import java.io.IOException;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import maplestory.inventory.item.EquipItem;
import maplestory.inventory.item.EquipItemInfo;
import maplestory.inventory.item.Item;
import maplestory.map.MapleMapItem;
import maplestory.player.MapleCharacter;

public class ItemScriptManager {

	public static void onEquipItem(MapleCharacter chr, EquipItem item) {
		String scriptName = "scripts/item/"+item.getItemId()+".js";
		
		MapleScript script = new MapleScript(scriptName);
		
		if(script.isDisabled()) {
			return;
		}
		
		try {
			SimpleBindings sb = new SimpleBindings();
			sb.put("im", new AbstractScriptManager(chr));
			
			MapleScriptInstance inst = script.execute(sb);

			inst.equipItem(chr, item);
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		}
	}
	
	public static void onItemPickup(MapleCharacter chr, MapleMapItem item){
		
		String scriptName = "scripts/item/";
		
		if(item.getItemId() / 10000 == 238){
			scriptName += "monsterBookCard.js";
		}else{
			chr.getClient().getLogger().warn("Missing item script for item "+item.getItemId());
			return;
		}
		
		MapleScript script = new MapleScript(scriptName);
		
		try {
			MapleScriptInstance inst = script.execute(new SimpleBindings());
			inst.itemPickup(chr, item);
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		}
		
	}

	public static void onUnEquipItem(MapleCharacter chr, EquipItem item) {
		String scriptName = "scripts/item/"+item.getItemId()+".js";
		
		MapleScript script = new MapleScript(scriptName);
		
		if(script.isDisabled()) {
			return;
		}
		
		try {
			MapleScriptInstance inst = script.execute(new SimpleBindings());

			inst.unEquipItem(chr, item);
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		}
	}

	public static int onGiveExp(MapleCharacter mapleCharacter, int exp, Item item) {
		String scriptName = "scripts/item/"+item.getItemId()+".js";
		
		MapleScript script = new MapleScript(scriptName);
		
		if(script.isDisabled()) {
			return exp;
		}
		
		try {
			MapleScriptInstance inst = script.execute(new SimpleBindings());

			return inst.giveExp(mapleCharacter, exp);
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		}
		return exp;
	}
	
}
