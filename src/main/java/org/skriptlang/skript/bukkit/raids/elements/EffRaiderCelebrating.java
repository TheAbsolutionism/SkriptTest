package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Raider Celebrate")
@Description("Makes a raider type entity start or stop celebrating")
@Examples({
	"spawn an illager at location(0, 0, 0)",
	"make last spawned illager celebrate"
})
@Since("INSERT VERSION")
public class EffRaiderCelebrating extends Effect {

	static {
		Skript.registerEffect(EffRaiderCelebrating.class,
			"make %livingentities% (celebrate|start celebrating)",
			"maake %livingentities% stop celebrating");
	}

	private Expression<LivingEntity> exprEntity;
	private boolean toCelebrate;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprEntity = (Expression<LivingEntity>) exprs[0];
		toCelebrate = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : exprEntity.getArray(event)) {
			if (!(entity instanceof Raider raider))
				continue;
			raider.setCelebrating(toCelebrate);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", exprEntity);
		if (toCelebrate)
			builder.append("celebrate");
		else
			builder.append("stop celebrating");
		return builder.toString();
	}

}
