package org.skriptlang.skript.bukkit.alignedtext.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.alignedtext.elements.ExprSecLineBuilder.LineBuilderEvent;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

public class EffLineFiller extends Effect implements EventRestrictedSyntax, SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffLineFiller.class,
			"(set|make) [the] [line] filler char[acter] to %string%");
	}

	private Expression<String> string;
	private Node node;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
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
		if (string == null || string.isEmpty()) {
			error("The filler character cannot be null nor empty.");
			return;
		}
		String uncolored = ChatMessages.stripStyles(string);
		if (uncolored.length() > 1) {
			error("The filler character can only be one character.");
			return;
		}
		lineBuilderEvent.getLineBuilder().setFillerCharacter(string);
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
