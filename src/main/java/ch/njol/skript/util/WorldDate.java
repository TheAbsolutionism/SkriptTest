package ch.njol.skript.util;

import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to get the minecraft date of a world
 */
public class WorldDate implements YggdrasilSerializable {

	private enum TimeReference {
		DAY("Days", DAY_TICKS),
		HOUR("Hours", HOUR_TICKS),
		MIN("Minutes", MIN_TICKS),
		SEC("Seconds", SEC_TICKS),
		TICK("Ticks", 1);

		private double time;
		private String reference;

		TimeReference(String reference, double time) {
			this.reference = reference;
			this.time = time;
		}

	}

	private static final double DAY_TICKS = 24000;
	private static final double HOUR_TICKS = DAY_TICKS / 24;
	private static final double MIN_TICKS = HOUR_TICKS / 60;
	private static final double SEC_TICKS = MIN_TICKS / 60;

	private final World world;
	private long totalTicks;

	public WorldDate() {
		this(Bukkit.getWorlds().get(0));
	}

	public WorldDate(World world) {
		this.world = world;
		this.totalTicks = world.getGameTime();
	}

	public WorldDate(World world, long totalTicks) {
		this.world = world;
		if (totalTicks < 0)
			totalTicks = 0;
		this.totalTicks = totalTicks;
	}

	public WorldDate(Entity entity) {
		this(entity.getWorld());
	}

	public WorldDate(Entity entity, long totalTicks) {
		this(entity.getWorld(), totalTicks);
	}

	public World getWorld() {
		return world;
	}

	public long getTotalTicks() {
		return totalTicks;
	}

	public void subtract(Timespan timespan) {
		totalTicks -= timespan.getAs(TimePeriod.TICK);
		if (totalTicks < 0)
			totalTicks = 0;
	}

	public void add(Timespan timespan) {
		totalTicks += timespan.getAs(TimePeriod.TICK);
	}

	public WorldDate minus(Timespan timespan) {
		return new WorldDate(world, totalTicks - timespan.getAs(TimePeriod.TICK));
	}

	public WorldDate plus(Timespan timespan) {
		return new WorldDate(world, totalTicks + timespan.getAs(TimePeriod.TICK));
	}

	private long getFromTimeReference(TimeReference timeReference) {
		return (long) Math.floor(totalTicks / timeReference.time);
	}

	/**
	 * Get only the total number of days that have passed
	 * @return total number of days
	 */
	public long getAsDays() {
		return getFromTimeReference(TimeReference.DAY);
	}

	/**
	 * Get only the total number of hours that have passed
	 * @return total number of hours
	 */
	public long getAsHours() {
		return getFromTimeReference(TimeReference.HOUR);
	}

	/**
	 * Get only the total number of minutes that have passed
	 * @return total number of minutes
	 */
	public long getAsMins() {
		return getFromTimeReference(TimeReference.MIN);
	}

	/**
	 * Get only the total number of seconds that have passed
	 * @return total number of seconds
	 */
	public long getAsSecs() {
		return getFromTimeReference(TimeReference.SEC);
	}

	public int compareTo(@Nullable WorldDate other) {
		long time = other == null ? totalTicks : totalTicks - other.totalTicks;
		return time < 0 ? -1 : time > 0 ? 1 : 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof WorldDate other))
			return false;
		return totalTicks == other.totalTicks;
	}

	@Override
	public String toString() {
		long remain = totalTicks;
		Map<TimeReference, Long> timeData = new HashMap<>();
		for (TimeReference timeReference : TimeReference.values()) {
			long current = 0;
			if (remain > timeReference.time) {
				current = (long) Math.floor(remain / timeReference.time);
				remain -= (long) (current * timeReference.time);
			}
			timeData.put(timeReference, current);
		}
		return String.format("Minecraft Day %d, %d:%d:%d:%d",
			timeData.get(TimeReference.DAY), timeData.get(TimeReference.HOUR), timeData.get(TimeReference.MIN),
			timeData.get(TimeReference.SEC), timeData.get(TimeReference.TICK));
	}

}
