package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Raid;
import org.jetbrains.annotations.Nullable;

@Name("Raid Total Waves")
@Description("The total number of waves a raid will spawn.")
@Examples({
	"loop the raids in world \"world\":",
		"\rbroadcast the total waves of loop-value"
})
@Since("INSERT VERSION")
public class ExprRaidTotalWaves extends SimplePropertyExpression<Raid, Integer> {

	static {
		registerDefault(ExprRaidTotalWaves.class, Integer.class, "total waves", "raids");
	}

	@Override
	public @Nullable Integer convert(Raid raid) {
		return raid.getTotalWaves();
	}

	@Override
	protected String getPropertyName() {
		return "total waves";
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}
}
