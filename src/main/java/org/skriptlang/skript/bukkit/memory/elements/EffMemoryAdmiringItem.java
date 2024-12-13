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

@Name("Admiring Item")
@Description("Make a piglin admire an item.")
@Examples({
	"make last spawned piglin admire an item"
})
@Since("INSERT VERSION")
public class EffMemoryAdmiringItem extends Effect {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.ADMIRING_ITEM;

	static {
		Skript.registerEffect(EffMemoryAdmiringItem.class,
			"make [the] %livingentities% admire [an] item",
			"force [the] %livingentities% to admire [an] item",
			"enable [the] admiring item memory [of %livingentities%]",
			"enable [the] %livingentities%'[s] admiring item memory",
			"make [the] %livingentities% (stop admiring|not admire) [an] item",
			"force [the] %livingentities% to (stop admiring|not admire) [an] item",
			"disable [the] admiring item memory [of %livingentities%]",
			"disable [the] %livingentities%'[s] admiring item memory");
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
		return (enable ? "enable" : "disable") + " the admiring item memory of " + expr.toString(event, debug);
	}

}
