package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidStopEvent.Reason;
import org.jetbrains.annotations.Nullable;

public class EvtRaidStop extends SkriptEvent {

	static {
		Skript.registerEvent("Raid Stop", EvtRaidStop.class, RaidStopEvent.class,
			"raid stop [of %-raidstopreason%]")
			.description("Called when a raid stops.")
			.examples("on raid stop:", "on raid stop of timed out:")
			.since("INSERT VERSION");

	}

	private Reason reason;

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null)
			reason = (Reason) exprs[0].getSingle();
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof RaidStopEvent raidStopEvent))
			return false;
		if (reason != null && !raidStopEvent.getReason().equals(reason))
			return false;
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "raid stop";
	}

}
