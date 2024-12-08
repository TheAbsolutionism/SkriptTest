package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMemoryJobSite extends SimplePropertyExpression<LivingEntity, Location> {

	private static final MemoryKey<Location> JOB_MEMORY = MemoryKey.JOB_SITE;
	private static final MemoryKey<Location> POTENTIAL_MEMORY = MemoryKey.POTENTIAL_JOB_SITE;

	static {
		Skript.registerExpression(ExprMemoryJobSite.class, Location.class, ExpressionType.PROPERTY,
			"[the] job site memory [of %livingentities%]",
			"[the] potential job site memory [of %livingentities%]");
	}

	private boolean isPotential;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isPotential = matchedPattern == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Location convert(LivingEntity entity) {
		MemoryKey<Location> memory = isPotential ? POTENTIAL_MEMORY : JOB_MEMORY;
		try {
			return entity.getMemory(memory);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Location.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		MemoryKey<Location> memory = isPotential ? POTENTIAL_MEMORY : JOB_MEMORY;
		Location loc = null;
		if (delta != null)
			loc = (Location) delta[0];

		for (LivingEntity entity : getExpr().getArray(event)) {
			entity.setMemory(memory, loc);
		}

	}

	@Override
	protected String getPropertyName() {
		return (isPotential ? "potential" : "") + "job site";
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

}
