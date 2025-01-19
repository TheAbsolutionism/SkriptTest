package org.skriptlang.skript.bukkit.alignedtext.elements;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.alignedtext.PixelUtils;

public class ExprPixelLength extends SimplePropertyExpression<String, Integer> {

	static {
		register(ExprPixelLength.class, Integer.class, "pixel length[s]", "strings");
	}

	@Override
	public @Nullable Integer convert(String string) {
		return PixelUtils.getLength(string);
	}

	@Override
	protected String getPropertyName() {
		return "pixel length";
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

}
