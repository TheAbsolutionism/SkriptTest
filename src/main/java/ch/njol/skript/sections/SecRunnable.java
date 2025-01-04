package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Runnable")
@Description({
	"This section will wait the amount of time from the provided timespan to run the code within the section.",
	"This will not halt the code after this section.",
	"Any local variables defined before hand will be usable within the section, but will not replace local variables within the code outside this section.",
	"Any local variables defined within this section will not be preset outside of this section.",
	"Any local variables defined outside and after this section will not be accessible in this section."
})
@Examples({
	"set {_a} to 1",
	"after 2 seconds run:",
		"\tadd 1 to {_a}",
		"\tset {_b} to 3",
		"\tif {_c} is set:",
			"\t\t# This will fail because this variable was not defined before or within this section.",
	"set {_c} to 4",
	"if {_a} = 2:",
		"\t# This will fail because the code within the section does not change the local variables outside of it.",
	"if {_b} is set:",
		"\t# This will fail because local variables defined within the section is not accessible outside of it."
})
@Since("INSERT VERSION")
public class SecRunnable extends Section implements SyntaxRuntimeErrorProducer {

	public static class RunnableEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(SecRunnable.class, "after %timespan% run");
	}

	private Trigger trigger;
	private Expression<Timespan> timespan;
	private Node node;
	private String rawExpr;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		if (sectionNode.isEmpty()) {
			Skript.error("This section cannot be empty.");
			return false;
		}
		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		trigger = loadCode(sectionNode, "runnable", afterLoading, RunnableEvent.class);
		//noinspection unchecked
		timespan = (Expression<Timespan>) exprs[0];
		node = getParser().getNode();
		rawExpr = parseResult.expr;
		if (timespan instanceof Literal<Timespan> literal && literal.getSingle().getAs(TimePeriod.TICK) == 0) {
			Skript.warning("The provided timespan is equal to 0 seconds. Consider running the code directly rather than within this section.");
		}
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		Object locals = Variables.copyLocalVariables(event);
		Timespan timespan = this.timespan.getSingle(event);
		if (timespan == null) {
			error("The provided timespan cannot be null.");
		} else {
			long ticks = timespan.getAs(TimePeriod.TICK);
			if (ticks == 0) {
				warning("The provided timespan is equal to 0 seconds. Consider running the code directly rather than within this section.");
				execute(locals);
			} else {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
					execute(locals);
				}, ticks);
			}
		}
		return getActualNext();
	}

	private void execute(Object locals) {
		RunnableEvent runnableEvent = new RunnableEvent();
		Variables.setLocalVariables(runnableEvent, locals);
		TriggerItem.walk(trigger, runnableEvent);
		Variables.removeLocals(runnableEvent);
	}

	@Override
	public @Nullable String toHighlight() {
		return rawExpr;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "after " + timespan.toString(event, debug) + " run";
	}

}
