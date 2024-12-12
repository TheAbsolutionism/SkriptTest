package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class CondMemoryHuntingCooldown extends PropertyCondition<LivingEntity> {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.HAS_HUNTING_COOLDOWN;

	static {
		Skript.registerCondition(CondMemoryHuntingCooldown.class, ConditionType.PROPERTY,
			"[the] hunting cooldown memory of %livingentities% (is|are) enabled",
			"[the] hunting cooldown memory of %livingentities% (is|are) disabled",
			"[the] hunting cooldown memory of %livingentities% (isn't|is not|aren't|are not) enabled",
			"[the] hunting cooldown memory of %livingentities% (isn't|is not|aren't|are not) disabled",
			"[the] %livingentities%'[s] hunting cooldown memory (is|are) enabled",
			"[the] %livingentities%'[s] hunting cooldown memory (is|are) disabled",
			"[the] %livingentities%'[s] hunting cooldown memory (isn't|is not|aren't|are not) enabled",
			"[the] %livingentities%'[s] hunting cooldown memory (isn't|is not|aren't|are not) disabled");
	}

	private Expression<LivingEntity> expr;
	private boolean checkEnabled;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkEnabled = matchedPattern % 2 == 0;
		setNegated(matchedPattern % 2 == 1);
		//noinspection unchecked
		expr = (Expression<LivingEntity>) exprs[0];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity entity) {
		try {
			Boolean memory = entity.getMemory(MEMORY_KEY);
			if (memory == null)
				return isNegated();
			return memory == checkEnabled;
		} catch (Exception ignored) {}
		return isNegated();
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the hunting cooldown memory of " + expr.toString(event, debug) + (isNegated() ? " are not " : " are ")
			+ (checkEnabled ? "enabled" : "dissabled");
	}

}
