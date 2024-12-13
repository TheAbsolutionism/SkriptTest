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

@Name("Has Detected Golem Recently")
@Description("Checks to see if a villager has detected a golem recently.")
@Examples({
	"if the last spawned zombie has villager a golem recently:",
		"\tmake the last spawned villager not detect a golem"
})
@Since("INSERT VERSION")
public class CondMemoryGolemDetected extends PropertyCondition<LivingEntity> {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.GOLEM_DETECTED_RECENTLY;

	static {
		Skript.registerCondition(CondMemoryGolemDetected.class, ConditionType.PROPERTY,
			"[the] %livingentities%['[s]] (has|have) detected [a] golem recently",
			"[the] %livingentities%['[s]] (has|have) recently detected [a] golem",
			"[the] %livingentities%['[s]] (hasn't|has not|haven't|have not) detected [a] golem recently",
			"[the] %livingentities%['[s]] (hasn't|has not|haven't|have not) recently detected [a] golem");
	}

	private Expression<LivingEntity> expr;
	private boolean checkDetected;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkDetected = matchedPattern <= 1;
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
			return memory == checkDetected;
		} catch (Exception ignored) {}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + expr.toString(event, debug) + (checkDetected ? " have " : " have not ")
			+ "recently detected a golem";
	}

}
