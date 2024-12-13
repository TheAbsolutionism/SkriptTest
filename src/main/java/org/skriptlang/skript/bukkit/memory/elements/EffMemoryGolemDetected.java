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

@Name("Detect Golem")
@Description("Make a villager detect a golem.")
@Examples({
	"make the last spawned villager detect a golem"
})
@Since("INSERT VERSION")
public class EffMemoryGolemDetected extends Effect {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.GOLEM_DETECTED_RECENTLY;

	static {
		Skript.registerEffect(EffMemoryGolemDetected.class,
			"make [the] %livingentities% detect a golem",
			"force [the] %livingentities% to detect a golem",
			"enable [the] golem detected recently memory [of %livingentities%]",
			"enable [the] %livingentities%'[s] golem detected recently memory",
			"enable [the] recently detected golem memory [of %livingentities%]",
			"enable [the] %livingentities%'[s] recently detected golem memory",
			"make [the] %livingentities% (stop detecting|not detect) a golem",
			"force [the] %livingentities% to (stop detecting|not detect) a golem",
			"disable [the] golem detected recently memory [of %livingentities%]",
			"disable [the] %livingentities%'[s] golem detected recently memory",
			"disable [the] recently detected golem memory [of %livingentities%]",
			"disable [the] %livingentities%'[s] recently detected golem memory");
	}

	private Expression<LivingEntity> expr;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enable = matchedPattern <= 5;
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
		return (enable ? "enable" : "disable") + " the golem detected recently memory of " + expr.toString(event, debug);
	}

}
