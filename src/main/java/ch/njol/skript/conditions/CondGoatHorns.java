package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffGoatHorns.GoatHorn;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Goat Has Horns")
@Description("Checks to see if a goat has or does not have a left, right, or both horns.")
@Examples({
	"if last spawned goat does not have both horns:",
		"\tmake last spawned goat have both horns",
	"",
	"if {_goat} has a right horn:",
		"\tforce {_goat} to not have a right horn"
})
@Since("INSERT VERSION")
public class CondGoatHorns extends Condition {

	static {
		Skript.registerCondition(CondGoatHorns.class,
			"%livingentities% (has|[does] have) [a] left horn",
			"%livingentities% (has|[does] have) [a] right horn",
			"%livingentities% (has|[does] have) both horns",
			"%livingentities% (does not|doesn't) have [a] left horn",
			"%livingentities% (does not|doesn't) have [a] right horn",
			"%livingentities% (does not|doesn't) have both horns");
	}

	private Expression<LivingEntity> entities;
	private GoatHorn goatHorn;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		goatHorn = GoatHorn.values()[matchedPattern / 3];
		setNegated(matchedPattern >= 3);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.check(event, entity -> {
			if (!(entity instanceof Goat goat))
				return false;
			boolean hasHorns = true;
			if (goatHorn != GoatHorn.RIGHT)
				hasHorns = goat.hasLeftHorn();
			if (goatHorn != GoatHorn.LEFT)
				hasHorns &= goat.hasRightHorn();
			return hasHorns;
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(entities);
		if (isNegated()) {
			builder.append("does not have");
		} else if (entities.isSingle()) {
			builder.append("has");
		} else {
			builder.append("have");
		}
		builder.append(switch (goatHorn) {
			case LEFT -> "left horn";
			case RIGHT -> "right horn";
			case BOTH -> "both horns";
		});
		return builder.toString();
	}

}
