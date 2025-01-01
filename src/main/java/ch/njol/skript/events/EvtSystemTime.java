package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EvtSystemTime extends SkriptEvent {

	public static class SystemTimeEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	private static final long HOUR_24_MILLISECONDS = 1000 * 60 * 60 * 24;
	private static final Timer timer;

	static {
		Skript.registerEvent("System Time", EvtSystemTime.class, SystemTimeEvent.class,
			"(system|real) time (of|at) %times%")
				.description("Called when the local time of the system reaches the provided time.")
				.examples(
					"on system time of 14:20:",
					"on real time at 2:30am:",
					"on system time at 6:10 pm:",
					"on real time of 5:00 am and 5:00 pm:",
					"on system time of 5:00 and 17:00:"
				)
				.since("INSERT VERSION");

		timer = new Timer("EvtSystemTime-Tasks");
	}

	private Literal<Time> times;
	private boolean unloaded = false;
	private final List<SystemTimeInfo> infoList = new ArrayList<>();

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		times = (Literal<Time>) args[0];
		return true;
	}

	@Override
	public boolean postLoad() {
		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTimeZone(TimeZone.getDefault());
		for (Time time : times.getArray()) {
			Calendar expectedCalendar = Calendar.getInstance();
			expectedCalendar.setTimeZone(TimeZone.getDefault());
			expectedCalendar.set(Calendar.MINUTE, time.getMinute());
			expectedCalendar.set(Calendar.SECOND, 0);
			expectedCalendar.set(Calendar.MILLISECOND, 0);
			expectedCalendar.set(Calendar.HOUR_OF_DAY, time.getHour());
			// Ensure the execution time is in the future and not the past
			while (expectedCalendar.before(currentCalendar)) {
				expectedCalendar.add(Calendar.HOUR_OF_DAY, 24);
			}
			SystemTimeInfo info = new SystemTimeInfo(time, expectedCalendar.getTimeInMillis());
			infoList.add(info);
			createNewTask(info);
		}
		return true;
	}

	@Override
	public void unload() {
		unloaded = true;
		for (SystemTimeInfo info : infoList) {
			if (info.task != null)
				info.task.cancel();
		}
		timer.purge();
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
		return "system time of " + times.toString(event, debug);
	}

	private void execute() {
		SystemTimeEvent event = new SystemTimeEvent();
		SkriptEventHandler.logEventStart(event);
		SkriptEventHandler.logTriggerStart(trigger);
		trigger.execute(event);
		SkriptEventHandler.logTriggerEnd(trigger);
		SkriptEventHandler.logEventEnd();
	}

	private void preExecute(SystemTimeInfo info) {
		// Safety check, ensure this 'EvtServerTime' was not unloaded
		if (unloaded)
			return;
		// Bump the next execution time by the appropriate amount
		info.executionTime += HOUR_24_MILLISECONDS;
		// Reschedule task for new executionTime
		createNewTask(info);
		// Activate trigger
		execute();
	}

	private void createNewTask(SystemTimeInfo info) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				preExecute(info);
			}
		};
		info.task = task;
		timer.schedule(task, new Date(info.executionTime));
	}

	private static class SystemTimeInfo {
		private long executionTime;
		private final Time time;
		private TimerTask task;

		public SystemTimeInfo(Time time, long executionTime) {
			this.time = time;
			this.executionTime = executionTime;
		}

	}

}
