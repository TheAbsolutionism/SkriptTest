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

@Name("Is Angered")
@Description("Checks to see if a piglin is angered.")
@Examples({
	"if the last spawned piglin is angry:",
		"\tmake last spawned piglin not angry"
})
@Since("INSERT VERSION")
public class CondMemoryUniversalAnger extends PropertyCondition<LivingEntity> {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.UNIVERSAL_ANGER;

	static {
		Skript.registerCondition(CondMemoryUniversalAnger.class, ConditionType.PROPERTY,
			"[the] %livingentities%['[s]] (is|are) (angered|angry)",
			"[the] anger state memory of %livingentities% (is|are) enabled",
			"[the] %livingentities%'[s] anger state memory (is|are) enabled",
			"[the] %livingentities%['[s]] (isn't|is not|aren't|are not) (angered|angry)",
			"[the] anger state memory of %livingentities% (is|are) disabled",
			"[the] %livingentities%'[s] anger state memory (is|are) disabled");
	}

	private Expression<LivingEntity> expr;
	private boolean checkAnger;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkAnger = matchedPattern <= 2;
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
			return memory == checkAnger;
		} catch (Exception ignored) {}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + expr.toString(event, debug) + (checkAnger ? " are " : " are not ") + "angered";
	}

}
