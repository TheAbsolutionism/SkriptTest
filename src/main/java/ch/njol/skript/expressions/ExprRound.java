package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Rounding")
@Description("Rounds numbers normally, up (ceiling) or down (floor) respectively.")
@Examples({
	"set {var} to rounded health of player",
	"set line 1 of the block to rounded \"%(1.5 * player's level)%\"",
	"add rounded down argument to the player's health"
})
@Since("2.0")
public class ExprRound extends PropertyExpression<Number, Long> {

	static {
		Skript.registerExpression(ExprRound.class, Long.class, ExpressionType.PROPERTY,
				"(a|the|) (round[ed] down|floored) %numbers%",
				"%numbers% (round[ed] down|floored)",
				"(a|the|) round[ed] %numbers%",
				"%numbers% round[ed]",
				"(a|the|) (round[ed] up|ceiled) %numbers%",
				"%numbers% (round[ed] up|ceiled)"
			);
	}
	
	private int action;

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends Number>) exprs[0]);
		action = matchedPattern / 2;
		return true;
	}
	
	@Override
	protected Long @Nullable [] get(Event event, Number[] source) {
		return get(source, number -> {
			if (number instanceof Integer integer) {
				return integer.longValue();
			} else if  (number instanceof Long long1) {
				return long1;
			}

			if (action == 0) {
				return Math2.floor(number.doubleValue());
			} else if (action == 1) {
				return Math2.round(number.doubleValue());
			} else {
				return Math2.ceil(number.doubleValue());
			}
		});
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (action == 0) {
			builder.append("floor");
		} else if (action == 1) {
			builder.append("round");
		} else {
			builder.append("ceil");
		}
		builder.append(getExpr());
		return builder.toString();
	}
	
}
