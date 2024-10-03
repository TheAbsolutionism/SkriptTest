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
package ch.njol.skript.effects;

import java.util.Locale;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Open/Close Inventory")
@Description({"Opens an inventory to a player. The player can then access and modify the inventory as if it was a chest that he just opened.",
		"Please note that currently 'show' and 'open' have the same effect, but 'show' will eventually show an unmodifiable view of the inventory in the future."})
@Examples({"show the victim's inventory to the player",
		"open the player's inventory for the player"})
@Since("2.0, 2.1.1 (closing), 2.2-Fixes-V10 (anvil), 2.4 (hopper, dropper, dispenser")
public class EffOpenInventory extends Effect {

	private static final boolean PAPER_ANVIL = Skript.methodExists(HumanEntity.class, "openAnvil");
	private static final boolean PAPER_CARTOGRAPHY = Skript.methodExists(HumanEntity.class, "openCartographyTable");
	private static final boolean PAPER_GRINDSTONE = Skript.methodExists(HumanEntity.class, "openGrindstone");
	private static final boolean PAPER_LOOM = Skript.methodExists(HumanEntity.class, "openLoom");
	private static final boolean PAPER_SMITHING = Skript.methodExists(HumanEntity.class, "openSmithingTable");
	private static final boolean PAPER_STONECUTTER = Skript.methodExists(HumanEntity.class, "openStonecutter");
	
	static {
		Skript.registerEffect(EffOpenInventory.class,
				"(open|:show) %inventory/inventorytype%) (to|for) %players%",
				"close [the] inventory [view] (to|of|for) %players%", "close %players%'[s] inventory [view]");
	}
	

	private @Nullable Expression<?> providedInv;
	
	boolean open;
	
	@SuppressWarnings("null")
	private Expression<Player> players;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (matchedPattern == 0) {
			open = true;
			players = (Expression<Player>) exprs[1];
		} else {
			players = (Expression<Player>) exprs[0];
		}
		providedInv = open ? exprs[0] : null;
		boolean showSyntax = parseResult.hasTag("show");
		if (showSyntax) {
			Skript.warning("Using 'show' inventory instead of 'open' is not recommended as it will eventually show an unmodifiable view of the inventory in the future.");
		}
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		Consumer<Player> changer = null;
		if (providedInv != null) {
			InventoryType invType;

			Object object = providedInv.getSingle(e);
			if (object instanceof Inventory inventory) {
				invType = inventory.getType();
				changer = player -> player.openInventory(inventory);
			} else if (object instanceof InventoryType inventoryType) {
				invType = inventoryType;
				changer = getInventoryChanger(inventoryType);
			} else {
				return;
			}
			if (changer == null)
				return;

			for (final Player p : players.getArray(e)) {
				try {
					changer.accept(p);
				} catch (IllegalArgumentException ex){
					Skript.error("You can't open a " + invType.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "") + " inventory to a player.");
				}
			}
		} else {
			for (final Player player : players.getArray(e)) {
				player.closeInventory();
			}
		}
	}

	private static Consumer<Player> getInventoryChanger(InventoryType inventoryType) {
		switch (inventoryType) {
			case ANVIL -> {
				if (PAPER_ANVIL)
					return player -> player.openAnvil(null, true);
			}
			case CRAFTING,WORKBENCH -> {
				return player -> player.openWorkbench(null, true);
			}
			case ENCHANTING -> {
				return player -> player.openEnchanting(null, true);
			}
			case CARTOGRAPHY -> {
				if (PAPER_CARTOGRAPHY)
					return player -> player.openCartographyTable(null, true);
			}
			case GRINDSTONE -> {
				if (PAPER_GRINDSTONE)
					return player -> player.openGrindstone(null, true);
			}
			case LOOM -> {
				if (PAPER_LOOM)
					return player -> player.openLoom(null, true);
			}
			case STONECUTTER -> {
				if (PAPER_STONECUTTER)
					return player -> player.openSmithingTable(null, true);
			}
			case SMITHING -> {
				if (PAPER_SMITHING)
					return player -> player.openSmithingTable(null, true);
			}
		}
		return player -> player.openInventory(Bukkit.createInventory(player, inventoryType));
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (open ? "open " + (providedInv != null ? providedInv.toString(e, debug) : "") + " to " : "close inventory view of ") + players.toString(e, debug);
	}
	
}
