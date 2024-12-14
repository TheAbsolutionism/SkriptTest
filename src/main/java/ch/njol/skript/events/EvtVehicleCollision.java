package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Checker;
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
	private Object[] types = null;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			expr = args[0];
			types = args[0].getArray();
		}
		blockCollision = matchedPattern == 1;
		entityCollision = matchedPattern == 2;
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof VehicleCollisionEvent collisionEvent))
			return false;

		if (types == null) {
			if (blockCollision && !(event instanceof VehicleBlockCollisionEvent)) {
				return false;
			} else if (entityCollision && !(event instanceof VehicleEntityCollisionEvent)) {
				return false;
			}
			return true;
		}

		ItemType initItemType = null;
		BlockData initBlockData = null;
		EntityData<?> initEntityData = null;
		if (collisionEvent instanceof VehicleBlockCollisionEvent blockCollisionEvent) {
			Block block = blockCollisionEvent.getBlock();
			initItemType = new ItemType(block.getType());
			initBlockData = block.getBlockData();
		} else if (collisionEvent instanceof VehicleEntityCollisionEvent entityCollisionEvent) {
			initEntityData = EntityData.fromEntity(entityCollisionEvent.getEntity());
		} else {
			return false;
		}

		ItemType finalItemType = initItemType;
		BlockData finalBlockData = initBlockData;
		EntityData<?> finalEntityData = initEntityData;

		Checker<Object> checker = null;
		if (types instanceof ItemType[] itemTypes && finalItemType != null) {
			checker = object -> {
				ItemType itemType = (ItemType) object;
				return itemType.isSupertypeOf(finalItemType);
			};
		} else if (types instanceof BlockData[] blockDatas && finalBlockData != null) {
			checker = object -> {
				BlockData blockData = (BlockData) object;
				return finalBlockData.matches(blockData);
			};
		} else if (types instanceof EntityData<?>[] entityDatas && finalEntityData != null) {
			checker = object -> {
				EntityData<?> entityData = (EntityData<?>) object;
				return entityData.isSupertypeOf(finalEntityData);
			};
		} else {
			return false;
		}

		return SimpleExpression.check(types, checker, false, expr.getAnd());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vehicle collision " + (expr != null ? "of " + expr.toString(event, debug) : "");
	}

}
