package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Raid;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Raider's Raid")
@Description("The raid a raider type entity is a part of.")
@Examples({
	"spawn an evoker at location(0, 0, 0)",
	"set the raid of last spawned evoker to the nearest raid from location(0, 0, 0) in radius 5"
})
@Since("INSERT VERSION")
public class ExprRaiderRaid extends SimplePropertyExpression<LivingEntity, Raid> {

	static {
		register(ExprRaiderRaid.class, Raid.class, "raid", "livingentities");
	}

	@Override
	public @Nullable Raid convert(LivingEntity entity) {
		if (!(entity instanceof Raider raider))
			return null;
		return raider.getRaid();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Raid.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Raid raid = delta != null ? (Raid) delta[0] : null;
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Raider raider))
				continue;
			raider.setRaid(raid);
		}
	}

	@Override
	protected String getPropertyName() {
		return "raid";
	}

	@Override
	public Class<Raid> getReturnType() {
		return Raid.class;
	}

}
