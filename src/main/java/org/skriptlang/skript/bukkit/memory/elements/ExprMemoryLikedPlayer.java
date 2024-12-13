package org.skriptlang.skript.bukkit.memory.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Liked Player")
@Description("The player that an allay likes.")
@Examples({
	"broadcast the liked player memory of last spawned allay",
	"set the liked player memory of last spawned allay to {_player}"
})
@Since("INSERT VERSION")
public class ExprMemoryLikedPlayer extends SimplePropertyExpression<LivingEntity, OfflinePlayer> {

	private static final MemoryKey<UUID> MEMORY_KEY = MemoryKey.LIKED_PLAYER;

	static {
		registerDefault(ExprMemoryLikedPlayer.class, OfflinePlayer.class, "liked player memory", "livingentities");
	}

	@Override
	public @Nullable OfflinePlayer convert(LivingEntity entity) {
		try {
			UUID uuid = entity.getMemory(MEMORY_KEY);
			if (uuid == null)
				return null;
			return Bukkit.getOfflinePlayer(uuid);
		} catch (Exception ignored) {}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(OfflinePlayer.class, UUID.class, String.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		UUID uuid = null;
		if (delta != null) {
			if (delta[0] instanceof OfflinePlayer offlinePlayer) {
				uuid = offlinePlayer.getUniqueId();
			} else if (delta[0] instanceof UUID uuid1) {
				uuid = uuid1;
			} else if (delta[0] instanceof String string) {
				uuid = UUID.fromString(string);
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
		return "liked player memory";
	}

	@Override
	public Class<OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

}
