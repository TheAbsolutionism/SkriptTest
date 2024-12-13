package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Angry At")
@Description("The entity that a piglin or piglin brute is angry at.")
@Examples({
	"broadcast the angry at memory of last spawned piglin",
	"set the angry at memory of last spawned piglin brute to last spawned wither"
})
@Since("INSERT VERSION")
public class ExprMemoryAngryAt extends SimplePropertyExpression<LivingEntity, Entity> {

	private static final MemoryKey<UUID> MEMORY_KEY = MemoryKey.ANGRY_AT;

	static {
		registerDefault(ExprMemoryAngryAt.class, Entity.class, "angry at memory", "livingentities");
	}

	@Override
	public @Nullable Entity convert(LivingEntity entity) {
		try {
			UUID uuid = entity.getMemory(MEMORY_KEY);
			if (uuid == null)
				return null;
			return Bukkit.getEntity(uuid);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Entity.class, UUID.class, String.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		UUID uuid = null;
		if (delta != null) {
			if (delta[0] instanceof Entity entity) {
				uuid = entity.getUniqueId();
			} else if (delta[0] instanceof UUID uuid1) {
				uuid = uuid1;
			} else if (delta[0] instanceof String string) {
				uuid = UUID.fromString(string);
			} else {
                throw new IllegalArgumentException("Invalid argument: " + delta[0]);
			}
		}

		for (LivingEntity entity : getExpr().getArray(event)) {
			try {
				entity.setMemory(MEMORY_KEY, uuid);
			} catch (Exception ignored) {}
		}

	}

	@Override
	protected String getPropertyName() {
		return "angry at memory";
	}

	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

}
