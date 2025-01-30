package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

import java.util.function.Consumer;

@Name("No Damage Ticks")
@Description("The number of ticks that an entity is invulnerable to damage for.")
@Examples({
	"on damage:",
		"\tset victim's invulnerability ticks to 20 #Victim will not take damage for the next second",
	"",
	"if the no damage timespan of {_entity} is 0 seconds:",
		"\tset the invincibility time of {_entity} to 1 minute"
})
@Since("2.5, INSERT VERSION (timespan)")
public class ExprNoDamageTicks extends SimplePropertyExpression<LivingEntity, Object> {
	
	static {
		register(ExprNoDamageTicks.class, Object.class,"(invulnerability|invincibility|no damage) tick[s]", "livingentities");
		register(ExprNoDamageTicks.class, Object.class, "(timespan:(invulnerability|invincibility|no damage) time[[ ]span])", "livingentities");
	}

	private boolean timespan;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		timespan = parseResult.hasTag("timespan");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Object convert(LivingEntity entity) {
		long ticks = entity.getNoDamageTicks();
		if (timespan)
			return new Timespan(TimePeriod.TICK, ticks);
		return ticks;
	}
	
	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET, ADD, REMOVE -> CollectionUtils.array(Number.class, Timespan.class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedTicks = 0;
		if (delta != null) {
			if (delta[0] instanceof Number number) {
				providedTicks = number.intValue();
			} else if (delta[0] instanceof Timespan timespan) {
				providedTicks = (int) timespan.getAs(TimePeriod.TICK);
			}
		}
		int finalTicks = providedTicks;
		Consumer<LivingEntity> consumer = switch (mode) {
			case SET, DELETE, RESET -> entity -> entity.setNoDamageTicks(finalTicks);
			case ADD -> entity -> {
				int current = entity.getNoDamageTicks();
				int value = Math2.fit(0, current + finalTicks, Integer.MAX_VALUE);
				entity.setNoDamageTicks(value);
			};
			case REMOVE -> entity -> {
				int current = entity.getNoDamageTicks();
				int value = Math2.fit(0, current - finalTicks, Integer.MAX_VALUE);
				entity.setNoDamageTicks(value);
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		for (LivingEntity entity : getExpr().getArray(event)) {
			consumer.accept(entity);
		}
	}
	
	@Override
	protected String getPropertyName() {
		return timespan ? "no damage timespan" : "no damage ticks";
	}
	
	@Override
	public Class<?> getReturnType() {
		return timespan ? Timespan.class : Long.class;
	}
	
}
