package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;

@Name("Fox Is Faceplanted")
@Description("Checks whether a fox has their face planted in the ground.")
@Examples("if last spawned fox is faceplanted:")
@Since("INSERT VERSION")
public class CondFoxIsFaceplanted extends PropertyCondition<LivingEntity> {

	static {
		register(CondFoxIsFaceplanted.class, "faceplanted",  "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Fox fox))
			return false;
		return fox.isFaceplanted();
	}

	@Override
	protected String getPropertyName() {
		return "faceplanted";
	}

}
