package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Admiring State")
@Description({
	"Allow a piglin to be admired.",
	"If disabled, piglins do not barter and are not distracted by gold items."
})
@Examples({
	"enabled the admiring state memory of last spawned piglin"
})
@Since("INSERT VERSION")
public class EffMemoryAdmiringDisabled extends Effect {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.ADMIRING_DISABLED;

	static {
		Skript.registerEffect(EffMemoryAdmiringDisabled.class,
			"enable [the] admiring state memory [of %livingentities%]",
			"enable [the] %livingentities%'[s] admiring state memory",
			"disable [the] admiring state memory [of %livingentities%]",
			"disable [the] %livingentities%'[s] admiring state memory");
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
				entity.setMemory(MEMORY_KEY, !enable);
			} catch (Exception ignored) {}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (enable ? "enable" : "disable") + " the admiring state memory of " + expr.toString(event, debug);
	}

}
