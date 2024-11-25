package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Raider Time Outside Raid")
@Description("The time a raider type entity has spent outside of a raid.")
@Examples({
	"set {_raid} to nearest raid from location(0, 0, 0) in radius 5",
	"loop the raiders of {_raid}:",
		"broadcast the time outside raid of loop-value"
})
@Since("INSERT VERSION")
public class ExprRaiderOutsideRaid extends SimplePropertyExpression<LivingEntity, Timespan> {

	static {
		register(ExprRaiderOutsideRaid.class, Timespan.class, "time outside raid", "livingentities");
	}

	@Override
	public @Nullable Timespan convert(LivingEntity entity) {
		if (!(entity instanceof Raider raider))
			return null;
		return new Timespan(TimePeriod.TICK, raider.getTicksOutsideRaid());
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Timespan.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Timespan timespan = (Timespan) delta[0];
		int time = (int) timespan.getAs(TimePeriod.TICK);
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Raider raider))
				continue;
			raider.setTicksOutsideRaid(time);
		}
	}

	@Override
	protected String getPropertyName() {
		return "time outside raid";
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

}
