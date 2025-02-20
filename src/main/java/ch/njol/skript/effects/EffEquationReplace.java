package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.ExprSecEquation.EquationEvent;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

public class EffEquationReplace extends Effect implements EventRestrictedSyntax, SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffEquationReplace.class,
			"replace [the] equation variable %string% with %string/number%");
	}

	private Expression<String> string;
	private Expression<?> replacing;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		//noinspection unchecked
		string = (Expression<String>) exprs[0];
		replacing = exprs[1];
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EquationEvent.class);
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof EquationEvent equationEvent))
			return;
		String string = this.string.getSingle(event);
		if (string == null || string.isEmpty()) {
			eventError("AHHHHHHHHHHHHH", equationEvent);
			return;
		}
		Object object = replacing.getSingle(event);
		String replacing;
		if (object == null) {
			eventError("AHHHH", equationEvent);
			return;
		} else if (object instanceof String stringReplace) {
			if (stringReplace.isEmpty()) {
				eventError("AHHHHHHHHHHHHHHHHHHH", equationEvent);
				return;
			}
			replacing = stringReplace;
		} else if (object instanceof Number number) {
			replacing = number.toString();
		}  else {
			eventError("AHHHHH", equationEvent);
			return;
		}
		if (!equationEvent.getFinalEquation().contains(string)) {
			eventError("AAHHHHH", equationEvent);
			return;
		}
		equationEvent.replaceVariables(string, replacing);
	}

	private void eventError(String errorMessage, EquationEvent equationEvent) {
		equationEvent.setErrorInSection();
		error(errorMessage);
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
