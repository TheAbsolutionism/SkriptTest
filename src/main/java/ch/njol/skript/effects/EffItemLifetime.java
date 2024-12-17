package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Unlimited Item Lifetime")
@Description("Makes a dropped item have unlimited lifetime, meaning it won't despawn from vanilla Minecraft timer.")
@Examples({
	"enabled unlimited lifetime of all dropped items"
})
@Since("INSERT VERSION")
public class EffItemLifetime extends Effect {

	static {
		Skript.registerEffect(EffItemLifetime.class,
			"enable unlimited lifetime (of|for) %entities%",
			"disable unlimited lifetime (of|for) %entities%");
	}

	private Expression<Entity> exprEntity;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enable = matchedPattern == 0;
		//noinspection unchecked
		exprEntity = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : exprEntity.getArray(event)) {
			if (!(entity instanceof Item item))
				continue;
			item.setUnlimitedLifetime(enable);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (enable ? "enable" : "disable") + " unlimited lifetime of " + exprEntity.toString(event, debug);
	}

}
