package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Name("Get Entity Snapshot")
@Description({
	"Gets an entity snapshot of the provided entity or entitydata.",
	"NOTE: Entity snapshots can not be edited after creation."
})
@Examples({
	"create a new entity snapshot from a zombie and store it in {_snapshot}:",
		"\tset the max health of entity to 30",
		"\tset the health of entity to 30",
	"set {_item} to a skeleton spawn egg",
	"set the spawn egg entity of {_item} to {_snapshot}",
	"",
	"spawn a warden at location(0,0,0):",
		"\tset the max health of entity to 100",
		"\tset {_entity} to entity",
	"create entity snapshot from {_entity}",
	"set the spawn egg entity of {_item} to {_snapshot}",
	"",
	"spawn {_snapshot} at location(0,0,0)"
})
@RequiredPlugins("Minecraft 1.20.2+")
@Since("INSERT VERSION")
public class EffSecEntitySnapshot extends Section {
	// TODO: Change to secspression
	public static class EntitySnapshotEvent extends Event {

		private final Entity entity;

		public EntitySnapshotEvent(Entity entity) {
			this.entity = entity;
		}

		public Entity getEntity() {
			return entity;
		}

		@Override
		@NotNull
		public HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(EffSecEntitySnapshot.class,
			"create [a[n]] [new] entity snapshot from %entity/entitydata% and store (it|the result) in %object%"
		);
		EventValues.registerEventValue(EntitySnapshotEvent.class, Entity.class, new Getter<Entity, EntitySnapshotEvent>() {
			@Override
			public Entity get(EntitySnapshotEvent event) {
				return event.getEntity();
			}
		}, EventValues.TIME_NOW);
	}

	private Expression<?> objects;
	private Expression<?> variable;
	private @Nullable Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		objects = exprs[0];
		variable = exprs[1];
		if (sectionNode != null) {
			AtomicBoolean delayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(sectionNode, "entity snapshot", afterLoading, EntitySnapshotEvent.class);
			if (delayed.get()) {
				Skript.error("Delays can't be used within a Create Entity Snapshot Section");
				return false;
			}
		}
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {

		Object entityObject = objects.getSingle(event);
		Consumer<Entity> consumer = null;
		if (trigger != null) {
			consumer = entity -> {
				EntitySnapshotEvent snapshotEvent = new EntitySnapshotEvent(entity);
				Variables.setLocalVariables(snapshotEvent, Variables.copyLocalVariables(event));
				TriggerItem.walk(trigger, snapshotEvent);
				Variables.setLocalVariables(event, Variables.copyLocalVariables(snapshotEvent));
				Variables.removeLocals(snapshotEvent);
			};
		}
		Entity finalEntity = null;
		if (entityObject instanceof EntityData<?> entityData) {
			finalEntity = entityData.spawn(new Location(Bukkit.getWorlds().get(0), 0, 320, 0));
		} else if (entityObject instanceof Entity entity) {
			finalEntity = entity;
		}
		if (consumer != null) consumer.accept(finalEntity);
		EntitySnapshot snapshot = finalEntity.createSnapshot();
		if (snapshot != null)
			variable.change(event, new EntitySnapshot[]{snapshot}, ChangeMode.SET);

		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "create a new entity snapshot from " + objects.toString(event, debug);
	}

}
