package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Raid;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Name("Raid Raiders")
@Description("The raider type entities of a raid.")
@Examples({
	"set {_raid} to nearest raid from location(0, 0, 0) in radius 5",
	"set {_raiders::*} to the raiders of {_raid}"
})
@Since("INSERT VERSION")
public class ExprRaidRaiders extends SimplePropertyExpression<Raid, Entity[]> {

	static {
		registerDefault(ExprRaidRaiders.class, Entity[].class, "raiders", "raids");
	}

	@Override
	public Entity @Nullable [] convert(Raid raid) {
		return raid.getRaiders().toArray(Entity[]::new);
	}

	@Override
	protected String getPropertyName() {
		return "raiders";
	}

	@Override
	public Class<Entity[]> getReturnType() {
		return Entity[].class;
	}

}
