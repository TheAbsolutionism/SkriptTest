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

@Name("Make Raider Raid Leader")
@Description("Makes a raider type entity a raid leader.")
@Examples({
	"spawn a spellcaster at location(0, 0, 0)",
	"make last spawned spellcaster a raid leader"
})
@Since("INSERT VERSION")
public class EffRaiderLeader extends Effect {

	static {
		Skript.registerEffect(EffRaiderLeader.class,
			"make %livingentities% [a] raider leader",
			"make %livingentities% not [a] raider leader");
	}

	private Expression<LivingEntity> exprEntity;
	private boolean beLeader;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprEntity = (Expression<LivingEntity>) exprs[0];
		beLeader = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : exprEntity.getArray(event)) {
			if (!(entity instanceof Raider raider))
				continue;
			raider.setPatrolLeader(beLeader);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder =  new SyntaxStringBuilder(event, debug);
		builder.append("make", exprEntity);
		if (!beLeader)
			builder.append("not");
		builder.append("a raider leader");
		return builder.toString();
	}

}
