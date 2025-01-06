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

@Name("Fox Faceplant")
@Description("Set whether a fox should plant its face or not.")
@Examples("make last spawned fox faceplant")
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class EffFoxFaceplant extends Effect {

	static {
		if (Skript.methodExists(Fox.class, "setFaceplanted", boolean.class)) {
			Skript.registerEffect(EffFoxFaceplant.class,
				"make %livingentities% faceplant",
				"make %livingentities% not faceplant");
		}
	}

	private Expression<LivingEntity> entities;
	private boolean faaceplant;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		faaceplant = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Fox fox)
				fox.setFaceplanted(faaceplant);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (faaceplant ? " start" : " stop") + " crouching";
	}

}
