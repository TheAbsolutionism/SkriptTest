package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMemoryHome extends SimplePropertyExpression<LivingEntity, Location> {

	private final static MemoryKey<Location> MEMORY_KEY = MemoryKey.HOME;

	static {
		registerDefault(ExprMemoryHome.class, Location.class, "home memory", "livingentities");
	}

	@Override
	public @Nullable Location convert(LivingEntity entity) {
		try {
			return entity.getMemory(MEMORY_KEY);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Location.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Location location = null;
		if (delta != null) {
			location = (Location) delta[0];
		}

		for (LivingEntity entity : getExpr().getArray(event)) {
			entity.setMemory(MEMORY_KEY, location);
		}

	}

	@Override
	protected String getPropertyName() {
		return "home memory";
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

}
