package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Effect Primary")
@Description("Represents the value if the effect applied is the primary effect from a on beacon effect event.")
@Examples({
	"on beacon effect:",
		"\tbroadcast effect primary"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class ExprBeaconEffectPrimary extends SimpleExpression<Boolean> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent")) {
			Skript.registerExpression(ExprBeaconEffectPrimary.class, Boolean.class, ExpressionType.SIMPLE, "effect primary");
		}
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BeaconEffectEvent.class)) {
			Skript.error("The expression 'effect primary' can only be used in a on beacon effect event.");
			return false;
		}
		return true;
	}

	@Override
	protected Boolean @Nullable [] get(Event event) {
		if (event instanceof BeaconEffectEvent beaconEffectEvent) {
			return new Boolean[] {beaconEffectEvent.isPrimary()};
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "effect primary";
	}

}
