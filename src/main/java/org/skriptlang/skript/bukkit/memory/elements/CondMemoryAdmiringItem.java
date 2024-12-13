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

@Name("Is Admiring Item")
@Description({
	"Checks to see if the admiring item memory of a piglin is enabled.",
	"If enabled, the piglin is currently admiring an item."
})
@Examples({
	"if the last spawned piglin is admiring an item:",
		"\tmake last spawned piglin stop admiring an item"
})
@Since("INSERT VERSION")
public class CondMemoryAdmiringItem extends PropertyCondition<LivingEntity> {

	private static final MemoryKey<Boolean> MEMORY_KEY = MemoryKey.ADMIRING_ITEM;

	static {
		Skript.registerCondition(CondMemoryAdmiringItem.class, ConditionType.PROPERTY,
			"[the] %livingentities% (is|are) admiring [an] item",
			"[the] admiring item memory of %livingentities% (is|are) enabled",
			"[the] %livingentities%'[s] admiring item memory (is|are) enabled",
			"[the] %livingentities% (isn't|is not|aren't|are not) admiring [an] item",
			"[the] admiring item memory of %livingentities% (is|are) disabled",
			"[the] %livingentities%'[s] admiring item memory (is|are) disabled");
	}

	private Expression<LivingEntity> expr;
	private boolean checkAdmiringItem;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkAdmiringItem = matchedPattern <= 2;
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
			return memory == checkAdmiringItem;
		} catch (Exception ignored) {}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the admiring item memory of " + expr.toString(event, debug) + " are "
			+ (checkAdmiringItem ? "enabled" : "dissabled");
	}

}
