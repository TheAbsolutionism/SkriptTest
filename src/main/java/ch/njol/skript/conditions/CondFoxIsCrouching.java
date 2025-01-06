package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;

@Name("Fox Is Crouching")
@Description("Checks whether a fox is crouching.")
@Examples({
	"if last spawned fox is not crouching:",
		"\tmake last spawned fox crouch"
})
@Since("INSERT VERSION")
public class CondFoxIsCrouching extends PropertyCondition<LivingEntity> {

	static {
		register(CondFoxIsCrouching.class, "crouching", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Fox fox))
			return false;
		return fox.isCrouching();
	}

	@Override
	protected String getPropertyName() {
		return "crouching";
	}

}
