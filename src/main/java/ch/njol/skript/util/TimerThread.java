package ch.njol.skript.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TimerThread {

	private static final TimerThread instance = new TimerThread();
	private static Map<Integer, TimerTask> scheduledTasks = new ConcurrentHashMap<>();
	private static int totalTasks = 0;
	private final Thread timerThread;
	private volatile boolean running = true;

	private TimerThread() {
		timerThread = new Thread(this::run);
		timerThread.start();
	}

	public static TimerThread getInstance() {
		return instance;
	}

	private void run() {
		long defaultWait = 60000;
		while (running) {
			try {
				long lowestWait = defaultWait;
				List<Integer> completed = new ArrayList<>();
				long currentTime = System.currentTimeMillis();
				Set<Integer> taskIds = scheduledTasks.keySet();
				for (Integer identity : taskIds) {
					TimerTask task = scheduledTasks.get(identity);
					if (task.cancelled)
						continue;
					if (task.executionTime > currentTime) {
						long taskWait = task.executionTime - currentTime;
						if (taskWait < lowestWait)
							lowestWait = taskWait;
					} else {
						// Execute Task
						if (!task.repeatable)
							completed.add(identity);
					}
				}
				completed.forEach(identity -> {
					TimerTask task = scheduledTasks.get(identity);
					task.completed = true;
					scheduledTasks.remove(identity, task);
				});
				//noinspection BusyWait
				Thread.sleep(lowestWait);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	public int scheduleTask(Runnable task, long delay) {
		return scheduleTask(task, delay, TimeUnit.SECONDS);
	}

	public int scheduleTask(Runnable task, long delay, TimeUnit unit) {
		totalTasks++;
		int identity = totalTasks;
		long executionTime = System.currentTimeMillis() + unit.toMillis(delay);
		TimerTask timerTask = new TimerTask(identity, executionTime, task);
		scheduledTasks.put(identity, timerTask);
		return identity;
	}

	public int scheduleRepeatingTask(Runnable task, long delay) {
		return scheduleRepeatingTask(task, delay, TimeUnit.SECONDS);
	}

	public int scheduleRepeatingTask(Runnable task, long delay, TimeUnit unit) {
		totalTasks++;
		int identity = totalTasks;
		long executionTime = System.currentTimeMillis() + unit.toMillis(delay);
		TimerTask timerTask = new TimerTask(identity, executionTime, task, true);
		scheduledTasks.put(identity, timerTask);
		return identity;
	}

	public boolean taskExists(int identity) {
		return scheduledTasks.containsKey(identity);
	}

	public boolean cancelTask(int identity) {
		TimerTask timerTask = scheduledTasks.get(identity);
		if (timerTask != null) {
			timerTask.cancelled = true;
			scheduledTasks.remove(identity, timerTask);
			return true;
		}
		return false;
	}

	public void shutdown() {
		running = false;
		timerThread.interrupt();
	}

	private static final class TimerTask {

		final int identity;
		final long executionTime;
		final Runnable task;
		volatile boolean cancelled = false;
		volatile boolean completed = false;
		final boolean repeatable;

		public TimerTask(int identity, long executionTime, Runnable task) {
			this(identity, executionTime, task, false);
		}

		public TimerTask(int identity, long executionTime, Runnable task, boolean repeatable) {
			this.identity = identity;
			this.executionTime = executionTime;
			this.task = task;
			this.repeatable = repeatable;
		}

	}

}
