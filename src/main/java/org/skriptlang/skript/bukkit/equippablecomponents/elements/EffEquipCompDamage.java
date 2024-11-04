package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class EffEquipCompDamage extends Effect {

	static {
		Skript.registerEffect(EffEquipCompDamage.class,
			"(set|make|force) [the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% [to] (allow|take) damage",
			"(set|make|force) [the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% [to] (disallow|not take) damage");
	}

	private Expression<?> objects;
	private boolean damage;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		damage = matchedPattern == 0;
		return false;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : objects.getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;
			ItemMeta meta = itemStack.getItemMeta();
			meta.getEquippable().setDamageOnHurt(damage);
			itemStack.setItemMeta(meta);
			if (object instanceof Slot slot) {
				slot.setItem(itemStack);
			} else if (object instanceof ItemType itemType) {
				itemType.setItemMeta(meta);
			} else if (object instanceof  ItemStack itemStack1) {
				itemStack1.setItemMeta(meta);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "set the equippable components of " + objects.toString(event, debug) + " to "
			+ (damage ? "take damage" : "not take damage");
	}

}
