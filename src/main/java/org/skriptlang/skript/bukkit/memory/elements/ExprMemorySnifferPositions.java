package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Explored Positions")
@Description("The explored positions of a sniffer.")
@Examples({
	"broadcast the sniffer explored positions memory of last spawned sniffer",
	"set the sniffer explored positions memory of last spawned sniffer to location(0, 0, 0)"
})
@Since("INSERT VERSION")
public class ExprMemorySnifferPositions extends SimplePropertyExpression<LivingEntity, Location> {

	private static final MemoryKey<Location> MEMORY_KEY = MemoryKey.SNIFFER_EXPLORED_POSITIONS;

	static {
		registerDefault(ExprMemorySnifferPositions.class, Location.class, "sniffer explored positions memory", "livingentities");
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
			return CollectionUtils.array(Location.class, Block.class);
		return null;
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
		return "sniffer explored positions memory";
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

}
