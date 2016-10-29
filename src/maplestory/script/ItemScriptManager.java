package maplestory.script;

import java.io.IOException;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import maplestory.map.MapleMapItem;
import maplestory.player.MapleCharacter;

public class ItemScriptManager {

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
	
}
