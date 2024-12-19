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
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Item Despawn")
@Description("Prevent a dropped item from naturally despawning through Minecraft's timer.")
@Examples({
	"prevent all dropped items from naturally despawning",
	"allow all dropped items to naturally despawn"
})
@Since("INSERT VERSION")
public class EffItemLifetime extends Effect {

	static {
		Skript.registerEffect(EffItemLifetime.class,
			"prevent %itementities% from naturally despawning",
			"prevent %itementities% from despawning naturally",
			"allow %itementities% to naturally despawn",
			"allow %itementities% to despawn naturally");
	}

	private Expression<Item> entities;
	private boolean prevent;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		prevent = matchedPattern <= 1;
		//noinspection unchecked
		entities = (Expression<Item>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Item item : entities.getArray(event)) {
			item.setUnlimitedLifetime(prevent);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String start = "prevent ";
		String designation = "from ";
		if (!prevent) {
			start = "allow ";
			designation = "to ";
		}
		return start + entities.toString(event, debug) + designation + "naturally despawning";
	}

}
