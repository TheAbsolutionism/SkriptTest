/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;


@Name("Furnace Event Values")
@Description({
	"Represents the expressions you can use within furnace events to get special data.",
	"Only 'smelting item' can be changed, the rest are getters."
})
@Examples({
	"on furnace smelt:",
		"\tbroadcast smelted item",
		"\t# Or 'result'",
	"on furnace extract:",
		"\tbroadcast extracted item",
	"on fuel burn:",
		"\tbroadcast fuel burned",
	"on smelting start:",
		"\tbroadcast smelting item",
		"\tclear smelting item"
})
@Events({"smelt", "fuel burn", "smelting start", "furnace extract"})
@Since("INSERT VERSION")
public class ExprFurnaceEventValues extends PropertyExpression<Block, ItemStack> {

	enum FurnaceValues {
		SMELTED("(smelted item|result [item])", FurnaceSmeltEvent.class, "Can only use 'smelted item' in a smelting event."),
		EXTRACTED("extracted item[s]", FurnaceExtractEvent.class, "Can only use 'extracted item' in a furnace extract event."),
		SMELTING("smelting item", FurnaceStartSmeltEvent.class, "Can only use 'smelting item' in a start smelting event"),
		BURNED("fuel burned [item]", FurnaceBurnEvent.class, "Can only use 'fuel burned' in a fuel burning event.");


		private String name, error;
		private Class<? extends Event> clazz;

		FurnaceValues(String name, Class<? extends Event> clazz, String error) {
			this.name = name;
			this.clazz = clazz;
			this.error = error;
		}

	}

	private static final FurnaceValues[] furnaceValues = FurnaceValues.values();

	static {

		int size = furnaceValues.length;
		String[] patterns  = new String[size];
		for (FurnaceValues value : furnaceValues) {
			patterns[value.ordinal()] = "[the] " + value.name;
		}

		Skript.registerExpression(ExprFurnaceEventValues.class, ItemStack.class, ExpressionType.PROPERTY, patterns);
	}

	private FurnaceValues type;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = furnaceValues[matchedPattern];
		if (!getParser().isCurrentEvent(type.clazz)) {
			Skript.error(type.error);
			return false;
		}
		setExpr(new EventValueExpression<>(Block.class));
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event, Block[] source) {
        ItemStack stack = null;
		switch (type) {
            case SMELTING -> {
                stack = ((FurnaceStartSmeltEvent) event).getSource();
            }
            case BURNED -> {
				stack =  ((FurnaceBurnEvent) event).getFuel();
            }
            case SMELTED -> {
				stack = ((FurnaceSmeltEvent) event).getResult();
            }
            case EXTRACTED -> {
                FurnaceExtractEvent extractEvent = (FurnaceExtractEvent) event;
				stack = new ItemStack(extractEvent.getItemType(), extractEvent.getItemAmount());
            }
        };
		return new ItemStack[]{stack};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (type != FurnaceValues.SMELTED) {
			return null;
		}
		switch (mode) {
			case SET, DELETE -> {return CollectionUtils.array(ItemStack.class);}
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof FurnaceSmeltEvent smeltEvent))
			return;

		switch (mode) {
			case SET -> {
				smeltEvent.setResult((ItemStack) delta[0]);
			}
			case DELETE -> {
				smeltEvent.setResult(ItemStack.of(Material.AIR));
			}
		}

	}



	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (type) {
			case SMELTED -> "smelted item";
			case SMELTING -> "smelting item";
			case BURNED -> "fuel burned item";
			case EXTRACTED -> "extracted item";
		};
	}

}
