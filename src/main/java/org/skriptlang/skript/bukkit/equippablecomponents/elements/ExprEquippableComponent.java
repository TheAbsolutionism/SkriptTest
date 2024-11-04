package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

public class ExprEquippableComponent extends PropertyExpression<Object, EquippableComponent> {

	static {
		register(ExprEquippableComponent.class,  EquippableComponent.class, "equip[pable] component", "itemstacks/itemtypes/slots");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected EquippableComponent @Nullable [] get(Event event, Object[] source) {
		return get(source, object -> {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				return null;
			return itemStack.getItemMeta().getEquippable();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EquippableComponent.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;
		EquippableComponent equipComp = (EquippableComponent) delta[0];
		if (equipComp == null)
			return;
		for (Object object : getExpr().getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;
			ItemMeta meta = itemStack.getItemMeta();
			meta.setEquippable(equipComp);
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
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<EquippableComponent> getReturnType() {
		return EquippableComponent.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the equippable component of " + getExpr().toString(event, debug);
	}

}
