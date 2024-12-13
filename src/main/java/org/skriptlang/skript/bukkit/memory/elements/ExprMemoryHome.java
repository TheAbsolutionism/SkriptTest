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
@Name("Entity Home")
@Description("The home location of a villager.")
@Examples({
	"broadcast the home memory of last spawned villager",
	"set the home memory of last spawned villager to location(0, 0, 0)"
})
@Since("INSERT VERSION")
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
		return "home memory";
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

}
