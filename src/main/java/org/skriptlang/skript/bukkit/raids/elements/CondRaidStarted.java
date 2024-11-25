package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.Raid;

@Name("Has Raid Started")
@Description("Checks if a raid has started.")
@Examples({
	"loop raids of world \"world\":",
		"\tif loop-raid has started:",
			"\tbroadcast the raid status of loop-raid"
})
@Since("INSERT VERSION")
public class CondRaidStarted extends PropertyCondition<Raid> {

	static {
		register(CondRaidStarted.class, PropertyType.HAVE, "started", "raids");
	}

	@Override
	public boolean check(Raid raid) {
		return raid.isStarted();
	}

	@Override
	protected String getPropertyName() {
		return "started";
	}

}
