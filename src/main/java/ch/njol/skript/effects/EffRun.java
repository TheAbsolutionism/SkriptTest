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
@Description("Executes a task (a function). Any returned result is discarded.")
@Examples({
		"set {_function} to the function named \"myFunction\"",
		"run {_function}",
		"run {_function} with arguments {_things::*}",
})
@Since("2.10")
@Keywords({"run", "execute", "reflection", "function"})
@SuppressWarnings({"rawtypes", "unchecked"})
public class EffRun extends Effect {

	static {
		Skript.registerEffect(EffRun.class,
				"(run|execute) %executable% [arguments:with arg[ument]s %-objects%] [after:after %-timespan%]",
				"(run|execute) %skriptrunnable% [after:after %-timespan%]",
				"after %timespan% (run|execute) %executable% [arguments:with arg[ument]s %-objects%]",
				"after %timespan% (run|execute) %skriptrunnable%");
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
		if (pattern >= 2) {
			timespan = (Expression<Timespan>) exprs[0];
			task = exprs[1];
		} else {
			task = exprs[0];
			if (result.hasTag("after"))
				timespan = (Expression<Timespan>) exprs[2];
		}
		hasArguments = result.hasTag("arguments");
		if (Executable.class.isAssignableFrom(task.getReturnType())) {
			if (hasArguments) {
				Expression<?> args = pattern == 0 ? exprs[1] : exprs[2];
				this.arguments = LiteralUtils.defendExpression(args);
				Expression<?>[] arguments;
				if (this.arguments instanceof ExpressionList<?>) {
					arguments = ((ExpressionList<?>) this.arguments).getExpressions();
				} else {
					arguments = new Expression[]{this.arguments};
				}
				this.input = new DynamicFunctionReference.Input(arguments);
				return LiteralUtils.canInitSafely(this.arguments);
			} else {
				this.input = new DynamicFunctionReference.Input();
			}
		} else if (SkriptRunnable.class.isAssignableFrom(task.getReturnType())) {
			if (hasArguments) {
				Skript.error("Cannot provide arguments when running a runnable. Can only be used for functions.");
				return false;
			}
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object object = task.getSingle(event);
		Runnable toRun = null;
		if (object == null) {
			return;
		} else if (object instanceof Executable executable) {
			Object[] arguments;
			if (task instanceof DynamicFunctionReference<?> reference) {
				Expression<?> validated = reference.validate(input);
				if (validated == null)
					return;
				arguments = validated.getArray(event);
			} else if (hasArguments) {
				arguments = this.arguments.getArray(event);
			} else {
				arguments = new Object[0];
			}
			toRun = () -> executable.execute(event, arguments);
		} else if (object instanceof SkriptRunnable skriptRunnable) {
			Object locals = Variables.copyLocalVariables(event);
			SkriptRunnableEvent runnableEvent = new SkriptRunnableEvent();
			toRun = () -> skriptRunnable.run(runnableEvent, locals);
		}
		if (toRun == null)
			return;
		long ticks = 0;
		if (timespan != null) {
			Timespan timespan = this.timespan.getSingle(event);
			if (timespan != null)
				ticks = timespan.getAs(TimePeriod.TICK);
		}
		if (ticks == 0) {
			toRun.run();
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), toRun, ticks);
		}
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
