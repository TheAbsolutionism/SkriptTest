package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Raid;
import org.jetbrains.annotations.Nullable;

@Name("Raid Health")
@Description("The total health of all the active raiders of a raid.")
@Examples({
	"loop the raids in world \"world\":",
		"\tbroadcast the raid health of loop-value"
})
@Since("INSERT VERSION")
public class ExprRaidTotalHealth extends SimplePropertyExpression<Raid, Float> {

	static {
		registerDefault(ExprRaidTotalHealth.class, Float.class, "raid health", "raids");
	}

	@Override
	public @Nullable Float convert(Raid raid) {
		return raid.getTotalHealth();
	}

	@Override
	protected String getPropertyName() {
		return "raid health";
	}

	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}

}
