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

@Name("Allow Raider Join Raid")
@Description("Allows a raider type entity to join a raid.")
@Examples({
	"spawn an illusioner at location(0, 0, 0)",
	"allow last spawned illusioner to join a raid"
})
@Since("INSERT VERSION")
public class EffRaiderJoinRaid extends Effect {

	static {
		Skript.registerEffect(EffRaiderJoinRaid.class,
			"allow %livingentities% to join [a] raid",
			"disallow %livingentities% to join [a] raid");
	}

	private Expression<LivingEntity> exprEntity;
	private boolean toJoin;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprEntity = (Expression<LivingEntity>) exprs[0];
		toJoin = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : exprEntity.getArray(event)) {
			if (!(entity instanceof Raider raider))
				continue;
			raider.setCanJoinRaid(toJoin);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (toJoin)
			builder.append("allow");
		else
			builder.append("disallow");
		builder.append(exprEntity, "to join a raid");
		return builder.toString();
	}

}
