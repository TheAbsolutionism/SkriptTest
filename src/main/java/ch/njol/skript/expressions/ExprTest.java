package ch.njol.skript.expressions;

import ch.njol.skript.classes.data.TestClassInfo;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;

public class ExprTest extends SimplePropertyExpression<TestClassInfo, String> {

	static {
		register(ExprTest.class, String.class,  "testing", "anvilgui");
	}

	@Override
	public @Nullable String convert(TestClassInfo from) {
		return "blah";
	}

	@Override
	protected String getPropertyName() {
		return "testing";
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
}
