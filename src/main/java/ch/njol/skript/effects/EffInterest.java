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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Interested")
@Description({
	"Set whether a wolf or fox should be interested.",
	"Forcing a wolf to become interested effectively rotates its head forever, until it becomes naturally interested and uninterested."
})
@Examples("make last spawned wolf interested")
@Since("INSERT VERSION")
public class EffInterest extends Effect {

	static {
		Skript.registerEffect(EffInterest.class,
			"make %livingentities% interested",
			"make %livingentities% (not |un|dis)interested");
	}

	private Expression<LivingEntity> entities;
	private boolean interested;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		interested = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Wolf wolf)
				wolf.setInterested(interested);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (interested ? " interested" : " uninterested") ;
	}

}
