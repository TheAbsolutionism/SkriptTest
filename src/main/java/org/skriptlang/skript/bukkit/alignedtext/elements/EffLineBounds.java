package org.skriptlang.skript.bukkit.alignedtext.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.alignedtext.LineBuilder;
import org.skriptlang.skript.bukkit.alignedtext.elements.ExprSecLineBuilder.LineBuilderEvent;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

public class EffLineBounds extends Effect implements EventRestrictedSyntax, SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffLineBounds.class,
			"(set|make) [the] [line] left bound character to %string%",
			"(set|make) [the] [line] right bound character to %string%");
	}

	private Expression<String> string;
	private boolean isLeft;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isLeft = matchedPattern == 0;
		//noinspection unchecked
		string = (Expression<String>) exprs[0];
		node = getParser().getNode();
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(LineBuilderEvent.class);
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof LineBuilderEvent lineBuilderEvent))
			return;
		String string = this.string.getSingle(event);
		if (string == null || string.length() > 1) {
			error("The line bound character can only be 1 character.");
			return;
		}
		LineBuilder lineBuilder = lineBuilderEvent.getLineBuilder();
		Character character = string.charAt(0);
		if (isLeft) {
			lineBuilder.setLeftBound(character);
		} else {
			lineBuilder.setRightBound(character);
		}
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
