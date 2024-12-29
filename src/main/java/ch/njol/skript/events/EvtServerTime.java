package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Time.TimeState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EvtServerTime extends SkriptEvent implements Comparable<EvtServerTime> {

	public static class ServerTimeEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	private static final long HOUR_12_MILLISECONDS = 43200000;
	private static final long HOUR_24_MILLISECONDS = HOUR_12_MILLISECONDS * 2;
	private static final PriorityQueue<EvtServerTime> instances = new PriorityQueue<>(EvtServerTime::compareTo);

	static {
		Skript.registerEvent("Server Time", EvtServerTime.class, ServerTimeEvent.class,
			"(server|real) time (of|at) %time%")
				.description()
				.examples()
				.since("INSERT VERSION");
	}

	private Time time;
	private long executionTime;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		time = ((Literal<Time>) args[0]).getSingle();
		return true;
	}

	@Override
	public boolean postLoad() {
		int adjustedHour = time.getHour();
		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTimeZone(TimeZone.getDefault());
		long currentEpoch = currentCalendar.getTimeInMillis();
		Calendar expectedCalendar = Calendar.getInstance();
		expectedCalendar.setTimeZone(TimeZone.getDefault());
		expectedCalendar.set(Calendar.MINUTE, time.getMinute());
		expectedCalendar.set(Calendar.SECOND, 0);
		expectedCalendar.set(Calendar.MILLISECOND, 0);
		expectedCalendar.set(Calendar.HOUR_OF_DAY, adjustedHour);
		while (expectedCalendar.before(currentCalendar)) {
			if (time.getTimeState() == TimeState.ANY) {
				expectedCalendar.add(Calendar.HOUR_OF_DAY, 12);
			} else {
				expectedCalendar.add(Calendar.HOUR_OF_DAY, 24);
			}
		}
		long expectedEpoch = expectedCalendar.getTimeInMillis();
		Skript.adminBroadcast("-----------------------------");
		Skript.adminBroadcast("[" + time + "] Current Time: " + currentEpoch + " | " + currentCalendar.getTime());
		Skript.adminBroadcast("[" + time + "] Expected Time: " + expectedEpoch + " | " + expectedCalendar.getTime());
		executionTime = expectedEpoch;
		instances.add(this);
		// We must refresh the thread to ensure this newly added 'EvtServerTime' gets executed on time if the specified time is within the next minute
		// Including adding a new 'EvtServerTime' to a file and reloading
		refreshThread();
		return true;
	}

	@Override
	public void unload() {
		instances.remove(this);
		if (instances.isEmpty())
			stopThread();
	}

	@Override
	public boolean check(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

	private void execute() {
		ServerTimeEvent event = new ServerTimeEvent();
		SkriptEventHandler.logEventStart(event);
		SkriptEventHandler.logTriggerStart(trigger);
		trigger.execute(event);
		SkriptEventHandler.logTriggerEnd(trigger);
		SkriptEventHandler.logEventEnd();
	}

	@Override
	public int compareTo(@Nullable EvtServerTime event) {
		return (int) (event == null ? executionTime : executionTime - event.executionTime);
	}

	private static Thread thread;
	private static volatile boolean running;

	/**
	 * Starts a new thread designed to monitor the system time to ensure accurate execution of {@link EvtServerTime}.
	 */
	private static void startThread() {
		running = true;
		if (thread == null || !thread.isAlive()) {
			thread = null;
			thread = new Thread(EvtServerTime::run);
			thread.start();
		}
	}

	/**
	 * Stops the thread to ensure it does not continuously run when not needed.
	 */
	private static void stopThread() {
		running = false;
		if (thread != null)
			thread.interrupt();
	}

	/**
	 * Refreshes the thread by interrupting it, ensuring any newly added {@link EvtServerTime} is executed accurately
	 */
	private static void refreshThread() {
		if (thread == null || !thread.isAlive()) {
			startThread();
		} else {
			thread.interrupt();
		}
	}

	/**
	 * Should never be called outside of this class.
	 * Method used within a new thread to monitor the system time and accurately execute a {@link EvtServerTime}.
	 */
	private static void run() {
		if (thread == null || Thread.currentThread() != thread)
			return;
		// We want to wait atleast 1 minute between each interval.
		long defaultWait = 60000;
		while (running) {
			long currentWait = defaultWait;
			if (!instances.isEmpty()) {
				// Gets the 'EvtServerTime' closest to being executed first
				EvtServerTime evtServerTime = instances.peek();
				long currentTime = (new Date()).getTime();
				// If the current system time is equal to or passed the execution time of the 'EvtServerTime'
				if (currentTime >= evtServerTime.executionTime) {
					// Execute the 'EvtServerTime' to run code inside.
					evtServerTime.execute();
					// If the user specified PM or AM, adds 24 hours worth of milliseconds to the execution time of the 'EvtServerTime'
					if (evtServerTime.time.getTimeState() == TimeState.ANY) {
						evtServerTime.executionTime += HOUR_12_MILLISECONDS;
					} else {
						evtServerTime.executionTime += HOUR_24_MILLISECONDS;
					}
					// Must remove and readd the current 'EvtServerTime' to refresh the placement of the PriorityQueue
					instances.remove(evtServerTime);
					instances.offer(evtServerTime);
					// If there are multiple 'EvtServerTime' , need to ensure the next one needs to be executed if designated at the same time or close to
					if (instances.size() > 1)
						currentWait = 0;
				} else if ((evtServerTime.executionTime - currentTime) < defaultWait) {
					// The current 'EvtServerTime' is not ready but the time until is less than the default wait
					// This allows the 'EvtServerTime' to be called on time
					currentWait = evtServerTime.executionTime - currentTime;
				}
			}
			try {
				//noinspection BusyWait
				Thread.sleep(currentWait);
			} catch (InterruptedException ignored) {}
		}
	}

}
