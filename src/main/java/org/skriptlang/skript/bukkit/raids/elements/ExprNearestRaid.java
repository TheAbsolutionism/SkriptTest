package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Nearest Raid")
@Description("Gets the nearest raid of a location in the provided radius.")
@Examples({
	"set {_raid} to the nearest raid from location(0, 0, 0) in radius 5"
})
@Since("INSERT VERSION")
public class ExprNearestRaid extends PropertyExpression<Location, Raid> {

	static {
		Skript.registerExpression(ExprNearestRaid.class, Raid.class, ExpressionType.PROPERTY,
			"nearest raid[s] (from|of) %locations% in radius [of] %integer%");
	}

	private Expression<Location> exprLocation;
	private Expression<Integer> exprInteger;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprLocation = (Expression<Location>) exprs[0];
		//noinspection unchecked
		exprInteger = (Expression<Integer>) exprs[1];
		return true;
	}

	@Override
	protected Raid @Nullable [] get(Event event, Location[] source) {
		int radius = (int) exprInteger.getSingle(event);
		return exprLocation.stream(event).map(location -> {
			return location.getWorld().locateNearestRaid(location, radius);
		}).toArray(Raid[]::new);
	}

	@Override
	public Class<Raid> getReturnType() {
		return Raid.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "nearest raid from " + exprLocation.toString(event, debug) + " in radius of " + exprInteger.toString(event, debug);
	}

}
