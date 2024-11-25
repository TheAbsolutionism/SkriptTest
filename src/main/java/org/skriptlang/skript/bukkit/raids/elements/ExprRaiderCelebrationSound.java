package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.jetbrains.annotations.Nullable;

@Name("Raider Celebration Sound")
@Description("The celebration sound of a raider type entity.")
@Examples({
	"spawn a ravager at location(0, 0, 0)",
	"broadcast the celebration sound of last spawned ravager"
})
@Since("INSERT VERSION")
public class ExprRaiderCelebrationSound extends SimplePropertyExpression<LivingEntity, Sound> {

	static {
		register(ExprRaiderCelebrationSound.class, Sound.class, "celebration sound", "livingentities");
	}

	@Override
	public @Nullable Sound convert(LivingEntity entity) {
		if (!(entity instanceof Raider raider))
			return null;
		return raider.getCelebrationSound();
	}

	@Override
	protected String getPropertyName() {
		return "celebration sound";
	}

	@Override
	public Class<Sound> getReturnType() {
		return Sound.class;
	}
}
