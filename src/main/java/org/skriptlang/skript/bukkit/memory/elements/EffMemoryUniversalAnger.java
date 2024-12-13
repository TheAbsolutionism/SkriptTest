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

@Name("Anger State")
@Description({
	"Make a piglin angry.",
	"Will not work if 'universal anger' gamerule is disabled."
})
@Examples({
	"make last spawned piglin angry"
})
@Since("INSERT VERSION")
public class EffMemoryUniversalAnger extends Effect {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.UNIVERSAL_ANGER;

	static {
		Skript.registerEffect(EffMemoryUniversalAnger.class,
			"make [the] %livingentities% (angered|angry)",
			"force [the] %livingentities% to be (angered|angry)",
			"enable [the] anger state memory [of %livingentities%]",
			"enable [the] %livingentities%'[s] anger state memory",
			"make [the] %livingentities% not (angered|angry)",
			"force [the] %livingentities% not to be (angered|angry)",
			"disable [the] anger state memory [of %livingentities%]",
			"disable [the] %livingentities%'[s] anger state memory");
	}

	private Expression<LivingEntity> expr;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enable = matchedPattern <= 3;
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
		return (enable ? "enable" : "disable") + " the anger state memory of " + expr.toString(event, debug);
	}

}
