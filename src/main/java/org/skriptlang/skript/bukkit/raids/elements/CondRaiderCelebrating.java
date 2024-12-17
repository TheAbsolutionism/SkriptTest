package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;

@Name("Is Raider Celebrating")
@Description("Checks if a raider type entity is celebrating.")
@Examples({
	"spawn a vindicator at location(0, 0, 0)",
	"if last spawned vindicator is celebrating:",
		"\tmake last spawned vindicator stop celebrating"
})
@Since("INSERT VERSION")
public class CondRaiderCelebrating extends PropertyCondition<LivingEntity> {

	static {
		register(CondRaiderCelebrating.class, PropertyType.BE, "celebrating", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Raider raider))
			return isNegated();
		return raider.isCelebrating();
	}

	@Override
	protected String getPropertyName() {
		return "celebrating";
	}

}
