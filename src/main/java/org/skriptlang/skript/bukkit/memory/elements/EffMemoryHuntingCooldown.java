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

@Name("Hunting Cooldown")
@Description("Make an axolotl have a hunting cooldown.")
@Examples({
	"enable the hunting cooldown of last spawned axolotl"
})
@Since("INSERT VERSION")
public class EffMemoryHuntingCooldown extends Effect {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.HAS_HUNTING_COOLDOWN;

	static {
		Skript.registerEffect(EffMemoryHuntingCooldown.class,
			"enable [the] hunting cool[ ]down [memory] [of %livingentities%]",
			"enable [the] %livingentities%'[s] hunting cool[ ]down [memory]",
			"disable [the] hunting cool[ ]down [memory] [of %livingentities%]",
			"disable [the] %livingentities%'[s] hunting cool[ ]down [memory]");
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
				Skript.adminBroadcast("Hunting Cooldown: " + entity.getMemory(MEMORY_KEY));
			} catch (Exception ignored) {}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (enable ? "enable" : "disable") + " the hunting cooldown memory of " + expr.toString(event, debug);
	}

}
