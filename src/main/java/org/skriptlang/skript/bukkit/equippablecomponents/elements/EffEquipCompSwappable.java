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

public class EffEquipCompSwappable extends Effect {

	static {
		Skript.registerEffect(EffEquipCompSwappable.class,
			"(set|make) [the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% [to be] swappable",
			"(set|make) [the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% [to be] unswappable");
	}

	private Expression<?> objects;
	private boolean swappable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		swappable = matchedPattern == 0;
		return false;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : objects.getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;
			ItemMeta meta = itemStack.getItemMeta();
			meta.getEquippable().setSwappable(swappable);
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
		return "set the equippable components of " + objects.toString(event, debug) + " to be "
			+ (swappable ? "swappable" : "unswappable");
	}
}
