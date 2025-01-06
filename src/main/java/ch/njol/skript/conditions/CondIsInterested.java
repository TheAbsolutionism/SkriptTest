package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

@Name("Is Interested")
@Description({
	"Checks if a wolf or fox is interested.",
	"Wolves become interested naturally when a player nearby is holding a food item that they like such as steak. "
		+ "This can be indicated by the slight head rotation of the wolf.",
	"Foxes can become interested for a multitude of reasons. One being of the fox about to attack a mob it likes such as a chicken.",
	"Foxes can only be checked on Paper."
})
@Examples({
	"if the last spawned fox is interested:",
		"\tmake the last spawned fox not interested"
})
@RequiredPlugins("Paper (foxes)")
@Since("INSERT VERSION")
public class CondIsInterested extends PropertyCondition<LivingEntity> {

	private static final boolean SUPPORTS_FOX = Skript.methodExists(Fox.class,  "isInterested", boolean.class);

	static {
		register(CondIsInterested.class, "interested", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (SUPPORTS_FOX && entity instanceof Fox fox) {
			return fox.isInterested();
		} else if (entity instanceof Wolf wolf) {
			return wolf.isInterested();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "interested";
	}

}
