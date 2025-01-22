package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprNamedTest extends SimpleExpression<NamedTest> {

	static {
		Skript.registerExpression(ExprNamedTest.class, NamedTest.class, ExpressionType.SIMPLE, "a new named test");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected NamedTest @Nullable [] get(Event event) {
		return new NamedTest[]{new NamedTest()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends NamedTest> getReturnType() {
		return NamedTest.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new named test";
	}

}
