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

public class EvtServerTime extends SkriptEvent {

	public static class ServerTimeEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	private static final long HOUR_12_MILLISECONDS = 43200000;
	private static final long HOUR_24_MILLISECONDS = HOUR_12_MILLISECONDS * 2;
	private static final Timer timer;

	static {
		Skript.registerEvent("Server Time", EvtServerTime.class, ServerTimeEvent.class,
			"(server|real) time (of|at) %time%")
				.description(
					"Called when the local time of the server reaches the provided time.",
					"Accepts 24 hour format, am/pm, and o'clock.",
					"Using o'clock, will be every 12 hours instead of every 24 as compared to 24 hour and am/pm."
				)
				.examples(
					"on server time of 14:20:",
					"on real time at 2:30am:",
					"on server time at 6:10 pm:",
					"on real time of 5:00 o'clock:",
						"\t# Will be called at 5 am and 5 pm / 5:00 and 17:00"
				)
				.since("INSERT VERSION");

		timer = new Timer("EvtServerTime-Tasks");
	}

	private Time time;
	private long executionTime;
	private TimerTask task;
	private boolean unloaded = false;

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
		Calendar expectedCalendar = Calendar.getInstance();
		expectedCalendar.setTimeZone(TimeZone.getDefault());
		expectedCalendar.set(Calendar.MINUTE, time.getMinute());
		expectedCalendar.set(Calendar.SECOND, 0);
		expectedCalendar.set(Calendar.MILLISECOND, 0);
		expectedCalendar.set(Calendar.HOUR_OF_DAY, adjustedHour);
		// Ensure the execution time is in the future and not the past
		while (expectedCalendar.before(currentCalendar)) {
			if (time.getTimeState() == TimeState.O_CLOCK) {
				expectedCalendar.add(Calendar.HOUR_OF_DAY, 12);
			} else {
				expectedCalendar.add(Calendar.HOUR_OF_DAY, 24);
			}
		}
        executionTime = expectedCalendar.getTimeInMillis();
		// Initial scheduling of this 'EvtServerTime'
		createNewTask();
		return true;
	}

	@Override
	public void unload() {
		unloaded = true;
		if (task != null) {
			task.cancel();
			timer.purge();
		}
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
		return "server time of " + time.toString();
	}

	private void execute() {
		ServerTimeEvent event = new ServerTimeEvent();
		SkriptEventHandler.logEventStart(event);
		SkriptEventHandler.logTriggerStart(trigger);
		trigger.execute(event);
		SkriptEventHandler.logTriggerEnd(trigger);
		SkriptEventHandler.logEventEnd();
	}

	private void preExecute() {
		// Safety check, ensure this 'EvtServerTime' was not unloaded
		if (unloaded)
			return;
		// Bump the next execution time by the appropriate amount
		if (time.getTimeState() == TimeState.O_CLOCK) {
			executionTime += HOUR_12_MILLISECONDS;
		} else {
			executionTime += HOUR_24_MILLISECONDS;
		}
		// Reschedule task for new executionTime
		createNewTask();
		// Activate trigger
		execute();
	}

	private void createNewTask() {
		task = new TimerTask() {
			@Override
			public void run() {
				preExecute();
			}
		};
		timer.schedule(task, new Date(executionTime));
	}

}
