package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffTest extends Effect {

	static {
		Skript.registerEffect(EffTest.class, "load %itemstacks/slots/itemtypes% with %itemtypes%");
	}

	private Expression<Object> items;
	private Expression<ItemType> types;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		items = (Expression<Object>) expressions[0];
		types = (Expression<ItemType>) expressions[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		ArrayList<ItemStack> list = new ArrayList<>();
		for (ItemType type : types.getArray(event)) {
			ItemStack thisItem = new ItemStack(type.getMaterial());
			for (int i = 0; i < thisItem.getAmount(); i++) {
				list.add(thisItem);
			}
		}
		List<ItemStack> list1 = list.subList(0, list.size());
		Skript.adminBroadcast("List: " + list1);
		for (Object object : items.getArray(event)) {
			ItemStack itemStack = ItemUtils.asItemStack(object);
			if (!(itemStack.getItemMeta() instanceof CrossbowMeta meta))
				continue;
			meta.setChargedProjectiles(list1);
			if (object instanceof Slot slot) {
				itemStack.setItemMeta(meta);
				slot.setItem(itemStack);
			} else if (object instanceof ItemType crossbow) {
				crossbow.setItemMeta(meta);
			} else if (object instanceof ItemStack crossbow) {
				crossbow.setItemMeta(meta);
			}

		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
