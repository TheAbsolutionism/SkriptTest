package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;

@Name("Fox Is Leaping")
@Description("Checks whether a fox is leaping.")
@Examples({
	"if the last spawned fox is not leaping:",
		"\tmake the last spawned fox leap"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class CondFoxIsLeaping extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(Fox.class, "isLeaping")) {
			register(CondIsInterested.class, "leaping", "livingentities");
		}
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Fox fox))
			return false;
		return fox.isLeaping();
	}

	@Override
	protected String getPropertyName() {
		return "leaping";
	}

}
