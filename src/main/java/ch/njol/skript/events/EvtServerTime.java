package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class EvtServerTime extends SkriptEvent {

	public static class ServerTimeEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	private enum TimeState {
		ANY, AM, PM
	}

	private static final TimeState[] TIME_STATES = TimeState.values();
	private static final int HOUR_12_SECONDS = 43200;
	private static final int HOUR_24_SECONDS = HOUR_12_SECONDS * 2;

	static {
		Skript.registerEvent("Server Time", EvtServerTime.class, ServerTimeEvent.class,
			"(server|real) time (of|at) %integer%.%integer%",
			"(server|real) time (of|at) %integer%.%integer%[ ]am",
			"(server|real) time (of|at) %integer%.%integer%[ ]pm")
				.description()
				.examples()
				.since("INSERT VERSION");
	}

	private TimeState state;
	private int hour;
	private int minute;
	private long nextTime;
	private int currentTask;
	private boolean unloaded = false;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		state = TIME_STATES[matchedPattern];
		//noinspection unchecked
		int hour = ((Literal<Integer>) args[0]).getSingle();
		if (hour < 1 || hour > 12) {
			Skript.error("The hour can not be below 1 or above 12.");
			return false;
		}
		//noinspection unchecked
		int minute = ((Literal<Integer>) args[1]).getSingle();
		if (minute < 0 || minute > 60) {
			Skript.adminBroadcast("The minute can not be below 0 or above 60.");
		} else if (minute == 60) {
			minute = 0;
			hour++;
		}
		this.hour = hour;
		this.minute = minute;
		return true;
	}

	@Override
	public boolean postLoad() {
		updateScheduler(false);
		return true;
	}

	@Override
	public void unload() {
		unloaded = true;
		Bukkit.getScheduler().cancelTask(currentTask);
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

	private void updateScheduler(boolean executed) {
		if (unloaded)
			return;
		long ticks;
		if (executed) {
			if (state == TimeState.ANY) {
				nextTime += HOUR_12_SECONDS;
				ticks = HOUR_12_SECONDS * 20;
			} else {
				nextTime += HOUR_24_SECONDS;
				ticks = HOUR_24_SECONDS * 20;
			}
		} else {
			ticks = ticksTilTime();
		}
		Skript.adminBroadcast("[" + state.toString() + "] Ticks until: " + ticks);
		currentTask = Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), this::checkExecute, ticks);
	}

	private long ticksTilTime() {
		LocalDateTime currentTime = LocalDateTime.now();
		boolean isPM = currentTime.getHour() >= 11;
		LocalDateTime expectedTime = currentTime.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
		Skript.adminBroadcast("[" + state.toString() + "] Current Time: " + currentTime);
		Skript.adminBroadcast("[" + state.toString() + "] Expected Time: " + expectedTime);
		if (state != TimeState.ANY) {
			boolean expectedPM = state == TimeState.PM;
			if (expectedPM != isPM)
				expectedTime = expectedTime.plusHours(12);
		}
		long currentEpoch = currentTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(currentTime));
		long expectedEpoch = expectedTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(expectedTime));
		(new Date()).toInstant().
		long diff = Math.abs(currentEpoch - expectedEpoch);
		nextTime = expectedEpoch;
		return (diff - 1) * 20;
	}

	private void checkExecute() {
		if (unloaded)
			return;
		LocalDateTime currentTime = LocalDateTime.now();
		long currentEpoch = currentTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(currentTime));
		if (Math.abs(nextTime - currentEpoch) <= 10) {
			execute();
		} else {
			updateScheduler(false);
		}
	}

	private void execute() {
		ServerTimeEvent event = new ServerTimeEvent();
		SkriptEventHandler.logEventStart(event);
		SkriptEventHandler.logTriggerStart(trigger);
		trigger.execute(event);
		SkriptEventHandler.logTriggerEnd(trigger);
		SkriptEventHandler.logEventEnd();
		updateScheduler(true);
	}

}
