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

	enum InventoryShorts {
		WORKBENCH("(crafting [table]|workbench)", InventoryType.WORKBENCH),
		CHEST("chest", InventoryType.CHEST),
		ANVIL("anvil", InventoryType.ANVIL),
		HOPPER("hopper", InventoryType.HOPPER),
		DROPPER("dropper", InventoryType.DROPPER),
		DISPENSER("dispenser", InventoryType.DISPENSER);

		private final String pattern;
		private final InventoryType invType;

		InventoryShorts(String pattern, InventoryType invType) {
			this.pattern = "(open|:show) " + pattern + "(view|window|inventory|) (to|for) %players%";
			this.invType = invType;
		}

	}

	private static final InventoryShorts[] invShorts = InventoryShorts.values();
	private static final int shortSize = invShorts.length;
	
	static {
		String[] patterns = new String[shortSize + 3];
		for (InventoryShorts invShort : invShorts){
			patterns[invShort.ordinal()] =  invShort.pattern;
		}
		patterns[shortSize + 1] = "(open|:show) (%inventory%/%inventorytype%) (to|for) %players%";
		patterns[shortSize + 2] = "close [the] inventory [view] (to|of|for) %players%";
		patterns[shortSize + 3] = "close %players%'[s] inventory [view]";

		Skript.registerEffect(EffOpenInventory.class, patterns);
	}

	private @Nullable Expression<?> providedInv;
	private @Nullable InventoryType providedType;
	boolean open;
	private Expression<Player> players;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (matchedPattern <= shortSize + 1) {
			open = true;
			if (matchedPattern == shortSize + 1) {
				providedInv = exprs[0];
				players = (Expression<Player>) exprs[1];
			} else {
				providedType = invShorts[matchedPattern].invType;
				players = (Expression<Player>) exprs[0];
			}
			boolean showSyntax = parseResult.hasTag("show");
			if (showSyntax) {
				Skript.warning("Using 'show' inventory instead of 'open' is not recommended as it will eventually show an unmodifiable view of the inventory in the future.");
			}
		} else {
			players = (Expression<Player>) exprs[0];
		}
		return true;
	}
	
	@Override
	protected void execute(final Event event) {
		if (open) {
			Consumer<Player> changer = null;
			InventoryType invType;
			if (providedInv != null) {
				Object object = providedInv.getSingle(event);
				if (object instanceof Inventory inventory) {
					invType = inventory.getType();
					changer = player -> player.openInventory(inventory);
				} else if (object instanceof InventoryType inventoryType) {
					invType = inventoryType;
					changer = getInventoryChanger(inventoryType);
				} else {
					return;
				}
			} else if (providedType != null) {
				invType = providedType;
				changer = getInventoryChanger(providedType);
			} else {
				return;
			}
			if (changer == null)
				return;

			for (Player player : players.getArray(event)) {
				try {
					changer.accept(player);
				} catch (IllegalArgumentException ex){
					Skript.error("You can't open a " + invType.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "") + " inventory to a player.");
				}
			}
		} else {
			for (Player player : players.getArray(event)) {
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
			case WORKBENCH -> {
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
	public String toString(@Nullable Event event, boolean debug) {
		return (open ?
			"open " + (providedInv != null ? providedInv.toString(event, debug) : "") + (providedType != null ? providedType.toString() : "")
			: "close inventory view of") + players.toString(event, debug);
	}
	
}
