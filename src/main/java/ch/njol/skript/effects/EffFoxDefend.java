package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fox Defend")
@Description("Set whether or not a fox should defend its trusted players from wolves and polar bears.")
@Examples("make last spawned fox start defending")
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class EffFoxDefend extends Effect {

	static {
		if (Skript.methodExists(Fox.class, "setDefending", boolean.class)) {
			Skript.registerEffect(EffFoxDefend.class,
				"make %livingentities% (start defending|defend)",
				"make %livingentities% (stop defending|not defend)");
		}
	}

	private Expression<LivingEntity> entities;
	private boolean defend;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		defend = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (!(entity instanceof Fox fox))
				continue;
			fox.setDefending(defend);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (defend ? " start" : " stop") + " defending";
	}

}
