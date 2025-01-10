package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.sections.ExprSecRunnable.SkriptRunnable;
import ch.njol.skript.sections.ExprSecRunnable.SkriptRunnableEvent;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Executable;

@Name("Run (Experimental)")
@Description({
	"Executes a function (any result from the function is discarded) or a runnable.",
	"Executing a runnable will allow any local variables from the current code to be usable within the runnable. "
		+ "Including any modifications to be present outside the runnable.",
	"Executing a runnable with a delay (e.g. after 1 second) will behave as the following:",
	" - Any local variables defined before the runnable is executed will be usable, but any changes within will not take affect outside the runnable.",
	" - Any local variables defined within the runnable will not be usable outside of it.",
	" - Any local variables defined after the runnable is executed will not be accessible within the runnable."
})
@Examples({
	"set {_function} to the function named \"myFunction\"",
	"run {_function}",
	"run {_function} with arguments {_things::*}",
	"",
	"execute a new runnable:",
		"\tbroadcast \"Hi!\"",
	"set {_runnable} to a new runnable:",
		"\tbroadcast \"Bye!\"",
	"execute {_runnable}",
	"run {_runnable} after 2 seconds",
	"after 1 second execute {_runnable}"
})
@Since("2.10, INSERT VERSION (runnables)")
@Keywords({"run", "execute", "reflection", "function"})
@SuppressWarnings({"rawtypes", "unchecked"})
public class EffRun extends Effect {

	static {
		Skript.registerEffect(EffRun.class,
				"(run|execute) %executable/skriptrunnable% [arguments:with arg[ument]s %-objects%] [after:after %-timespan%]",
				"after %timespan% (run|execute) %executable/skriptrunnable% [arguments:with arg[ument]s %-objects%]");
	}

	// We don't bother with the generic type here because we have no way to verify it
	// from the expression, and it makes casting more difficult to no benefit.
	private Expression<?> task;
	private @Nullable Expression<?> arguments;
	private DynamicFunctionReference.Input input;
	private boolean hasArguments;
	private @Nullable Expression<Timespan> timespan;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean isDelayed, ParseResult result) {
		if (pattern == 0) {
			task = exprs[0];
			if (result.hasTag("after"))
				timespan = (Expression<Timespan>) exprs[2];
		} else {
			timespan = (Expression<Timespan>) exprs[0];
			task = exprs[1];
		}
		hasArguments = result.hasTag("arguments");
		if (hasArguments) {
			Expression<?> args = pattern == 0 ? exprs[1] : exprs[2];
			this.arguments = LiteralUtils.defendExpression(args);
			Expression<?>[] arguments;
			if (this.arguments instanceof ExpressionList<?> expressionList) {
				arguments = expressionList.getExpressions();
			} else {
				arguments = new Expression[]{this.arguments};
			}
			this.input = new DynamicFunctionReference.Input(arguments);
			return LiteralUtils.canInitSafely(this.arguments);
		} else {
			this.input = new DynamicFunctionReference.Input();
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object object = task.getSingle(event);
		Runnable toRun = null;
		long ticks = 0;
		if (timespan != null) {
			Timespan timespan = this.timespan.getSingle(event);
			if (timespan != null)
				ticks = timespan.getAs(TimePeriod.TICK);
		}
		if (object == null) {
			return;
		} else if (object instanceof SkriptRunnable skriptRunnable) {
			if (ticks == 0) {
				skriptRunnable.run(event, null);
				return;
			}
			Object locals = Variables.copyLocalVariables(event);
			SkriptRunnableEvent runnableEvent = new SkriptRunnableEvent();
			toRun = () -> skriptRunnable.run(runnableEvent, locals);
		} else if (object instanceof Executable executable) {
			Object[] arguments;
			if (executable instanceof DynamicFunctionReference<?> reference) {
				assert input != null;
				Expression<?> validated = reference.validate(input);
				if (validated == null)
					return;
				arguments = validated.getArray(event);
			} else if (this.arguments != null) {
				arguments = this.arguments.getArray(event);
			} else {
				arguments = new Object[0];
			}
			if (ticks == 0) {
				executable.execute(event, arguments);
				return;
			}
			toRun = () -> executable.execute(event, arguments);
		}
		if (toRun == null)
			return;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), toRun, ticks);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("run", task);
		if (arguments != null)
			builder.append("with arguments", arguments);
		if (timespan != null)
			builder.append("after", timespan);
		return builder.toString();
	}

}
