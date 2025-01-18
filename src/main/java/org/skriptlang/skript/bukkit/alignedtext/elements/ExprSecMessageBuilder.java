package org.skriptlang.skript.bukkit.alignedtext.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.alignedtext.MessageBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExprSecMessageBuilder extends SectionExpression<MessageBuilder> {

	public static class MessageBuilderEvent extends Event {

		private final MessageBuilder messageBuilder;

		public MessageBuilderEvent(MessageBuilder messageBuilder) {
			this.messageBuilder = messageBuilder;
		}

		public MessageBuilder getMessageBuilder() {
			return messageBuilder;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerExpression(ExprSecMessageBuilder.class, MessageBuilder.class, ExpressionType.SIMPLE, "a new message builder [with:with a max pixel length of %-integer%]");
	}

	private Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int pattern, Kleenean isDelayed, ParseResult result, @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
		if (node == null) {
			return false;
		}
		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		//noinspection unchecked
		trigger = loadCode(node, "message builder", afterLoading, MessageBuilderEvent.class);
		if (delayed.get()) {
			Skript.error("Delays cannot be used within a 'message builder' section.");
			return false;
		}
		return true;
	}

	@Override
	protected MessageBuilder @Nullable [] get(Event event) {
		MessageBuilder messageBuilder = new MessageBuilder();
		MessageBuilderEvent messageBuilderEvent = new MessageBuilderEvent(messageBuilder);
		Variables.withLocalVariables(event, messageBuilderEvent, () -> TriggerItem.walk(trigger, messageBuilderEvent));
		return new MessageBuilder[]{messageBuilderEvent.getMessageBuilder()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<MessageBuilder> getReturnType() {
		return MessageBuilder.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

}
