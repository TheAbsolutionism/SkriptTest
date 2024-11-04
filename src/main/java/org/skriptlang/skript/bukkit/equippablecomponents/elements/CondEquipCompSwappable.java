package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

@Name("Equippable Component - Is Swappable")
@Description("Checks if the item can be swapped by right clicking in it your hand.")
@Examples({
	"if {_item} is swappable:",
		"\tadd \"Swappable\" to lore of {_item}",
	"",
	"set {_component} to the equippable component of {_item}",
	"if {_component} is not swappable:",
		"\tmake {_component} swappable"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class CondEquipCompSwappable extends PropertyCondition<Object> {

	static {
		Skript.registerCondition(CondEquipCompSwappable.class, ConditionType.PROPERTY,
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (is|are) [:un]swappable",
			"[the] %equippablecomponents% (is|are) [:un]swappable",
			"[the] [equip[pable] component[s] of] %itemstacks/itemtypes/slots% (isn't|is not|aren't|are not) [:un]swappable",
			"[the] %equippablecomponents% (isn't|is not|aren't|are not) [:un]swappable");
	}

	private Expression<?> objects;
	private boolean swappable;
	private boolean isComponents;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		swappable = !parseResult.hasTag("un");
		isComponents = matchedPattern == 1 || matchedPattern == 3;
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(Object object) {
		if (object instanceof EquippableComponent component) {
			return component.isSwappable() == swappable;
		} else {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (itemStack != null)
				return itemStack.getItemMeta().getEquippable().isSwappable() == swappable;
		}
		return isNegated();
	}

	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + (isComponents ? "" : "equippable components of ") + objects.toString(event, debug) + " " +
			(isNegated() ? "are not" : "are") + " " + (swappable ? "swappable" : "unswappable");
	}

}
