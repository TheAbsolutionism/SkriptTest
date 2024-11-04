package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CondEquipCompSwappable extends Condition {

	static {
		Skript.registerCondition(CondEquipCompSwappable.class, ConditionType.PROPERTY,
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (is|are) [:un]swappable",
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (isn't|is not|aren't|are not) [:un]swappable");
	}

	private Expression<?> objects;
	private boolean swappable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		swappable = !parseResult.hasTag("un");
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		boolean finalProvocation = false;
		for (Object object : objects.getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;
			if (itemStack.getItemMeta().getEquippable().isSwappable() == swappable) {
				finalProvocation = true;
			} else {
				finalProvocation = false;
				break;
			}
		}
		return finalProvocation;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the equippable components of " + objects.toString(event, debug)
			+ (isNegated() ? "are not" : "are") + (swappable ? "swappable" : "unswappable");
	}

}
