package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprMemoryMeetingPoint extends SimplePropertyExpression<LivingEntity, Location> {

	private static final MemoryKey<Location> MEMORY_KEY = MemoryKey.MEETING_POINT;

	static {
		registerDefault(ExprMemoryMeetingPoint.class, Location.class, "meet[ing] (point|location) memory", "livingentities");
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
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(Location.class, Block.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Location location = null;
		if (delta != null) {
			if (delta[0] instanceof Location loc) {
				location = loc;
			} else if (delta[0] instanceof Block block) {
				location = block.getLocation();
			}
		}

		for (LivingEntity entity : getExpr().getArray(event)) {
			try {
				entity.setMemory(MEMORY_KEY, location);
			} catch (Exception ignored) {}
		}
	}

	@Override
	protected String getPropertyName() {
		return "meeting point memory";
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

}
