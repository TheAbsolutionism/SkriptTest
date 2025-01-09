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
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Allay Duplicate")
@Description({
	"Set whether an allay can or cannot duplicate itself.",
	"This is not the same as breeding allays."
})
@Examples({
	"allow all allays to duplicate",
	"disallow all allays to duplicate"
})
@Since("INSERT VERSION")
public class EffAllayCanDuplicate extends Effect {

	static {
		Skript.registerEffect(EffAllayCanDuplicate.class,
			"allow %livingentities% to (duplicate|clone)",
			"disallow %livingentities% to (duplicate|clone)",
			"(disallow|prevent) %livingentities% from (duplicating|cloning)");
	}

	private Expression<LivingEntity> entities;
	private boolean duplicate;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		duplicate = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Allay allay)
				allay.setCanDuplicate(duplicate);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (duplicate ? "allow " : "disallow ") + entities.toString(event, debug) + " to duplicate";
	}

}
