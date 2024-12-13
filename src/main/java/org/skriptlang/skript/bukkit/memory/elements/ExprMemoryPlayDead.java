package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Play Dead")
@Description("The amount of time for an axolotl to play dead.")
@Examples({
	"broadcast the play dead memory of last spawned axolotl",
	"add 5 seconds to the playing dead time memory of last spawned axolotl"
})
@Since("INSERT VERSION")
public class ExprMemoryPlayDead extends SimplePropertyExpression<LivingEntity, Timespan> {

	private static final MemoryKey<Integer> MEMORY_KEY = MemoryKey.PLAY_DEAD_TICKS;

	static {
		registerDefault(ExprMemoryPlayDead.class, Timespan.class, "play[ing] dead [time] memory", "livingentities");
	}

	@Override
	public @Nullable Timespan convert(LivingEntity entity) {
		try {
			Integer time = entity.getMemory(MEMORY_KEY);
			if (time == null)
				return null;
			return new Timespan(TimePeriod.TICK, time);
		} catch (Exception ignored) {}
		return null;
	}

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
				assert time != null;
				Integer current = entity.getMemory(MEMORY_KEY);
				Integer newTime = current != null ? current + time : time;
				entity.setMemory(MEMORY_KEY, newTime);
			};
			case REMOVE -> entity -> {
				assert time != null;
				Integer current = entity.getMemory(MEMORY_KEY);
				Integer newTime = current != null ? current - time : time;
				entity.setMemory(MEMORY_KEY, newTime);
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
		return "play dead time memory";
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

}
