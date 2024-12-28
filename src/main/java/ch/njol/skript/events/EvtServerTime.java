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

import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.TimeZone;

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
		if (time.getTimeState() == TimeState.AM) {
			adjustedHour += 12;
		} else if (time.getTimeState() == TimeState.PM) {
			adjustedHour -= 12;
		}
		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTimeZone(TimeZone.getDefault());
		long currentEpoch = currentCalendar.getTimeInMillis();
		Calendar expectedCalendar = Calendar.getInstance();
		expectedCalendar.setTimeZone(TimeZone.getDefault());
		expectedCalendar.set(Calendar.HOUR, adjustedHour);
		expectedCalendar.set(Calendar.MINUTE, time.getMinute());
		expectedCalendar.set(Calendar.SECOND, 0);
		expectedCalendar.set(Calendar.MILLISECOND, 0);
		long expectedEpoch = expectedCalendar.getTimeInMillis();
		Skript.adminBroadcast("--------------------------------------");
		Skript.adminBroadcast("[" + time + "] Prior: " + expectedEpoch + " | " + expectedCalendar.getTime());
		if (expectedEpoch < currentEpoch) {
			if (time.getTimeState() == TimeState.ANY) {
				expectedCalendar.add(Calendar.HOUR, 12);
			} else {
				expectedCalendar.add(Calendar.HOUR, 24);
			}
			expectedEpoch = expectedCalendar.getTimeInMillis();
		}
		Skript.adminBroadcast("[" + time + "] Current Time: " + currentEpoch + " | " + currentCalendar.getTime());
		Skript.adminBroadcast("[" + time + "] Expected Time: " + expectedEpoch + " | " + expectedCalendar.getTime());
		executionTime = expectedEpoch;
		instances.add(this);
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
		Skript.adminBroadcast("Executing");
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

	static {
		createThread();
	}

	private static void createThread() {
		if (thread == null) {
			thread = new Thread(EvtServerTime::run);
			thread.start();
		} else if (!thread.isAlive()) {
			thread.start();
		}
	}

	private static void stopThread() {
		if (thread != null)
			thread.interrupt();
	}

	private static void run() {
		long defaultWait = 60000;
		Skript.adminBroadcast("Thread Started");
		while (true) {
			long currentWait = defaultWait;
			if (!instances.isEmpty()) {
				EvtServerTime evtServerTime = instances.peek();
				long currentTime = (new Date()).getTime();
				if (currentTime >= evtServerTime.executionTime) {
					evtServerTime.execute();
					Skript.adminBroadcast("Before: " + evtServerTime.executionTime);
					if (evtServerTime.time.getTimeState() == TimeState.ANY) {
						evtServerTime.executionTime += HOUR_12_MILLISECONDS;
					} else {
						evtServerTime.executionTime += HOUR_24_MILLISECONDS;
					}
					Skript.adminBroadcast("After: " + evtServerTime.executionTime);
					if (instances.size() > 1)
						currentWait = 0;
				} else if ((evtServerTime.executionTime - currentTime) < defaultWait) {
					currentWait = evtServerTime.executionTime - currentTime;
				}
			}
			Skript.adminBroadcast("Sleeping: " + currentWait);
			try {
				//noinspection BusyWait
				Thread.sleep(currentWait);
			} catch (InterruptedException ignored) {}
		}
	}

}
