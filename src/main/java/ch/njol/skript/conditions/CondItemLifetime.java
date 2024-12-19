package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Will Despawn")
@Description("Checks if the dropped item will be despawned naturally through Minecraft's timer.")
@Examples({
	"if all dropped items can despawn naturally:",
		"\tprevent all dropped items from naturally despawning"
})
@Since("INSERT VERSION")
public class CondItemLifetime extends Condition {

	static {
		Skript.registerCondition(CondItemLifetime.class, ConditionType.PROPERTY,
			"%itementities% can despawn naturally",
			"%itementities% can naturally despawn",
			"%itementities% (can not|can't) despawn naturally",
			"%itementities% (can not|can't) naturally despawn"
		);
	}

	private Expression<Item> entities;
	private boolean canDespawn;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		canDespawn = matchedPattern <= 1;
		//noinspection unchecked
		entities = (Expression<Item>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.check(event, Item::isUnlimitedLifetime, canDespawn);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + entities.toString(event, debug) + (canDespawn ? " can " : " can not ") + "despawn naturally";
	}

}
