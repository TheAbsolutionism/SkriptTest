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
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fox Crouch")
@Description("Make a fox start or stop crouching.")
@Examples("make last spawned fox crouch")
@Since("INSERT VERSION")
public class EffFoxCrouch extends Effect {

	static {
		Skript.registerEffect(EffFoxCrouch.class,
			"make %livingentities% (start crouching|crouch)",
			"make %livingentities% (stop crouching|not crouch|uncrouch)");
	}

	private Expression<LivingEntity> entities;
	private boolean crouch;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		crouch = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Fox fox)
				fox.setCrouching(crouch);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (crouch ? " start" : " stop") + " crouching";
	}

}
