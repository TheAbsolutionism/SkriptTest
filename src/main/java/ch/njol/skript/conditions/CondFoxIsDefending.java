package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;

@Name("Fox Is Defending")
@Description("Checks whether a fox is defending its trusted players from wolves and polar bears.")
@Examples({
	"if the last spawned fox is not defending:",
		"\tmake the last spawned fox start defending"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class CondFoxIsDefending extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(Fox.class, "isDefending")) {
			register(CondFoxIsDefending.class, "defending", "livingentities");
		}
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Fox fox))
			return false;
		return fox.isDefending();
	}

	@Override
	protected String getPropertyName() {
		return "defending";
	}

}
