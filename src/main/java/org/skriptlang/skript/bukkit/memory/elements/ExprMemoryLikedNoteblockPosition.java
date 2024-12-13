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

@Name("Liked Noteblock")
@Description("The location of a noteblock that an allay likes.")
@Examples({
	"broadcast the liked noteblock location memory of last spawned allay",
	"set the liked noteblock position memory of last spawned allay to location(0, 0, 0)"
})
@Since("INSERT VERSION")
public class ExprMemoryLikedNoteblockPosition extends SimplePropertyExpression<LivingEntity, Location> {

	private final static MemoryKey<Location> MEMORY_KEY = MemoryKey.LIKED_NOTEBLOCK_POSITION;

	static {
		registerDefault(ExprMemoryLikedNoteblockPosition.class, Location.class, "liked note[ ]block (position|location) memory", "livingentities");
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
		return "liked noteblock position memory";
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

}
