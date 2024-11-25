package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;

@Name("Is Raider Raid Leader")
@Description("Checks if a raider type entity is a raid leader.")
@Examples({
	"spawn an evoker at location(0, 0, 0)",
	"if last spawned evoker is raid leader:",
		"\tmake last spawned evoke not raid leader"
})
@Since("INSERT VERSION")
public class CondRaiderLeader extends PropertyCondition<LivingEntity> {

	static {
		register(CondRaiderLeader.class, PropertyType.BE, "raid leader", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Raider raider))
			return isNegated();
		return raider.isPatrolLeader();
	}

	@Override
	protected String getPropertyName() {
		return "raid leader";
	}

}
