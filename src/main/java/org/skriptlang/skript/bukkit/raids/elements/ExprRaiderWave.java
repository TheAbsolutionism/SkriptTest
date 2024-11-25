package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Raider Raid Wave")
@Description("The raid wave a raider type entity is a part of.")
@Examples({
	"set {_raid} to nearest raid from location(0, 0, 0) in radius 5",
	"loop the raiders of {_raid}:",
		"\tbroadcast the raid wave of loop-value"
})
@Since("INSERT VERSION")
public class ExprRaiderWave extends SimplePropertyExpression<LivingEntity, Integer> {

	static {
		register(ExprRaiderWave.class, Integer.class, "raid wave", "livingentities");
	}

	@Override
	public @Nullable Integer convert(LivingEntity entity) {
		if (!(entity instanceof Raider raider))
			return null;
		return raider.getWave();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Integer.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int wave = (int) delta[0];
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Raider raider))
				continue;
			raider.setWave(wave);
		}
	}

	@Override
	protected String getPropertyName() {
		return "raid wave";
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}
}
