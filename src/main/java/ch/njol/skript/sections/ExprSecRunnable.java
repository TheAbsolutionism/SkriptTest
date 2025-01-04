package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.NoDoc;
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

@NoDoc
public class ExprSecRunnable extends SectionExpression<Object> {

	public static class SkriptRunnable {

		private final ExprSecRunnable exprRunnable;

		public SkriptRunnable(ExprSecRunnable exprRunnable) {
			this.exprRunnable = exprRunnable;
		}

		public void run(Event event, @Nullable Object locals) {
			exprRunnable.runSection(event, locals);
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
	public Class<?> getReturnType() {
		return SkriptRunnable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new runnable";
	}

}
