package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.util.WorldDate;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Last Worked")
@Description("The time since a villager last worked.")
@Examples({
	"broadcast the last worked memory of last spawned villager",
	"set the last worked at memory of last spawned villager to now",
	"add 5 seconds to the last worked at memory of last spawned villager"
})
@Since("INSERT VERSION")
public class ExprMemoryLastWorked extends SimplePropertyExpression<LivingEntity, WorldDate> {

	private static final MemoryKey<Long> MEMORY_KEY = MemoryKey.LAST_WORKED_AT_POI;

	static {
		Skript.registerExpression(ExprMemoryLastWoken.class, WorldDate.class, ExpressionType.PROPERTY,
			"[the] last worked [at] memory [of %livingentities%]",
			"[the] %livingentities%'[s] last worked [at] memory");
	}

	@Override
	public @Nullable WorldDate convert(LivingEntity entity) {
		try {
			Long time = entity.getMemory(MEMORY_KEY);
			if (time == null)
				return null;
			return new WorldDate(entity, time);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE -> CollectionUtils.array(Timespan.class);
			case SET -> CollectionUtils.array(WorldDate.class);
			case DELETE, RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Consumer<LivingEntity> consumer = switch (mode) {
			case ADD -> {
				long time =  ((Timespan) delta[0]).getAs(TimePeriod.TICK);
				yield entity -> {
					Long current = entity.getMemory(MEMORY_KEY);
					Long newValue = current != null ? current + time : entity.getWorld().getGameTime() + time;
					entity.setMemory(MEMORY_KEY, newValue);
				};
			}
			case REMOVE -> {
				long time =  ((Timespan) delta[0]).getAs(TimePeriod.TICK);
				yield entity -> {
					Long current = entity.getMemory(MEMORY_KEY);
					Long newValue = current != null ? current - time : entity.getWorld().getGameTime() - time;
					entity.setMemory(MEMORY_KEY, newValue);
				};
			}
			case SET -> {
				long newValue = ((WorldDate) delta[0]).getTotalTicks();
				yield entity -> {
					entity.setMemory(MEMORY_KEY, newValue);
				};
			}
			case DELETE -> entity -> {
				entity.setMemory(MEMORY_KEY, null);
			};
			case RESET -> entity -> {
				entity.setMemory(MEMORY_KEY, Date.now().getTimestamp());
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		for (LivingEntity entity : getExpr().getArray(event)) {
			try {
				consumer.accept(entity);
			} catch (Exception ignored) {}
		}
	}

	@Override
	protected String getPropertyName() {
		return "time since last worked at memory";
	}

	@Override
	public Class<WorldDate> getReturnType() {
		return WorldDate.class;
	}

}
