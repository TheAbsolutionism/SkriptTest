package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Raid;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Raid Raiders")
@Description("The raider type entities of a raid.")
@Examples({
	"set {_raid} to nearest raid from location(0, 0, 0) in radius 5",
	"set {_raiders::*} to the raiders of {_raid}"
})
@Since("INSERT VERSION")
public class ExprRaidRaiders extends PropertyExpression<Raid, Entity> {

	static {
		registerDefault(ExprRaidRaiders.class, Entity.class, "raiders", "raids");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends Raid>) exprs[0]);
		return true;
	}

	@Override
	protected Entity @Nullable [] get(Event event, Raid[] source) {
		List<Entity> entityList = new ArrayList<>();
		for (Raid raid : getExpr().getArray(event)) {
			entityList.addAll(raid.getRaiders());
		}
		return entityList.toArray(new Entity[0]);
	}

	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
