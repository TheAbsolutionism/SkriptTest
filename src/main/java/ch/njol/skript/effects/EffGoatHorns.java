package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Goat Have Horns")
@Description("Make a goat have or not have a left, right, or both horns.")
@Examples({
	"make last spawned goat not have both horns",
	"force {_goat} to have a left horn",
	"make all goats have a right horn"
})
@Since("INSERT VERSION")
public class EffGoatHorns extends Effect {

	public enum GoatHorn {
		LEFT, RIGHT, BOTH
	}

	static {
		Skript.registerEffect(EffGoatHorns.class,
			"make %livingentities% [:not] have ([a] left horn|right:[a] right horn|both:both horns)",
			"force %livingentities% to [:not] have ([a] left horn|right:[a] right horn|both:both horns)");
	}

	private Expression<LivingEntity> entities;
	private GoatHorn goatHorn = GoatHorn.LEFT;
	private boolean have;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (parseResult.hasTag("right")) {
			goatHorn = GoatHorn.RIGHT;
		} else if (parseResult.hasTag("both")) {
			goatHorn = GoatHorn.BOTH;
		}
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		have = !parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Goat goat) {
				if (goatHorn != GoatHorn.RIGHT)
					goat.setLeftHorn(have);
				if (goatHorn != GoatHorn.LEFT)
					goat.setRightHorn(have);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities);
		if (!have)
			builder.append("not");
		builder.append("have");
		builder.append(switch (goatHorn) {
			case LEFT -> "a left horn";
			case RIGHT -> "a right horn";
			case BOTH -> "both horns";
		});
		return builder.toString();
	}

}
