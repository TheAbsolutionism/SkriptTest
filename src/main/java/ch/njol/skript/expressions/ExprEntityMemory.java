package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public class ExprEntityMemory extends PropertyExpression<LivingEntity, Object> {

	private enum MemoryHandler {
		INTEGER(Integer.class) {
			@Override
			public Consumer<LivingEntity> changer(MemoryKey<?> memoryKey, ChangeMode mode, Object delta) {
				//noinspection unchecked
				MemoryKey<Integer> typedKey = (MemoryKey<Integer>) memoryKey;
				Integer value = (Integer) delta;
				return switch (mode) {
					case ADD -> entity -> {
						Integer current = entity.getMemory(typedKey);
						Integer change = current != null ? current + value :  value;
						entity.setMemory(typedKey, change);
					};
					case REMOVE -> entity -> {
						Integer current = entity.getMemory(typedKey);
						Integer change = current != null ? current - value :  value;
						entity.setMemory(typedKey, change);
					};
					case SET -> entity -> {
						entity.setMemory(typedKey, value);
					};
					default -> null;
				};
			}
		},

		LONG(Long.class) {
			@Override
			public Consumer<LivingEntity> changer(MemoryKey<?> memoryKey, ChangeMode mode, Object delta) {
				//noinspection unchecked
				MemoryKey<Long> typedKey = (MemoryKey<Long>) memoryKey;
				Long value = (Long) delta;
				return switch (mode) {
					case ADD -> entity -> {
						Long current = entity.getMemory(typedKey);
						Long change = current != null ? current + value :  value;
						entity.setMemory(typedKey, change);
					};
					case REMOVE -> entity -> {
						Long current = entity.getMemory(typedKey);
						Long change = current != null ? current - value :  value;
						entity.setMemory(typedKey, change);
					};
					case SET -> entity -> {
						entity.setMemory(typedKey, value);
					};
					default -> null;
				};
			}
		},

		BOOLEAN(Boolean.class) {
			@Override
			public Consumer<LivingEntity> changer(MemoryKey<?> memoryKey, ChangeMode mode, Object delta) {
				//noinspection unchecked
				MemoryKey<Boolean> typedKey = (MemoryKey<Boolean>) memoryKey;
				if (mode != ChangeMode.SET)
					return null;
				Boolean value = (Boolean) delta;
				return entity -> {
					entity.setMemory(typedKey, value);
				};
			}
		},

		UUID(UUID.class) {
			@Override
			public Consumer<LivingEntity> changer(MemoryKey<?> memoryKey, ChangeMode mode, Object delta) {
				//noinspection unchecked
				MemoryKey<UUID> typedKey = (MemoryKey<UUID>) memoryKey;
				if (mode != ChangeMode.SET)
					return null;
				UUID value;
				if (delta instanceof UUID uuid) {
					value = uuid;
				} else if (delta instanceof String string) {
					value = java.util.UUID.fromString(string);
				} else {
                    value = null;
                }
                return entity -> {
					entity.setMemory(typedKey, value);
				};
			}
		},

		LOCATION(Location.class) {
			@Override
			public Consumer<LivingEntity> changer(MemoryKey<?> memoryKey, ChangeMode mode, Object delta) {
				//noinspection unchecked
				MemoryKey<Location> typedKey = (MemoryKey<Location>) memoryKey;
				if (mode != ChangeMode.SET)
					return null;
				Location value = (Location) delta;
				return entity -> {
					entity.setMemory(typedKey, value);
				};
			}
		};

		private final Class<?> clazz;

		MemoryHandler(Class<?> clazz) {
			this.clazz = clazz;
		}

		public abstract Consumer<LivingEntity> changer(MemoryKey<?> memoryKey, ChangeMode mode, Object delta);

		public static MemoryHandler getMemoryHandler(Class<?> clazz) {
			for (MemoryHandler handler : values()) {
				if (handler.clazz.equals(clazz))
					return handler;
			}
			return null;
		}

	}

	private static final MemoryKey<?>[] memoryKeys = MemoryKey.values().toArray(new MemoryKey[0]);

	static {
		String[] patterns = new String[memoryKeys.length];
		for (int i = 0; i < memoryKeys.length; i++) {
			MemoryKey<?> memoryKey = memoryKeys[i];
			patterns[i] = "[the] " + memoryKey.getKey().getKey().replace('_', ' ').toLowerCase(Locale.ENGLISH)
				+ " memory of %livingentities%";
		}
		Skript.adminBroadcast("Patterns: " + Arrays.toString(patterns));
		Skript.registerExpression(ExprEntityMemory.class, Object.class, ExpressionType.PROPERTY, patterns);
	}

	private MemoryKey<?> selectedMemory;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedMemory = memoryKeys[matchedPattern];
		//noinspection unchecked
		setExpr((Expression<? extends LivingEntity>) exprs[0]);
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event, LivingEntity[] source) {
		return get(source, entity -> {
			try {
                return entity.getMemory(selectedMemory);
			} catch (Exception ignored) {}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			if (selectedMemory.getMemoryClass().isAssignableFrom(Number.class))
				return CollectionUtils.array(selectedMemory.getMemoryClass());
		} else if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			return CollectionUtils.array(selectedMemory.getMemoryClass());
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Class<?> memoryClass = selectedMemory.getMemoryClass();
		Consumer<LivingEntity> consumer = null;
		if (mode == ChangeMode.DELETE) {
			consumer = entity -> {
				entity.setMemory(selectedMemory, null);
			};
		} else {
			MemoryHandler handler = MemoryHandler.getMemoryHandler(memoryClass);
			if (handler == null) {
				throw new IllegalStateException("Unexpected value: " + memoryClass);
			}
			consumer = handler.changer(selectedMemory, mode, delta[0]);
		}
		if (consumer == null)
			return;

		for (LivingEntity entity : getExpr().getArray(event)) {
			consumer.accept(entity);
		}

	}

	@Override
	public Class<?> getReturnType() {
		return selectedMemory.getMemoryClass();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + selectedMemory + " of " + getExpr().toString(event, debug);
	}

}
