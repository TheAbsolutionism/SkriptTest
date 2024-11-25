package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Raid;
import org.bukkit.Raid.RaidStatus;
import org.jetbrains.annotations.Nullable;

@Name("Raid Status")
@Description("The status of a raid.")
@Examples({
	"loop all raids of world \"world\":",
		"\tbroadcast the raid status of loop-value"
})
@Since("INSERT VERSION")
public class ExprRaidStatus extends SimplePropertyExpression<Raid, RaidStatus> {

	static {
		registerDefault(ExprRaidStatus.class, RaidStatus.class, "raid status", "raids");
	}

	@Override
	public @Nullable RaidStatus convert(Raid raid) {
		return raid.getStatus();
	}

	@Override
	protected String getPropertyName() {
		return "raid status";
	}

	@Override
	public Class<RaidStatus> getReturnType() {
		return RaidStatus.class;
	}
}
