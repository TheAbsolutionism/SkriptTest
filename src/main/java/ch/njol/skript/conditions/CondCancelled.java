package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Event Cancelled")
@Description("Checks whether or not the event is cancelled.")
@Examples({"on click:",
		"\tif event is cancelled:",
		"\t\tbroadcast \"no clicks allowed!\""
})
@Since("2.2-dev36")
public class CondCancelled extends Condition {

	static {
		Skript.registerCondition(CondCancelled.class,
				"[the] event is cancel[l]ed",
				"[the] event (is not|isn't) cancel[l]ed"
		);
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return (e instanceof Cancellable && ((Cancellable) e).isCancelled()) ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return isNegated() ? "event is not cancelled" : "event is cancelled";
	}

}
