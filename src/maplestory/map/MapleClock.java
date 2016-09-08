package maplestory.map;

import java.util.concurrent.TimeUnit;

import tools.TimerManager.MapleTask;

public class MapleClock {
	
	private long startTime;
	private int seconds;
	private MapleTask finishTask;
	
	public MapleClock(int seconds, MapleTask finishTask) {
		startTime = System.currentTimeMillis();
		this.seconds = seconds;
		this.finishTask = finishTask;
	}
	
	public void destroy(){
		finishTask.cancel(false);
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getEndTime() {
		return startTime + seconds * 1000;
	}
	
	public int getSecondsLeft(){
		return (int) (seconds - (TimeUnit.SECONDS.convert(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)));
	}
	
}
