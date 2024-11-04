package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ExprEquipCompEntities extends PropertyExpression<Object, EntityType> {

	static {
		Skript.registerExpression(ExprEquipCompEntities.class, EntityType.class, ExpressionType.PROPERTY,
			"[the] [equip[pable] component] allowed entities (of|from) %itemstacks/itemtypes/slots%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected EntityType[] get(Event event, Object[] source) {
		List<EntityType> types = new ArrayList<>();
		for (Object object : getExpr().getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;
			ItemMeta meta = itemStack.getItemMeta();
			EquippableComponent component = meta.getEquippable();
			component.getAllowedEntities().forEach(entityType -> {
				Class<? extends Entity> clazz = entityType.getEntityClass();
				types.add(new EntityType(clazz, 1));
			});
		}
		return types.toArray(new EntityType[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(EntityType.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {

		EntityType[] types = (EntityType[]) delta;
		List<org.bukkit.entity.EntityType> converted = new ArrayList<>();
		if (types != null && types.length > 0) {
			Arrays.stream(types).forEach(entityType -> {
				converted.add(EntityUtils.toBukkitEntityType(entityType.data));
			});
		}

		Consumer<ItemMeta> changeItemMeta = switch (mode) {
			case SET -> itemMeta -> {
				itemMeta.getEquippable().setAllowedEntities(converted);
			};
			case ADD -> itemMeta -> {
				List<org.bukkit.entity.EntityType> current = itemMeta.getEquippable().getAllowedEntities().stream().toList();
				current.addAll(converted);
				itemMeta.getEquippable().setAllowedEntities(current);
			};
			case REMOVE -> itemMeta -> {
				List<org.bukkit.entity.EntityType> current = itemMeta.getEquippable().getAllowedEntities().stream().toList();
				current.removeAll(converted);
				itemMeta.getEquippable().setAllowedEntities(current);
			};
			case DELETE -> itemMeta -> {
				List<org.bukkit.entity.EntityType> empty = new ArrayList<>();
				itemMeta.getEquippable().setAllowedEntities(empty);
			};
			default -> throw new IllegalStateException("Unexpected Value: " +  mode);
		};

		for (Object object : getExpr().getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;
			ItemMeta meta = itemStack.getItemMeta();
			changeItemMeta.accept(meta);
			itemStack.setItemMeta(meta);
			if (object instanceof Slot slot) {
				slot.setItem(itemStack);
			} else if (object instanceof ItemType itemType) {
				itemType.setItemMeta(meta);
			} else if (object instanceof ItemStack itemStack1) {
				itemStack1.setItemMeta(meta);
			}
		}
	}

	@Override
	public Class<EntityType> getReturnType() {
		return EntityType.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the equippable component allowed entities of " + getExpr().toString(event, debug);
	}
}
