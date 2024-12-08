package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ExprMemoryItemPickupCooldown extends SimplePropertyExpression<LivingEntity, Timespan> {

	private static final MemoryKey<Integer> MEMORY_KEY = MemoryKey.ITEM_PICKUP_COOLDOWN_TICKS;

	static {
		registerDefault(ExprMemoryItemPickupCooldown.class, Timespan.class, "item pick[ ]up cool[ ]down [time]", "livingentities");
	}

	@Override
	public @Nullable Timespan convert(LivingEntity entity) {
		try {
			Integer time = entity.getMemory(MEMORY_KEY);
			return new Timespan(TimePeriod.TICK, time);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, DELETE -> CollectionUtils.array(Timespan.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Integer time;
		if (delta != null) {
			time = (int) ((Timespan) delta[0]).getAs(TimePeriod.TICK);
		} else {
            time = null;
        }
        Consumer<LivingEntity> consumer = switch (mode) {
			case ADD -> entity -> {
				Integer current = entity.getMemory(MEMORY_KEY);
				entity.setMemory(MEMORY_KEY, current + time);
			};
			case REMOVE -> entity -> {
				Integer current = entity.getMemory(MEMORY_KEY);
				entity.setMemory(MEMORY_KEY, current - time);
			};
			case SET, DELETE -> entity -> {
				entity.setMemory(MEMORY_KEY, time);
			};
			default -> {
				throw new IllegalStateException("Unexpected value: " + mode);
			}
		};

		for (LivingEntity entity : getExpr().getArray(event)) {
			try {
				consumer.accept(entity);
			} catch (Exception ignored) {}
		}

	}

	@Override
	protected String getPropertyName() {
		return "item pickup cooldown time";
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}
}
