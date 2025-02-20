package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.effects.EffChange;
import ch.njol.skript.expressions.arithmetic.ExprArithmetic;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExprSecEquation extends SectionExpression<Number> implements SyntaxRuntimeErrorProducer {

	public static class EquationEvent extends Event {

		private final String originalEquation;
		private String finalEquation;
		private boolean errorInSection = false;

		public EquationEvent(String originalEquation) {
			this.originalEquation = originalEquation;
			this.finalEquation = originalEquation;
		}

		public String getOriginalEquation() {
			return originalEquation;
		}

		public String getFinalEquation() {
			return finalEquation;
		}

		public void replaceVariables(String x, String replacing) {
			finalEquation = finalEquation.replace(x, replacing);
		}

		public void setErrorInSection() {
			this.errorInSection = true;
		}

		public boolean hasErrorInSection() {
			return errorInSection;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}


	}
	static {
		Skript.registerExpression(ExprSecEquation.class, Number.class, ExpressionType.SIMPLE,
			"equation %string%");
	}

	private Expression<String> string;
	private Trigger trigger;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean isDelayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node == null) {
			Skript.error("You put no section dumbass");
			return false;
		} else if (node.isEmpty()) {
			Skript.error("Theres nothing in the section dumb fuck.");
			return false;
		}
		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		//noinspection unchecked
		trigger = loadCode(node, "equation", afterLoading, EquationEvent.class);
		if (delayed.get()) {
			Skript.error("Delays can't be used within an Equation Section");
			return false;
		}
		//noinspection unchecked
		string = (Expression<String>) exprs[0];
		this.node = node;
		return true;
	}

	@Override
	protected Number @Nullable [] get(Event event) {
		String equation = this.string.getSingle(event);
		if (equation == null || equation.isEmpty()) {
			error("wtf is wrong with you");
			return null;
		}
		EquationEvent equationEvent = new EquationEvent(equation);
		Variables.withLocalVariables(event, equationEvent, () -> TriggerItem.walk(trigger, equationEvent));
		if (equationEvent.hasErrorInSection())
			return null;
		String finalEquation = equationEvent.getFinalEquation();
		if (finalEquation.matches("[a-zA-Z&$#@!<,>.?:;'\"{}_`~|\\\\\\[\\]]")) {
			error("STUPID");
			return null;
		}
		Effect effect = Effect.parse("set {_equation} to " + finalEquation, null);
		if (!(effect instanceof EffChange effChange))
			return null;
		Expression<?> changeExpr = effChange.getChanger();
		if (!(changeExpr instanceof ExprArithmetic<?,?,?> exprArithmetic))
			return null;
		return (Number[]) exprArithmetic.getAll(equationEvent);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}
}
