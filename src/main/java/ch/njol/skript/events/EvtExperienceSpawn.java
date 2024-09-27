/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ExperienceSpawnEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EvtExperienceSpawn extends SkriptEvent {

	static {
		Skript.registerEvent("Experience Spawn", EvtExperienceSpawn.class, ExperienceSpawnEvent.class,
				"[e]xp[erience] [orb] spawn",
				"spawn of [a[n]] [e]xp[erience] [orb]"
			).description(
				"Called whenever experience is about to spawn.",
				"Please note that this event will not fire for xp orbs spawned by plugins (including Skript) with Bukkit."
			).examples(
				"on xp spawn:",
				"\tworld is \"minigame_world\"",
				"\tcancel event"
			).since("2.0");
		EventValues.registerEventValue(ExperienceSpawnEvent.class, Location.class, new Getter<Location, ExperienceSpawnEvent>() {
			@Override
			public Location get(ExperienceSpawnEvent event) {
				return event.getLocation();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(ExperienceSpawnEvent.class, Experience.class, new Getter<Experience, ExperienceSpawnEvent>() {
			@Override
			public Experience get(ExperienceSpawnEvent event) {
				return new Experience(event.getSpawnedXP());
			}
		}, EventValues.TIME_NOW);
	}

	private static final List<Trigger> TRIGGERS = Collections.synchronizedList(new ArrayList<>());

	private static final AtomicBoolean REGISTERED_EXECUTORS = new AtomicBoolean();

	private static final EventExecutor EXECUTOR = (listener, event) -> {
		ExperienceSpawnEvent experienceEvent;
		if (event instanceof BlockExpEvent blockExpEvent) {
			experienceEvent = new ExperienceSpawnEvent(
				blockExpEvent.getExpToDrop(),
				blockExpEvent.getBlock().getLocation().add(0.5, 0.5, 0.5)
			);
		} else if (event instanceof EntityDeathEvent deathEvent) {
			experienceEvent = new ExperienceSpawnEvent(
				deathEvent.getDroppedExp(),
				deathEvent.getEntity().getLocation()
			);
		} else if (event instanceof ExpBottleEvent bottleEvent) {
			experienceEvent = new ExperienceSpawnEvent(
				bottleEvent.getExperience(),
				bottleEvent.getEntity().getLocation()
			);
		} else if (event instanceof PlayerFishEvent fishEvent) {
			if (fishEvent.getState() != PlayerFishEvent.State.CAUGHT_FISH) // There is no EXP
				return;
			experienceEvent = new ExperienceSpawnEvent(
				fishEvent.getExpToDrop(),
				fishEvent.getPlayer().getLocation()
			);
		} else {
			assert false;
			return;
		}

		SkriptEventHandler.logEventStart(event);
		synchronized (TRIGGERS) {
			for (Trigger trigger : TRIGGERS) {
				SkriptEventHandler.logTriggerStart(trigger);
				trigger.execute(experienceEvent);
				SkriptEventHandler.logTriggerEnd(trigger);
			}
		}
		SkriptEventHandler.logEventEnd();

		if (experienceEvent.isCancelled())
			experienceEvent.setSpawnedXP(0);

		if (event instanceof BlockExpEvent blockExpEvent) {
			blockExpEvent.setExpToDrop(experienceEvent.getSpawnedXP());
		} else if (event instanceof EntityDeathEvent deathEvent) {
			deathEvent.setDroppedExp(experienceEvent.getSpawnedXP());
		} else if (event instanceof ExpBottleEvent bottleEvent) {
			bottleEvent.setExperience(experienceEvent.getSpawnedXP());
		} else if (event instanceof PlayerFishEvent fishEvent) {
			fishEvent.setExpToDrop(experienceEvent.getSpawnedXP());
		}
	};
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean postLoad() {
		TRIGGERS.add(trigger);
		if (REGISTERED_EXECUTORS.compareAndSet(false, true)) {
			EventPriority priority = SkriptConfig.defaultEventPriority.value();
			//noinspection unchecked
			for (Class<? extends Event> clazz : new Class[]{BlockExpEvent.class, EntityDeathEvent.class, ExpBottleEvent.class, PlayerFishEvent.class})
				Bukkit.getPluginManager().registerEvent(clazz, new Listener(){}, priority, EXECUTOR, Skript.getInstance(), true);
		}
		return true;
	}

	@Override
	public void unload() {
		TRIGGERS.remove(trigger);
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
		return "experience spawn";
	}
	
}
