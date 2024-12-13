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

@Name("Hunting Cooldown Is Enabled")
@Description("Checks to see if an axolotl has a hunting cooldown.")
@Examples({
	"if the hunting cooldown of last spawned axolotl is enabled:",
		"\tdisable the hunting cooldown of last spawned axolotl"
})
@Since("INSERT VERSION")
public class CondMemoryHuntingCooldown extends PropertyCondition<LivingEntity> {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.HAS_HUNTING_COOLDOWN;

	static {
		Skript.registerCondition(CondMemoryHuntingCooldown.class, ConditionType.PROPERTY,
			"[the] hunting cool[ ]down [memory] of %livingentities% (is|are) enabled",
			"[the] hunting cool[ ]down [memory] of %livingentities% (is|are) disabled");
	}

	private Expression<LivingEntity> expr;
	private boolean checkHunting;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkHunting = matchedPattern == 0;
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
			return memory == checkHunting;
		} catch (Exception ignored) {}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + expr.toString(event, debug) + (checkHunting ? "" : " does not ") + " have a hunting cooldown";
	}

}
