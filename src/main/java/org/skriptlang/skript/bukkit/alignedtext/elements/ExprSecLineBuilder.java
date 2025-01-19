package org.skriptlang.skript.bukkit.alignedtext.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.alignedtext.LineBuilder;
import org.skriptlang.skript.bukkit.alignedtext.MessageBuilder;
import org.skriptlang.skript.bukkit.alignedtext.elements.ExprSecMessageBuilder.MessageBuilderEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExprSecLineBuilder extends SectionExpression<LineBuilder> implements EventRestrictedSyntax {

	public static class LineBuilderEvent extends Event {

		private final LineBuilder lineBuilder;

		public LineBuilderEvent(LineBuilder lineBuilder) {
			this.lineBuilder = lineBuilder;
		}

		public LineBuilder getLineBuilder() {
			return lineBuilder;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerExpression(ExprSecLineBuilder.class, LineBuilder.class, ExpressionType.SIMPLE, "a new line builder");
		EventValues.registerEventValue(LineBuilderEvent.class, LineBuilder.class, LineBuilderEvent::getLineBuilder);
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node == null) {
			return false;
		}
		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		//noinspection unchecked
		trigger = loadCode(node, "line builder", afterLoading, LineBuilderEvent.class);
		if (delayed.get()) {
			Skript.error("Delays cannot be used within a 'line builder' section.");
			return false;
		}
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(MessageBuilderEvent.class);
	}

	@Override
	protected LineBuilder @Nullable [] get(Event event) {
		if (!(event instanceof MessageBuilderEvent messageBuilderEvent))
			return null;
		MessageBuilder messageBuilder = messageBuilderEvent.getMessageBuilder();
		LineBuilder lineBuilder = new LineBuilder();
		LineBuilderEvent lineBuilderEvent = new LineBuilderEvent(lineBuilder);
		Variables.withLocalVariables(event, lineBuilderEvent, () -> TriggerItem.walk(trigger, lineBuilderEvent));
		return new LineBuilder[]{lineBuilderEvent.getLineBuilder()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends LineBuilder> getReturnType() {
		return LineBuilder.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

}
