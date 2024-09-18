package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.jetbrains.annotations.Nullable;

public class EvtFurnace extends SkriptEvent {

	static {
		Skript.registerEvent("Smelt", EvtFurnace.class, FurnaceSmeltEvent.class, "[furnace] smelt[ing] [of %-itemtypes%]")
			.description("Called when a furnace smelts an item in its <a href='expressions.html#ExprFurnaceSlot'>ore slot</a>.")
			.examples(
				"on smelt:",
				"on smelt of raw iron:",
					"\tbroadcast "
			)
			.since("1.0, INSERT VERSION (Of ItemType)");
		Skript.registerEvent("Fuel Burn", EvtFurnace.class, FurnaceBurnEvent.class, "[furnace] fuel burn[ing] [of %-itemtypes%]")
			.description("Called when a furnace burns an item from its <a href='expressions.html#ExprFurnaceSlot'>fuel slot</a>.")
			.examples("on fuel burning:")
			.since("1.0, INSERT VERSION (Of ItemType)");
		Skript.registerEvent("Furnace Extract", EvtFurnace.class, FurnaceExtractEvent.class, "furnace extract[ing] [of %-itemtypes%]")
			.description("Called when a player takes any item out of the furnace.")
			.examples(
				"on furnace extract:",
					"\tif event-items is an iron ingot:",
						"\t\tremove event-items from event-player's inventory"
			)
			.since("INSERT VERSION");
		Skript.registerEvent("Start Smelt", EvtFurnace.class, FurnaceStartSmeltEvent.class,
			"[furnace] start smelt[ing] [of %-itemtypes%]",
			"[furnace] smelt[ing] start [of %-itemtypes%]")
			.description("Called when a furnace starts smelting an item in its ...")
			.examples("on furnace start smelting of raw iron:")
			.since("INSERT VERSION");
	}

	private @Nullable Literal<Object> types;

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null)
			types = (Literal<Object>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (types == null)
			return true;

		ItemType item;

		if (event instanceof FurnaceSmeltEvent smeltEvent) {
			item = new ItemType(smeltEvent.getSource());
		} else if (event instanceof FurnaceBurnEvent burnEvent) {
			item = new ItemType(burnEvent.getFuel());
		} else if (event instanceof FurnaceExtractEvent extractEvent) {
			item = new ItemType(extractEvent.getItemType());
		} else if (event instanceof FurnaceStartSmeltEvent startEvent) {
			item = new ItemType(startEvent.getSource());
		} else {
			assert false;
			return false;
		}

		return types.check(event, o -> {
			if (o instanceof ItemType)
				return ((ItemType) o).isSupertypeOf(item);
			return false;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "smelt/burn/extract/start smelt of " + Classes.toString(types);
	}

}
