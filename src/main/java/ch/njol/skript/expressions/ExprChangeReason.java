package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason;
import org.eclipse.jdt.annotation.Nullable;

@Name("Experience Change Reason")
@Description({
	"The <a href='classes.html#experiencechangereason'>experience change reason</a> within in an" +
	"<a href='events.html#experience cooldown change event'>experience cooldown change event</a>."
})
@Examples({
	"on player experience cooldown change:",
		"\tchange reason is plugin",
		"\tchange reason is orb pickup"
})
@Since("INSERT VERSION")
public class ExprChangeReason extends EventValueExpression<ChangeReason> {

	static {
		register(ExprChangeReason.class, ChangeReason.class, "[the] [experience|exp|xp] change (reason|cause|type)");
	}

	public ExprChangeReason() {
		super(ChangeReason.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "change reason";
	}

}
