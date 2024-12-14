package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.jetbrains.annotations.Nullable;

public class EvtVehicleCollision extends SkriptEvent {

	static {
		Skript.registerEvent("Vehicle Collision", EvtVehicleCollision.class, new Class[]{VehicleBlockCollisionEvent.class, VehicleEntityCollisionEvent.class}
			,"vehicle collision [(with|of) [a[n]] %-itemtypes/blockdatas/entitydatas%]",
			"vehicle block collision [(with|of) [a[n]] %-itemtypes/blockdatas%]",
			"vehicle entity collision [(with|of) [a[n]] %-entitydatas%]")
				.description("Called when a vehicle collides with a block or entity.")
				.examples("on vehicle collision:", "on vehicle collision with obsidian:", "on vehicle collision with a zombie:")
				.since("INSERT VERSION");
	}

	private Literal<?> expr;
	private boolean blockCollision;
	private boolean entityCollision;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		expr = args[0];
		blockCollision = matchedPattern == 1;
		entityCollision = matchedPattern == 2;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof VehicleCollisionEvent collisionEvent))
			return false;

		if (expr == null) {
			if (blockCollision && !(event instanceof VehicleBlockCollisionEvent)) {
				return false;
			} else if (entityCollision && !(event instanceof VehicleEntityCollisionEvent)) {
				return false;
			}
			return true;
		}

		ItemType item = null;
		BlockData blockData = null;
		EntityData<?> entityData = null;
		if (collisionEvent instanceof VehicleBlockCollisionEvent blockCollisionEvent) {
			Block block = blockCollisionEvent.getBlock();
			item = new ItemType(block.getType());
			blockData = block.getBlockData();
		} else if (collisionEvent instanceof VehicleEntityCollisionEvent entityCollisionEvent) {
			entityData = EntityData.fromEntity(entityCollisionEvent.getEntity());
		} else {
			return false;
		}

		ItemType finalItem = item;
		BlockData finalBlockData = blockData;
		EntityData<?> finalEntityData = entityData;

		return expr.check(event, object -> {
			if (object instanceof ItemType itemType && finalItem != null) {
				return itemType.isSupertypeOf(finalItem);
			} else if (object instanceof BlockData blockData1 && finalBlockData != null) {
				return finalBlockData.matches(blockData1);
			} else if (object instanceof EntityData<?> entityData1 && finalEntityData != null) {
				return entityData1.isSupertypeOf(finalEntityData);
			}
			return false;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vehicle collision " + (expr != null ? "of " + expr.toString(event, debug) : "");
	}

}
