package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Get Entity Snapshot")
@Description({
	"Get an entity snapshot of an entity. Contains data from entity i.e max health",
	"NOTE: Data of an entity snapshot can not be viewed nor modified."
})
@Examples({
	"spawn a pig at location(0,0,0):",
		"\tset the max health of entity to 20",
		"\tset the health of entity to 20",
		"\tset {_snapshot} to the entity snapshot of entity",
		"\tclear entity",
	"spawn {_snapshot} at location(0,0,0)"
})
@RequiredPlugins("Minecraft 1.20.2+")
@Since("INSERT VERSION")
public class ExprEntitySnapshot extends SimpleExpression<EntitySnapshot> {

	static {
		if (Skript.classExists("org.bukkit.entity.EntitySnapshot")) {
			Skript.registerExpression(ExprEntitySnapshot.class, EntitySnapshot.class, ExpressionType.SIMPLE,
				"[the] entity snapshot[s] of %entities%",
				"%entities%'[s] entity snapshot[s]");
		}
	}

	private Expression<Entity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected EntitySnapshot @Nullable [] get(Event event) {
		return entities.stream(event).map(Entity::createSnapshot).toArray(EntitySnapshot[]::new);
	}

	@Override
	public boolean isSingle() {
		return entities.isSingle();
	}

	@Override
	public Class<EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the entity snapshot of " + entities.toString(event, debug);
	}

}
