package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Equippable Component Can Take Damage")
@Description("Checks if the items can take damage when the entity wearing them gets hurt.")
@Examples({
	"if the equippable component of diamond chestplate can take damage:",
		"\t"
})
public class CondEquipCompDamage extends Condition {

	static {
		Skript.registerCondition(CondEquipCompDamage.class, ConditionType.PROPERTY,
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (can take|allow) damage",
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (can't take|can not take|disallow) damage");
	}

	private Expression<?> objects;
	private boolean damage;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		objects = exprs[0];
		damage = matchedPattern == 0;
		setNegated(!damage);
		return true;
	}

	@Override
	public boolean check(Event event) {
		boolean finalProvocation = false;
		for (Object object : objects.getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack == null)
				continue;
			if (itemStack.getItemMeta().getEquippable().isDamageOnHurt() == damage) {
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
			+ (isNegated() ? "can not" : "can") + " take damage";
	}

}
