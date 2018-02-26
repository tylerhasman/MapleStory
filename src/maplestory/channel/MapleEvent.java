package maplestory.channel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import maplestory.script.EventScriptManager;
import maplestory.script.MapleScript;
import maplestory.script.MapleScriptInstance;

public class MapleEvent {

	private MapleScriptInstance instance;
	
	private String path;
	
	private EventScriptManager manager;
	
	private Map<String, Object> properties;
	
	public MapleEvent(MapleChannel channel, String script) {
		MapleScript sc = new MapleScript(script);
		properties = new HashMap<>();
		manager = new EventScriptManager(channel, this);
		path = script;
		
		if(!sc.isDisabled()) {
			SimpleBindings sb = new SimpleBindings();
			sb.put("em", manager);
			try {
				instance = sc.execute(sb);
				instance.function("onLoad");
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
			}
		}
	}
	
	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	public Object getProperty(String key, Object def) {
		return properties.getOrDefault(key, def);
	}
	
	public void setProperty(String key, Object val) {
		properties.put(key, val);
	}
	
	public void cancelAllEvents() {
		manager.cancelAllTasks();
	}
	
	public String getEventId() {
		try {
			Object name = instance.function("get_event_name");
			
			if(name == null) {
				return path;
			}
			
			return name.toString();
		} catch (NoSuchMethodException e) {
			return "MissingNameFunction "+path;
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return path;
	}
	
	public MapleScriptInstance getScriptInstance() {
		return instance;
	}
	
}
