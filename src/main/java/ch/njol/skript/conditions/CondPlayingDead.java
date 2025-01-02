package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.LivingEntity;

@Name("Axolotl Is Playing Dead")
@Description("Checks to see if an axolotl is playing dead.")
@Examples({
	"if last spawned axolotl is playing dead:",
		"\tmake last spawned axolotl stop playing dead"
})
@Since("INSERT VERSION")
public class CondPlayingDead extends PropertyCondition<LivingEntity> {

	static {
		PropertyCondition.register(CondPlayingDead.class, PropertyType.BE, "playing dead", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (!(entity instanceof Axolotl axolotl))
			return false;
		return axolotl.isPlayingDead();
	}

	@Override
	protected String getPropertyName() {
		return "playing dead";
	}

}
