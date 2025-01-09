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
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Allay Dance")
@Description({
	"Make an allay start or stop dancing.",
	"By providing a location when making an allay dance, checks to see if the block at the location is a jukebox and playing music.",
	"By not providing a location, the allay will dance forever."
})
@Examples({
	"if last spawned allay is not dancing:",
		"\tmake last spawned allay start dancing"
})
@Since("INSERT VERSION")
public class EffAllayDancing extends Effect {

	static {
		Skript.registerEffect(EffAllayDancing.class,
			"make %livingentities% (start dancing|dance) [from %-direction% %-location%]",
			"make %livingentities% (stop dancing|not dance)");
	}

	private Expression<LivingEntity> entities;
	private boolean start;
	private @Nullable Expression<Location> location;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		start = matchedPattern == 0;
		if (start && exprs[1] != null) {
			//noinspection unchecked
			Expression<Direction> direction = (Expression<Direction>) exprs[1];
			//noinspection unchecked
			Expression<Location> location = (Expression<Location>) exprs[2];
			this.location = Direction.combine(direction, location);
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Location location = null;
		if (this.location != null) {
			location = this.location.getSingle(event);
			//if (location == null)
				// Runtime warning;
		}
		for (LivingEntity entity : entities.getArray(event)) {
			if (!(entity instanceof Allay allay))
				continue;
			if (!start) {
				allay.stopDancing();
				continue;
			}
			if (location == null) {
				allay.startDancing();
				continue;
			}
			allay.startDancing(location);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities);
		if (start) {
			builder.append("start");
		} else {
			builder.append("stop");
		}
		builder.append("dancing");
		if (location != null)
			builder.append("from", location);
		return builder.toString();
	}

}
