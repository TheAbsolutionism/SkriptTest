package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Is Sleeping")
@Description("Checks whether an entity is sleeping.")
@Examples({
	"# cut your enemies' throats in their sleep >=)",
	"on attack:",
		"\tattacker is holding a sword",
		"\tvictim is sleeping",
		"\tincrease the damage by 1000"
})
@Since("1.4.4, INSERT VERSION (livingentities)")
public class CondIsSleeping extends PropertyCondition<LivingEntity> {
	
	static {
		register(CondIsSleeping.class, "sleeping", "livingentities");
	}
	
	@Override
	public boolean check(LivingEntity entity) {
		return entity.isSleeping();
	}
	
	@Override
	protected String getPropertyName() {
		return "sleeping";
	}
	
}
