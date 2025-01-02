package ch.njol.skript.conditions;

import org.bukkit.entity.Camel;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

@Name("Is Sprinting")
@Description("Checks whether a player or camel is sprinting.")
@Examples("player is not sprinting")
@Since("1.4.4, INSERT VERSION (camels)")
public class CondIsSprinting extends PropertyCondition<LivingEntity> {
	
	static {
		register(CondIsSprinting.class, "sprinting", "livingentities");
	}
	
	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Player player) {
			return player.isSprinting();
		} else if (entity instanceof Camel camel) {
			return camel.isDashing();
		}
		return false;
	}
	
	@Override
	protected String getPropertyName() {
		return "sprinting";
	}
	
}
