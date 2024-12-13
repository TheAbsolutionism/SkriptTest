package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Admiring State Enabled")
@Description({
	"Checks to see if the admiring state memory of a piglin is enabled.",
	"If disabled, piglins do not barter and are not distracted by gold items."
})
@Examples({
	"if the admiring state memory of last spawned piglin is disabled:",
		"\tenable the admiring state memory of last spawned piglin"
})
@Since("INSERT VERSION")
public class CondMemoryAdmiringDisabled extends PropertyCondition<LivingEntity> {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.ADMIRING_DISABLED;

	static {
		Skript.registerCondition(CondMemoryAdmiringDisabled.class, ConditionType.PROPERTY,
			"[the] admiring state memory of %livingentities% (is|are) enabled",
			"[the] %livingentities%'[s] admiring state memory (is|are) enabled",
			"[the] admiring state memory of %livingentities% (is|are) disabled",
			"[the] %livingentities%'[s] admiring state memory (is|are) disabled");
	}

	private Expression<LivingEntity> expr;
	private boolean checkAdmiringState;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkAdmiringState = matchedPattern <= 1;
		//noinspection unchecked
		expr = (Expression<LivingEntity>) exprs[0];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity entity) {
		try {
			Boolean memory = entity.getMemory(MEMORY_KEY);
			if (memory == null)
				return false;
			return memory == !checkAdmiringState;
		} catch (Exception ignored) {}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the admiring state memory of " + expr.toString(event, debug) + " are "
			+ (checkAdmiringState ? "enabled" : "dissabled");
	}

}
