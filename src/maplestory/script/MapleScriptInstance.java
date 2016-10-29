package maplestory.script;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import lombok.Getter;
import lombok.Setter;
import maplestory.map.MapleMapItem;
import maplestory.player.MapleCharacter;

public class MapleScriptInstance {

	private Invocable instance;
	@Setter @Getter
	private boolean isQuestEnd;
	
	protected MapleScriptInstance(Invocable instance){
		this.instance = instance;
	}
	
	public void startNpc() throws NoSuchMethodException, ScriptException{
		function("start");
	}
	
	public void action(int mode, int type, int selection) throws NoSuchMethodException, ScriptException{
		function("action", mode, type, selection);
	}
	
	public void startPortal() throws NoSuchMethodException, ScriptException{
		function("enter");
	}
	
	
	public void questStart(int mode, int type, int selection) throws NoSuchMethodException, ScriptException{
		function("start", mode, type, selection);
	}
	
	public void questEnd(int mode, int type, int selection) throws NoSuchMethodException, ScriptException{
		function("end", mode, type, selection);
	}
	
	public void setValue(String key, Object value){
		if(instance instanceof ScriptEngine){
			ScriptEngine engine = (ScriptEngine) instance;
			engine.put(key, value);
		}
	}
	
	public void questAction(int mode, int type, int selection) throws NoSuchMethodException, ScriptException{
		if(isQuestEnd){
			questEnd(mode, type, selection);
		}else{
			questStart(mode, type, selection);
		}
	}
	
	public void reactorDestroy() throws NoSuchMethodException, ScriptException{
		function("destroy");
	}
	
	public void function(String name, Object... args) throws NoSuchMethodException, ScriptException{
		instance.invokeFunction(name, args);
	}
	
	public void setVariable(String name, Object obj){
		if(instance instanceof ScriptEngine){
			ScriptEngine eng = (ScriptEngine) instance;
			
			eng.getBindings(ScriptContext.ENGINE_SCOPE).put(name, obj);
		}
	}

	public void itemPickup(MapleCharacter chr, MapleMapItem item) throws NoSuchMethodException, ScriptException {
		function("onPickup", chr, item);
	}

/*	public void onUserEnter(MapleCharacter mapleCharacter) throws NoSuchMethodException, ScriptException {
		function("onUserEnter", mapleCharacter);
	}*/
	
	
}
