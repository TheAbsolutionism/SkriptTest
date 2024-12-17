package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Item Thrower")
@Description("The player that thrown the dropped item.")
@Examples({
	"broadcast the item thrower of all dropped items"
})
@Since("INSERT VERSION")
public class ExprItemThrower extends SimplePropertyExpression<Item, Object> {

	static {
		registerDefault(ExprItemThrower.class, Object.class, "[dropped] item thrower", "itementities");
	}

	@Override
	public @Nullable Object convert(Item item) {
		UUID uuid = item.getThrower();
		if (uuid == null)
			return null;
		Entity checkEntity = Bukkit.getEntity(uuid);
		if (checkEntity != null)
			return checkEntity;
		return Bukkit.getOfflinePlayer(uuid);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(OfflinePlayer.class, Entity.class, String.class, UUID.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		UUID newId = null;
		if (delta != null) {
			if (delta[0] instanceof OfflinePlayer offlinePlayer) {
				newId = offlinePlayer.getUniqueId();
			} else if (delta[0] instanceof Entity entity) {
				newId = entity.getUniqueId();
			} else if (delta[0] instanceof UUID uuid) {
				newId = uuid;
			} else if (delta[0] instanceof String string) {
				newId = UUID.fromString(string);
			}
		}

		for (Item item : getExpr().getArray(event)) {
			item.setThrower(newId);
		}

	}

	@Override
	protected String getPropertyName() {
		return "dropped item thrower";
	}

	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}

}
