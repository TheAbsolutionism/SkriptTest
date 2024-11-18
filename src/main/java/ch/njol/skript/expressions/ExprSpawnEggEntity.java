package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.Nullable;

@Name("Spawn Egg Entity")
@Description("The entity to spawn from a spawn egg.")
@Examples({
	"set {_item} to a zombie spawn egg",
	"broadcast the spawn egg entity of {_item}",
	"",
	"create a new entity snapshot from a zombie and store it in {_snapshot}:",
		"\tset the max health of entity to 30",
		"\tset the health of entity to 30",
	"set the spawn egg entity of {_item} to {_snapshot}"
})
@RequiredPlugins("Minecraft 1.20.2+")
@Since("INSERT VERSION")
public class ExprSpawnEggEntity extends PropertyExpression<Object, EntitySnapshot> {

	static {
		if (Skript.classExists("org.bukkit.entity.EntitySnapshot") && Skript.methodExists(SpawnEggMeta.class, "setSpawnedEntity", EntitySnapshot.class)) {
			Skript.registerExpression(ExprSpawnEggEntity.class, EntitySnapshot.class, ExpressionType.PROPERTY,
				"[the] spawn egg entity of %itemstacks/itemtypes/slots%");
		}
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected EntitySnapshot[] get(Event event, Object[] source) {
		return get(source, object -> {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null || !(itemStack.getItemMeta() instanceof SpawnEggMeta eggMeta))
				return null;
			return eggMeta.getSpawnedEntity();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EntitySnapshot.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		EntitySnapshot snapshot = (EntitySnapshot) (delta != null ? delta[0] : null);
		if (snapshot != null) {
			for (Object object : getExpr().getArray(event)) {
				ItemStack item = ItemUtils.asItemStack(object);
				if (item == null || !(item.getItemMeta() instanceof SpawnEggMeta eggMeta))
					continue;
				eggMeta.setSpawnedEntity(snapshot);
				if (object instanceof Slot slot) {
					item.setItemMeta(eggMeta);
					slot.setItem(item);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(eggMeta);
				} else if (object instanceof ItemStack itemStack) {
					itemStack.setItemMeta(eggMeta);
				}
			}
		}
	}

	@Override
	public Class<EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the spawn egg entity of " + getExpr().toString(event, debug);
	}
}
