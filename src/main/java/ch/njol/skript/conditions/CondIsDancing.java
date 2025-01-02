package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;

@Name("Is Dancing")
@Description("Checks to see if an entity is dancing. (e.g allays and parrots)")
@Examples({
	"if last spawned allay is dancing:",
		"\tbroadcast \"Dance Party!\""
})
@Since("INSERT VERSION")
public class CondIsDancing extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsDancing.class, "dancing", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Allay allay) {
			return allay.isDancing();
		} else if (entity instanceof Parrot parrot) {
			return parrot.isDancing();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "dancing";
	}

}
