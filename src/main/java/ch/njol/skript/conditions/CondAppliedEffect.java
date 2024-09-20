package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Applied Effect")
@Description("Checks to see if the applied effect of a beacon effect event is primary or secondary")
@Examples({
	"on beacon effect:",
		"\tif applied effect is primary:",
			"\t\tbroadcast \"Is Primary\"",
		"\telse if applied effect is secondary:",
			"\t\tbroadcast \"Is Secondary\""
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class CondAppliedEffect extends Condition {

	static {
		Skript.registerCondition(CondAppliedEffect.class,
			"[the] applied effect is [the] (primary|:secondary) [[potion] effect]",
			"[the] applied effect (isn't|is not) [the] (primary|:secondary) [[potion] effect]"
			);
	}

	private boolean checkPrimary;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BeaconEffectEvent.class)) {
			Skript.error("This condition can only be used in an on beacon effect event.");
			return false;
		}
		checkPrimary = !parseResult.hasTag("secondary");
		if (matchedPattern == 1)
			setNegated(true);
		return true;
	}

	@Override
	public boolean check(Event event) {
		BeaconEffectEvent beaconEffectEvent = (BeaconEffectEvent) event;
		boolean isPrimary = beaconEffectEvent.isPrimary();
		return isPrimary == checkPrimary;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "applied effect is " + (isNegated() ? "not" : "") + (checkPrimary ? "primary" : "secondary");
	}

}
