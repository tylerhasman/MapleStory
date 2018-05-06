package tools;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

public class TimerManager {

	private static final AtomicInteger num = new AtomicInteger(0);
	private static TimerManager instance = new TimerManager();
	
	private ScheduledThreadPoolExecutor executor;
	
	private static final Logger logger = LoggerFactory.getLogger("TimerManager");
	
	public TimerManager() {
		executor = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable runnable) {

				String name = "TimerManager-Thread-"+num.incrementAndGet();

				Thread thread = new Thread(runnable);
				thread.setName(name);
				
				return thread;
			}
			
		});
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		executor.setRemoveOnCancelPolicy(true);
		
		executor.setKeepAliveTime(5, TimeUnit.MINUTES);
        executor.allowCoreThreadTimeOut(true);
        executor.setMaximumPoolSize(15);
        
	}
	
	public static MapleTask schedule(Runnable runnable, long delay, TimeUnit unit){
		
		MapleTask task = new MapleTask(t -> runnable.run(), Thread.currentThread().getStackTrace());
		
		ScheduledFuture<?> future = instance.executor.schedule(task, delay, unit);
		
		task.setFuture(future);
		
		return task;
	}
	
	public static MapleTask scheduleRepeatingTask(Runnable runnable, long delay, long timeBetweenExecution, TimeUnit unit){
		MapleTask task = new MapleTask(t -> runnable.run(), Thread.currentThread().getStackTrace());
		
		ScheduledFuture<?> future = instance.executor.scheduleAtFixedRate(task, delay, timeBetweenExecution, unit);
		
		task.setFuture(future);
		
		return task;
	}
	
	public static MapleTask scheduleRepeatingTask(Consumer<MapleTask> runnable, long delay, long timeBetweenExecution, TimeUnit unit){
		MapleTask task = new MapleTask(runnable, Thread.currentThread().getStackTrace());
		
		ScheduledFuture<?> future = instance.executor.scheduleAtFixedRate(task, delay, timeBetweenExecution, unit);
		
		task.setFuture(future);
		
		return task;
	}
	
	public static void shutdown(){
		instance.executor.shutdownNow();
	}

	public static MapleTask schedule(Runnable runnable, long delay) {
		return schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}
	
	public static MapleTask scheduleRepeatingTask(Runnable runnable, long delay, long interval) {
		return scheduleRepeatingTask(runnable, delay, interval, TimeUnit.MILLISECONDS);
	}
	
	public static class MapleTask implements Runnable {
		
		@Getter
		private StackTraceElement[] sourceStack;
		@Getter
		private long creationTime;
		@Getter
		private long timesExecuted;
		@Getter
		private Consumer<MapleTask> task;
		
		private ScheduledFuture<?> future;
		
		MapleTask(Consumer<MapleTask> task, StackTraceElement[] source) {
			sourceStack = source;
			this.task = task;
			timesExecuted = 0;
			creationTime = System.currentTimeMillis();
		}
		
		private void setFuture(ScheduledFuture<?> future){
			this.future = future;
		}
		
		public boolean cancel(boolean interuptIfRunning){
			return future.cancel(interuptIfRunning);
		}
		
		@Override
		public void run() {
			
			try{
				task.accept(this);
			}catch(Exception e){
				logger.error("Error with task! Error below");
				e.printStackTrace();
				logger.error("Error source below");
				for(StackTraceElement ele : sourceStack){
					if(ele.getFileName() != null){
						System.out.println(ele.toString());
					}
				}
			}
			
			timesExecuted++;
			
		}

		public boolean isDone() {
			return future.isDone();
		}
		
	}
	
}
