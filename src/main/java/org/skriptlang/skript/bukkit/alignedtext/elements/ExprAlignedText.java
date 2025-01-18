package org.skriptlang.skript.bukkit.alignedtext.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.alignedtext.AlignedText;
import org.skriptlang.skript.bukkit.alignedtext.AlignedText.Alignment;

import java.util.ArrayList;
import java.util.List;

public class ExprAlignedText extends SimpleExpression<AlignedText> {

	static {
		Skript.registerExpression(ExprAlignedText.class, AlignedText.class, ExpressionType.COMBINED,
			"[left] aligned text of %strings% [with:with %integer% (indents|indentations|spaces)]",
			"center aligned text of %strings% [with:with %integer% (indents|indentations|spaces)]",
			"right aligned text of %strings% [with:with %integer% (indents|indentations|spaces)]");
	}

	private Expression<String> strings;
	private Alignment alignment = Alignment.LEFT;
	private @Nullable Expression<Integer> indents;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		strings = (Expression<String>) exprs[0];
		if (matchedPattern == 1) {
			alignment = Alignment.CENTER;
		} else if (matchedPattern == 2) {
			alignment = Alignment.RIGHT;
		}
		if (parseResult.hasTag("with"))
			//noinspection unchecked
			indents = (Expression<Integer>) exprs[1];
		return true;
	}

	@Override
	protected AlignedText @Nullable [] get(Event event) {
		List<AlignedText> alignedTexts = new ArrayList<>();
		int indentation = 0;
		if (indents != null)
			indentation = indents.getSingle(event);
		for (String string : strings.getArray(event)) {
			alignedTexts.add(new AlignedText(string, alignment, indentation));
		}
		return alignedTexts.toArray(new AlignedText[0]);
	}

	@Override
	public boolean isSingle() {
		return strings.isSingle();
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
