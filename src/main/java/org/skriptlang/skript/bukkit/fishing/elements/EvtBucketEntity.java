package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.util.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Name("Bucket Catch Entity")
@Description("Called when a player catches an entity in a bucket.")
@Examples({
	"on bucket catch of a puffer fish:",
		"\tsend \"You caught a fish with a %future event-item%!\" to player"
})
@Since("INSERT VERSION")
public class EvtBucketEntity extends SkriptEvent {

	static {
		Skript.registerEvent("Bucket Catch Entity", EvtBucketEntity.class, PlayerBucketEntityEvent.class,
			"bucket (catch[ing]|captur(e|ing)) [[of] %-entitydatas%]");

		EventValues.registerEventValue(PlayerBucketEntityEvent.class, ItemStack.class, new Getter<>() {
			@Override
			public ItemStack get(PlayerBucketEntityEvent event) {
				return event.getOriginalBucket();
			}
		}, EventValues.TIME_NOW);

		EventValues.registerEventValue(PlayerBucketEntityEvent.class, ItemStack.class, new Getter<>() {
			@Override
			public ItemStack get(PlayerBucketEntityEvent event) {
				return event.getEntityBucket();
			}
		}, EventValues.TIME_FUTURE);

		EventValues.registerEventValue(PlayerBucketEntityEvent.class, Player.class, new Getter<>() {
			@Override
			public Player get(PlayerBucketEntityEvent event) {
				return event.getPlayer();
			}
		}, EventValues.TIME_NOW);

		EventValues.registerEventValue(PlayerBucketEntityEvent.class, Entity.class, new Getter<>() {
			@Override
			public @NotNull Entity get(PlayerBucketEntityEvent event) {
				return event.getEntity();
			}
		}, EventValues.TIME_NOW);
	}

	private EntityData<?>[] entities;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			//noinspection unchecked
			entities = ((Literal<EntityData<?>>) args[0]).getAll();

		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerBucketEntityEvent bucketEvent))
			return false;

		return entities == null || entities.length == 0 || Arrays.stream(entities)
			.map(EntityData::getType)
			.anyMatch(it -> it.isInstance(bucketEvent.getEntity()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "bucket catch" + (entities.length == 0 ? "" :
				" of " + StringUtils.join(List.of(entities), ", ", " and "));
	}

}
