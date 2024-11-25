package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Raider Patrol Target")
@Description("The patrol target of a raider type entity.")
@Examples({
	"spawn a pillager at location(0, 0, 0)",
	"set the patrol target of last spawned pillager to location(10, 0, 0)"
})
@Since("INSERT VERSION")
public class ExprRaiderPatrolTarget extends SimplePropertyExpression<LivingEntity, Location> {

	static {
		register(ExprRaiderPatrolTarget.class, Location.class, "patrol target", "livingentities");
	}

	@Override
	public @Nullable Location convert(LivingEntity entity) {
		if (!(entity instanceof Raider raider))
			return null;
		return raider.getPatrolTarget().getLocation();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Location.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Block block = null;
		if (delta != null) {
			Location location = (Location) delta[0];
			block = location.getBlock();
		}
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Raider raider))
				continue;
			raider.setPatrolTarget(block);
		}
	}

	@Override
	protected String getPropertyName() {
		return "patrol target";
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

}
