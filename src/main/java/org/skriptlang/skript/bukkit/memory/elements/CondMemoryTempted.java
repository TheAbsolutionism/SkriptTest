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

@Name("Is Tempted")
@Description("Checks to see if an axolotl, camel, or goat is tempted.")
@Examples({
	"if the last spawned goat is tempted:",
		"\tmake last spawned goat not be tempted"
})
@Since("INSERT VERSION")
public class CondMemoryTempted extends PropertyCondition<LivingEntity> {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.IS_TEMPTED;

	static {
		Skript.registerCondition(CondMemoryTempted.class, ConditionType.PROPERTY,
			"[the] %livingentities%['[s]] (is|are) tempted",
			"[the] temptation state memory of %livingentities% (is|are) enabled",
			"[the] %livingentities%'[s] temptation state memory (is|are) enabled",
			"[the] %livingentities%['[s]] (isn't|is not|aren't|are not) tempted",
			"[the] temptation state memory of %livingentities% (is|are) disabled",
			"[the] %livingentities%'[s] temptation state memory (is|are) disabled");
	}

	private Expression<LivingEntity> expr;
	private boolean checkTempted;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkTempted = matchedPattern <= 2;
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
			return memory == checkTempted;
		} catch (Exception ignored) {}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + expr.toString(event, debug) + (checkTempted ? " are " : " are not ") + "tempted";
	}

}
