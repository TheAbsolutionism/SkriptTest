package ch.njol.skript.events;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Relation;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ClickEventTracker;
import ch.njol.skript.classes.data.DefaultComparators;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.coll.CollectionUtils;

public class EvtClick extends SkriptEvent {

	/**
	 * Click types.
	 */
	private final static int RIGHT = 1, LEFT = 2, ANY = RIGHT | LEFT;

	/**
	 * Tracks PlayerInteractEvents to deduplicate them.
	 */
	public static final ClickEventTracker interactTracker = new ClickEventTracker(Skript.getInstance());

	static {
		Class<? extends PlayerEvent>[] eventTypes = CollectionUtils.array(
			PlayerInteractEvent.class, PlayerInteractEntityEvent.class, PlayerInteractAtEntityEvent.class
		);
		Skript.registerEvent("Click", EvtClick.class, eventTypes,
				"[(" + RIGHT + ":right|" + LEFT + ":left)(| |-)][mouse(| |-)]click[ing] [on %-entitydata/itemtype/blockdata%] [(with|using|holding) %-itemtype%]",
				"[(" + RIGHT + ":right|" + LEFT + ":left)(| |-)][mouse(| |-)]click[ing] (with|using|holding) %itemtype% on %entitydata/itemtype/blockdata%")
				.description("Called when a user clicks on a block, an entity or air with or without an item in their hand.",
						"Please note that rightclick events with an empty hand while not looking at a block are not sent to the server, so there's no way to detect them.",
						"Also note that a leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event.")
				.examples("on click:",
						"on rightclick holding a fishing rod:",
						"on leftclick on a stone or obsidian:",
						"on rightclick on a creeper:",
						"on click with a sword:",
						"on click on chest[facing=north]:")
				.since("1.0, INSERT VERSION (blockdata)");
	}

	/**
	 * Only trigger when one of these is interacted with.
	 */
	private @Nullable Literal<?> type;

	/**
	 * Only trigger when then item player clicks with is one of these.
	 */
	private @Nullable Literal<ItemType> tools;

	/**
	 * Click types to trigger.
	 */
	private int click = ANY;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		click = parseResult.mark == 0 ? ANY : parseResult.mark;
		type = args[matchedPattern];
		if (type != null && !ItemType.class.isAssignableFrom(type.getReturnType()) && !BlockData.class.isAssignableFrom(type.getReturnType())) {
			Literal<EntityData<?>> entitydata = (Literal<EntityData<?>>) type;
			if (click == LEFT) {
				if (Vehicle.class.isAssignableFrom(entitydata.getSingle().getType())) {
					Skript.error("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'vehicle damage' event.");
				} else {
					Skript.error("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event.");
				}
				return false;
			} else if (click == ANY) {
				if (Vehicle.class.isAssignableFrom(entitydata.getSingle().getType())) {
					Skript.error("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'vehicle damage' event. " +
							"Change this event to a rightclick to fix this warning message.");
				} else {
					Skript.error("A leftclick on an entity is an attack and thus not covered by the 'click' event, but the 'damage' event. " +
							"Change this event to a rightclick to fix this warning message.");
				}
			}
		}
		tools = (Literal<ItemType>) args[1 - matchedPattern];
		return true;
	}

	@Override
	public boolean check(Event event) {
		Block block;
		Entity entity;
		
		if (event instanceof PlayerInteractEntityEvent interactEntityEvent) {
			Entity clicked = interactEntityEvent.getRightClicked();
			
			// Usually, don't handle these events
			if (interactEntityEvent instanceof PlayerInteractAtEntityEvent) {
				// But armor stands are an exception
				// Later, there may be more exceptions...
				if (!(clicked instanceof ArmorStand))
					return false;
			}
			
			if (click == LEFT) // Lefts clicks on entities don't work
				return false;
			
			// PlayerInteractAtEntityEvent called only once for armor stands
			if (!(event instanceof PlayerInteractAtEntityEvent)) {
				if (!interactTracker.checkEvent(interactEntityEvent.getPlayer(), interactEntityEvent, interactEntityEvent.getHand())) {
					return false; // Not first event this tick
				}
			}
			
			entity = clicked;
			block = null;
		} else if (event instanceof PlayerInteractEvent interactEvent) {
			// Figure out click type, filter non-click events
			Action a = interactEvent.getAction();
			int click;
			switch (a) {
				case LEFT_CLICK_AIR:
				case LEFT_CLICK_BLOCK:
					click = LEFT;
					break;
				case RIGHT_CLICK_AIR:
				case RIGHT_CLICK_BLOCK:
					click = RIGHT;
					break;
				case PHYSICAL: // Not a click event
				default:
					return false;
			}
			if ((this.click & click) == 0)
				return false; // We don't want to handle this kind of events
			
			EquipmentSlot hand = interactEvent.getHand();
			assert hand != null; // Not PHYSICAL interaction
			if (!interactTracker.checkEvent(interactEvent.getPlayer(), interactEvent, hand)) {
				return false; // Not first event this tick
			}
			
			block = interactEvent.getClickedBlock();
			entity = null;
		} else {
			assert false;
			return false;
		}
		
		if (tools != null && !tools.check(event, new Checker<ItemType>() {
			@Override
			public boolean check(final ItemType t) {
				if (event instanceof PlayerInteractEvent interactEvent) {
					return t.isOfType(interactEvent.getItem());
				} else { // PlayerInteractEntityEvent doesn't have item associated with it
					PlayerInventory invi = ((PlayerInteractEntityEvent) event).getPlayer().getInventory();
					ItemStack item = ((PlayerInteractEntityEvent) event).getHand() == EquipmentSlot.HAND
							? invi.getItemInMainHand() : invi.getItemInOffHand();
					return t.isOfType(item);
				}
			}
		})) {
			return false;
		}
		
		if (type != null) {
			BlockData blockDataCheck = block != null ? block.getBlockData() : null;
			return type.check(event, new Checker<Object>() {
				@Override
				public boolean check(final Object o) {
					if (entity != null) {
						return o instanceof EntityData ? ((EntityData<?>) o).isInstance(entity) : Relation.EQUAL.isImpliedBy(DefaultComparators.entityItemComparator.compare(EntityData.fromEntity(entity), (ItemType) o));
					} else if (o instanceof ItemType itemType) {
						return itemType.isOfType(block);
					} else if (blockDataCheck != null && o instanceof BlockData blockData)  {
						return blockDataCheck.matches(blockData);
					}
					return false;
				}
			});
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (click == LEFT ? "left" : click == RIGHT ? "right" : "") + "click" + (type != null ? " on " + type.toString(event, debug) : "") + (tools != null ? " holding " + tools.toString(event, debug) : "");
	}

}
