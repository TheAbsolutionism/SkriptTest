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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Furnace Slot")
@Description({
	"A slot of a furnace, i.e. either the ore, fuel or result slot.",
	"If used within any furnace event, you do not have to provide 'of event-block'."
})
@Examples({
	"set the fuel slot of the clicked block to a lava bucket",
	"set the block's ore slot to 64 iron ore",
	"clear the result slot of the block",
	"on smelt:",
		"\tif the fuel slot is charcoal:",
			"\t\tadd 5 seconds to the burn time"
})
@Events({"smelt", "fuel burn"})
@Since("1.0, 2.8.0 (syntax rework)")
public class ExprFurnaceSlot extends SimpleExpression<Slot> {

	private static final int ORE = 0, FUEL = 1, RESULT = 2;

	static {
		Skript.registerExpression(ExprFurnaceSlot.class, Slot.class, ExpressionType.PROPERTY,
			"[the] (0:ore|1:fuel|2:result) slot[s] [of %blocks%]"
		);
	}


	private @Nullable Expression<Block> blocks;
	private boolean isEvent;
	private int slot;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {

		if (exprs[0] != null) {
			blocks = (Expression<Block>) exprs[0];
		} else {
			if (!getParser().isCurrentEvent(FurnaceBurnEvent.class, FurnaceStartSmeltEvent.class, FurnaceExtractEvent.class, FurnaceSmeltEvent.class)) {
				Skript.error("There's no furnace in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			isEvent = true;
		}
		slot = parseResult.mark;
		return true;
	}

	@Override
	protected Slot @Nullable [] get(Event event) {
		Block[] blocks;
		if (isEvent) {
			blocks = new Block[1];
			if (event instanceof FurnaceSmeltEvent smeltEvent) {
				blocks[0] = smeltEvent.getBlock();
			} else if (event instanceof FurnaceBurnEvent burnEvent) {
				blocks[0] = burnEvent.getBlock();
			} else if (event instanceof FurnaceStartSmeltEvent startEvent) {
				blocks[0] = startEvent.getBlock();
			} else if (event instanceof FurnaceExtractEvent extractEvent) {
				blocks[0] = extractEvent.getBlock();
			} else {
				return new Slot[0];
			}
		} else {
			assert this.blocks != null;
			blocks = this.blocks.getArray(event);
		}

		List<Slot> slots = new ArrayList<>();
		for (Block block : blocks) {
			BlockState state = block.getState();
			if (!(state instanceof Furnace))
				continue;
			FurnaceInventory furnaceInventory = ((Furnace) state).getInventory();
			slots.add(new InventorySlot(furnaceInventory, slot));
		}
		return slots.toArray(new Slot[0]);
	}

	@Override
	public boolean isSingle() {
		if (isEvent)
			return true;
		assert blocks != null;
		return blocks.isSingle();
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return InventorySlot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String result = "";
		switch (slot) {
			case ORE -> {result = "ore slot";}
			case FUEL -> {result = "fuel slot";}
			case RESULT -> {result = "result slot";}
		}
		if (isEvent) {
			result += " of " + event.getEventName();
		} else {
			result += " of " + blocks.toString(event, debug);
		}
		return result;
	}



}