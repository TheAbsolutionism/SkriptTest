package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.jetbrains.annotations.Nullable;

@Name("Raid Location")
@Description("The location of a raid")
@Examples({
	"loop the raids of world \"world\":",
		"\tbroadcast the location of loop-value"
})
@Since("INSERT VERSION")
public class ExprRaidLocation extends SimplePropertyExpression<Raid, Location> {

	static {
		registerDefault(ExprRaidLocation.class, Location.class, "location", "raids");
	}

	@Override
	public @Nullable Location convert(Raid raid) {
		return raid.getLocation();
	}

	@Override
	protected String getPropertyName() {
		return "location";
	}

	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}

}
