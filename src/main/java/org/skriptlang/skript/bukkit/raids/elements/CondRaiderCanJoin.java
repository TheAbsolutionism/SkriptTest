package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;

@Name("Can Raider Join Raid")
@Description("Checks if a raider type entity can join a raid.")
@Examples({
	"spawn a pillager at location(0, 0, 0)",
	"if last spawned pillager can join raid:",
		"\tallow last spawned pillager to join a raid"
})
@Since("INSERT VERSION")
public class CondRaiderCanJoin extends PropertyCondition<LivingEntity> {

	static {
		register(CondRaiderCanJoin.class, PropertyType.CAN, "join raid", "livingentites");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Raider raider))
			return isNegated();
		return raider.isCanJoinRaid();
	}

	@Override
	protected String getPropertyName() {
		return "join raid";
	}

}
