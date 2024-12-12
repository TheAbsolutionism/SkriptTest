package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffMemoryTempted extends Effect {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.IS_TEMPTED;

	static {
		Skript.registerEffect(EffMemoryTempted.class,
			"enable [the] temptation state memory [of %livingentities%]",
			"enable [the] %livingentities%'[s] temptation state memory",
			"disable [the] temptation state memory [of %livingentities%]",
			"disable [the] %livingentities%'[s] temptation state memory");
	}

	private Expression<LivingEntity> expr;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enable = matchedPattern <= 1;
		//noinspection unchecked
		expr = (Expression<LivingEntity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : expr.getArray(event)) {
			try {
				entity.setMemory(MEMORY_KEY, enable);
			} catch (Exception ignored) {}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (enable ? "enable" : "disable") + " the temptation state memory of " + expr.toString(event, debug);
	}

}
