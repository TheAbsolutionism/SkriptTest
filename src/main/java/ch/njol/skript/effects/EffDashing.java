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
import org.bukkit.entity.Camel;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Camel Dashing")
@Description({
	"Make a camel start or stop dashing.",
	"Dashing is a temporary speed burst, a dash lasts for 0.35 seconds."
})
@Examples({
	"make last spawned camel start dashing",
	"make last spawned camel stop dashing",
})
@Since("INSERT VERSION")
public class EffDashing extends Effect {

	static {
		Skript.registerEffect(EffDashing.class,
			"make %livingentities% (start dashing|dash)",
			"make %livingentities% stop dashing");
	}

	private Expression<LivingEntity> entities;
	private boolean dash;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		dash = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Camel camel)
				camel.setDashing(dash);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (dash ? " start" : " stop") + " dashing";
	}

}
