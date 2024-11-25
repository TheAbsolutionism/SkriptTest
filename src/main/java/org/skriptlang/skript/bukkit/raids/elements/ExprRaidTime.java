package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import org.bukkit.Raid;
import org.jetbrains.annotations.Nullable;

@Name("Raid Time")
@Description("The time a raid has been active for.")
@Examples({
	"loop all raids in world \"world\":",
		"\tbroadcast the active time of loop-value"
})
@Since("INSERT VERSION")
public class ExprRaidTime extends SimplePropertyExpression<Raid, Timespan> {

	static {
		registerDefault(ExprRaidTime.class, Timespan.class, "active time", "raids");
	}

	@Override
	public @Nullable Timespan convert(Raid raid) {
		return new Timespan(TimePeriod.TICK, raid.getActiveTicks());
	}

	@Override
	protected String getPropertyName() {
		return "active time";
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

}
