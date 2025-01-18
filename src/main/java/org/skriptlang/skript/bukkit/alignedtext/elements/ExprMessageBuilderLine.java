package org.skriptlang.skript.bukkit.alignedtext.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.alignedtext.LineBuilder;
import org.skriptlang.skript.bukkit.alignedtext.MessageBuilder;
import org.skriptlang.skript.bukkit.alignedtext.elements.ExprSecMessageBuilder.MessageBuilderEvent;

public class ExprMessageBuilderLine extends SimpleExpression<LineBuilder> implements EventRestrictedSyntax {

	static {
		Skript.registerExpression(ExprMessageBuilderLine.class, LineBuilder.class, ExpressionType.COMBINED,
			"[the] message [builder] line %integer% [of %messagebuilder%]");
	}

	private Expression<Integer> line;
	private Expression<MessageBuilder> builder;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		line = (Expression<Integer>) exprs[0];
		//noinspection unchecked
		builder = (Expression<MessageBuilder>) exprs[1];
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(MessageBuilderEvent.class);
	}

	@Override
	protected LineBuilder @Nullable [] get(Event event) {
        MessageBuilder messageBuilder = builder.getSingle(event);
		Integer line = this.line.getSingle(event);
		assert messageBuilder != null;
		assert line != null;
		return new LineBuilder[]{messageBuilder.getLine(line)};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(LineBuilder.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		MessageBuilder messageBuilder = builder.getSingle(event);
		Integer line = this.line.getSingle(event);
		assert messageBuilder != null;
		assert delta != null;
		assert line != null;
		LineBuilder lineBuilder = (LineBuilder) delta[0];
		lineBuilder.setLine(line);
		messageBuilder.setLine(line, lineBuilder);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<LineBuilder> getReturnType() {
		return LineBuilder.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

}
