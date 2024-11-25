package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.jetbrains.annotations.Nullable;

public class EvtRaids extends SkriptEvent {

	static {
		Skript.registerEvent("Raid Finish", EvtRaids.class, RaidFinishEvent.class, "raid finish[ed]")
			.description("Called when players beat a raid.")
			.examples(
				"on raid finish:",
					"\tgive 5 diamonds to event-players",
				"on raid finished:")
			.since("INSERT VERSION");

		Skript.registerEvent("Raid Trigger", EvtRaids.class, RaidTriggerEvent.class, "raid trigger[ed]")
			.description("Called when a raid is triggered by a player with bad omen.")
			.examples(
				"on raid trigger:",
					"\tbroadcast \"%event-player%\" has activated a raid.",
				"on raid triggered:"
			)
			.since("INSERT VERSION");

		Skript.registerEvent("Raid Wave Spawn", EvtRaids.class, RaidSpawnWaveEvent.class, "raid wave spawn[ed]", "raid spawn[ed] wave")
			.description("Called when a new wave of a raid is spawned.")
			.examples(
				"on raid wave spawned:",
					"\tbroadcast event-entities",
				"on raid spawn wave:"
			)
			.since("INSERT VERSION");

	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (event instanceof RaidFinishEvent) {
			return "raid finished";
		} else if (event instanceof RaidTriggerEvent) {
			return "raid triggered";
		} else if (event instanceof RaidSpawnWaveEvent) {
			return "raid wave spawned";
		}
		return "";
	}

}
