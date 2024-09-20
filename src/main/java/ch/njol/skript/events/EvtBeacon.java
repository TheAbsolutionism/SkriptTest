package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import io.papermc.paper.event.block.BeaconActivatedEvent;
import io.papermc.paper.event.block.BeaconDeactivatedEvent;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EvtBeacon extends SkriptEvent {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent")) {
			Skript.registerEvent("Beacon Effect", EvtBeacon.class, BeaconEffectEvent.class, "beacon effect")
				.description("Called when a player gets an effect from a beacon")
				.examples(
					"on beacon effect:",
						"\tbroadcast event-potioneffecttype",
						"\tbroadcast event-player",
						"\tbroadcast event-block"
				)
				.since("INSERT VERSION")
				.requiredPlugins("Paper");
		}
		if (Skript.classExists("io.papermc.paper.event.block.BeaconActivatedEvent")) {
			Skript.registerEvent("Beacon Toggle", EvtBeacon.class, new Class[] {BeaconActivatedEvent.class, BeaconDeactivatedEvent.class}, "beacon [:de]activat(e|ion)")
				.description("Called when a beacon is activated or deactivated")
				.examples(
					"on beacon activate:",
						"\tbroadcast event-block"
				)
				.since("INSERT VERSION")
				.requiredPlugins("Paper");
		}
		if (Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent")) {
			Skript.registerEvent("Beacon Change Effect", EvtBeacon.class, PlayerChangeBeaconEffectEvent.class,
				"beacon change effect", "beacon effect change", "player chang(e[s]|ing) [of] beacon effect")
				.description("Called when a player changes the effects of a beacon")
				.examples(
					"on beacon effect change:",
						"\tbroadcast event-player",
						"\tbroadcast event-block",
						"\tbroadcast primary beacon effect",
						"\tbroadcast secondary beacon effect"
				)
				.since("INSERT VERSION")
				.requiredPlugins("Paper");
		}
	}

	private boolean isActivate;

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		isActivate = !parseResult.hasTag("de");
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (event instanceof BeaconActivatedEvent) {
			return isActivate;
		} else if (event instanceof BeaconDeactivatedEvent) {
			return !isActivate;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "beacon effect/activate/deactivate/change effect";
	}

}
