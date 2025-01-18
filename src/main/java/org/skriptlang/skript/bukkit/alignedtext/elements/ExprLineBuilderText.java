package org.skriptlang.skript.bukkit.alignedtext.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.alignedtext.AlignedText;
import org.skriptlang.skript.bukkit.alignedtext.LineBuilder;
import org.skriptlang.skript.bukkit.alignedtext.elements.ExprSecLineBuilder.LineBuilderEvent;

public class ExprLineBuilderText extends PropertyExpression<LineBuilder, AlignedText> implements EventRestrictedSyntax {

	static {
		registerDefault(ExprLineBuilderText.class, AlignedText.class, "line builder text[s]", "linebuilder");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends LineBuilder>) exprs[0]);
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(LineBuilderEvent.class);
	}

	@Override
	protected AlignedText @Nullable [] get(Event event, LineBuilder[] source) {
		LineBuilder lineBuilder = getExpr().getSingle(event);
		assert lineBuilder != null;
		return lineBuilder.getAlignedTexts().toArray(new AlignedText[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD)
			return CollectionUtils.array(AlignedText[].class, String[].class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		LineBuilder lineBuilder = getExpr().getSingle(event);
		assert lineBuilder != null;
		if (mode == ChangeMode.SET)
			lineBuilder.clearAlignedTexts();
		for (Object object : delta) {
			if (object instanceof String string) {
				AlignedText alignedText = new AlignedText(string);
				lineBuilder.addAlignedText(alignedText);
			} else if (object instanceof AlignedText alignedText) {
				lineBuilder.addAlignedText(alignedText);
			}
		}
	}

	@Override
	public Class<? extends AlignedText> getReturnType() {
		return AlignedText.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

}
