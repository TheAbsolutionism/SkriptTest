package org.skriptlang.skript.bukkit.raids;

import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Raider;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MutableRaid implements Raid {

	private boolean started = false;
	private Date startTime;
	private int badOmenLevel;
	private Location location;
	private int spawnedGroups = 0;
	private int totalGroups = 0;
	private int totalWaves = 0;

	public MutableRaid() {}

	public MutableRaid(Location location) {
		this.location = location;
		badOmenLevel = 1;
		totalGroups = 5;
		totalWaves = 5;
	}

	// --- GENERAL --- //

	public void startRaid() {
		started = true;
		startTime = new Date();
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	// --- GETTERS --- //

	@Override
	public long getActiveTicks() {
		return startTime.getTime();
	}

	@Override
	public int getBadOmenLevel() {
		return badOmenLevel;
	}

	@Override
	public @NotNull Location getLocation() {
		return location;
	}

	@NotNull
	@Override
	public RaidStatus getStatus() {
		return null;
	}

	@Override
	public int getSpawnedGroups() {
		return spawnedGroups;
	}

	@Override
	public int getTotalGroups() {
		return totalGroups;
	}

	@Override
	public int getTotalWaves() {
		return totalWaves;
	}

	@Override
	public float getTotalHealth() {
		return 0;
	}

	@Override
	public @NotNull Set<UUID> getHeroes() {
		return null;
	}

	@Override
	public @NotNull List<Raider> getRaiders() {
		return null;
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public @NotNull BossBar getBossBar() {
		return null;
	}

	@Override
	public @NotNull PersistentDataContainer getPersistentDataContainer() {
		return null;
	}

	// --- SETTERS --- //
	@Override
	public void setBadOmenLevel(int badOmenLevel) {
		this.badOmenLevel = badOmenLevel;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setTotalGroups(int totalGroups) {
		this.totalGroups = totalGroups;
	}

	public void setTotalWaves(int totalWaves) {
		this.totalWaves = totalWaves;
	}

}
