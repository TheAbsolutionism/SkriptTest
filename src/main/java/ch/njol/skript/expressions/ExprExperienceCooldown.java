package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Experience Cooldown")
@Description({
	"The experience cooldown of a player.",
	"Experience cooldown is how long until a player can pick up another orb of experience."
})
@Examples({
	"send experience cooldown of player",
	"set experience cooldown of player to 1 hour",
	"if experience cooldown of player >= 10 minutes:",
		"\tclear experience cooldown of player"
})
@Since("INSERT VERSION")
public class ExprExperienceCooldown extends SimplePropertyExpression<Player, Timespan> {

	static {
		register(ExprExperienceCooldown.class, Timespan.class, "[the] (experience|[e]xp) cooldown", "players");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Player>) exprs[0]);
		return true;
	}


	@Override
	public @Nullable Timespan convert(Player player) {
		return new Timespan(Timespan.TimePeriod.TICK, player.getExpCooldown());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		switch (mode) {
			case REMOVE_ALL, DELETE, REMOVE -> {return null;}
		}
		return CollectionUtils.array(Timespan.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedTime = 0;
		if (delta[0] != null)
			providedTime = (int) ((Timespan) delta[0]).get(Timespan.TimePeriod.TICK);

		switch (mode) {
			case ADD -> {
				for (Player player : getExpr().getArray(event)) {
					player.setExpCooldown(player.getExpCooldown() + providedTime);
				}
			}
			case REMOVE -> {
				for (Player player : getExpr().getArray(event)) {
					player.setExpCooldown(Math.max(player.getExpCooldown() - providedTime, 0));
				}
			}
			case SET -> {
				for (Player player : getExpr().getArray(event)) {
					player.setExpCooldown(providedTime);
				}
			}
			case RESET -> {
				for (Player player : getExpr().getArray(event)) {
					player.setExpCooldown(0);
				}
			}
			default -> {}
		}
	}

	@Override
	protected String getPropertyName() {
		return "experience cooldown";
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

}
