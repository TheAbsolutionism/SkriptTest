package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Raid;
import org.jetbrains.annotations.Nullable;

@Name("Raid Spawned Groups")
@Description("The number of groups that have already spawned in a raid.")
@Examples({
	"on raid wave spawn:",
		"broadcast the spawned groups of event-raid"
})
@Since("INSERT VERSION")
public class ExprRaidSpawnedGroups extends SimplePropertyExpression<Raid, Integer> {

	static {
		registerDefault(ExprRaidSpawnedGroups.class, Integer.class, "spawned groups", "raids");
	}

	@Override
	public @Nullable Integer convert(Raid raid) {
		return raid.getSpawnedGroups();
	}

	@Override
	protected String getPropertyName() {
		return "spawned groups";
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}
}
