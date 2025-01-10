package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Runnable")
@Description({
	"Create a runnable where the code within the section can be executed anywhere using 'EffRun'.",
	"Executing a runnable after a delay will not halt the code that is defined afterwards.",
	"Using a delay will make the runnable behave as the following:",
	" - Any local variables defined before the runnable is executed will be usable, but any changes within will not take affect outside the runnable.",
	" - Any local variables defined within the runnable will not be usable outside of it.",
	" - Any local variables defined after the runnable is executed will not be accessible within the runnable."
})
@Examples({
	"# Without a delay",
	"set {_a} to 1",
	"execute a new runnable:",
		"\tadd 1 to {_a}",
		"\tset {_b} to 3",
	"if {_a} is 2:",
		"\t# This will pass",
	"if {_b} is 3:",
		"\t# This will pass",
	"",
	"# With a delay",
	"set {_a} to 1",
	"after 2 seconds execute a new runnable:",
		"\tadd 1 to {_a}",
		"\tset {_b} to 3",
	"if {_a} is 2:",
		"\t# This will fail",
	"if {_b} is set:",
		"\t# This will fail",
	"",
	"set {_runnable} a new runnable:",
		"\tbroadcast \"Welcome!\"",
	"run {_runnable}",
	"run {_runnable} after 2 seconds",
	"after 1 second run {_runnable}"
})
@Since("INSERT VERSION")
public class ExprSecRunnable extends SectionExpression<Object> {

	public static class SkriptRunnable {

		private final ExprSecRunnable exprRunnable;

		public SkriptRunnable(ExprSecRunnable exprRunnable) {
			this.exprRunnable = exprRunnable;
		}

		public void run(Event event, @Nullable Object locals) {
			exprRunnable.runSection(event, locals);
		}

		@Override
		public String toString() {
			return "skript runnable";
		}
	}

	public static class SkriptRunnableEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerExpression(ExprSecRunnable.class, Object.class, ExpressionType.SIMPLE, "[a] new runnable");
		Classes.registerClass(new ClassInfo<>(SkriptRunnable.class, "skriptrunnable")
			.user("skript ?runnables?")
			.name("Skript Runnable")
			.description("Represents a runnable created using 'ExprSecRunnable'.")
			.since("INSERT VERSION")
		);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node == null) {
			Skript.error("This expression requires a section.");
			return false;
		} else if (node.isEmpty()) {
			Skript.error("You cannot make a new runnable with an empty section.");
			return false;
		}
		loadCode(node);
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		SkriptRunnable skriptRunnable = new SkriptRunnable(this);
		return new SkriptRunnable[] {skriptRunnable};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<SkriptRunnable> getReturnType() {
		return SkriptRunnable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new runnable";
	}

}
