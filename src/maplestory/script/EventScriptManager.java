package maplestory.script;

import java.util.ArrayList;
import java.util.List;

import maplestory.channel.MapleChannel;
import maplestory.channel.MapleEvent;
import tools.TimerManager;
import tools.TimerManager.MapleTask;

public class EventScriptManager {
	
	private MapleChannel channel;
	
	private List<MapleTask> tasks;
	
	private MapleEvent event;
	
	public EventScriptManager(MapleChannel channel, MapleEvent mapleEvent) {
		this.channel = channel;
		event = mapleEvent;
		tasks = new ArrayList<>();
	}
	
	public void setProperty(String key, Object val) {
		event.setProperty(key, val);
	}
	
	public MapleChannel getChannel() {
		return channel;
	}
	
	public void cancelAllTasks() {
		tasks.forEach(task -> task.cancel(true));
		tasks.clear();
	}
	
	public MapleTask scheduleTask(Runnable task, int delay) {
		MapleTask t = TimerManager.schedule(task, delay);
		tasks.add(t);
		
		return t;
	}
	
}
