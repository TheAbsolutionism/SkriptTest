package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.World;

@Name("World Has Raids")
@Description("Checks if a world has any active raids.")
@Examples("if world \"world\" has raids:")
@Since("INSERT VERSION")
public class CondWorldHasRaids extends PropertyCondition<World> {

	static {
		register(CondWorldHasRaids.class, PropertyType.HAVE, "raids", "worlds");
	}

	@Override
	public boolean check(World world) {
		return world.hasRaids();
	}

	@Override
	protected String getPropertyName() {
		return "raids";
	}
}
