package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class Test extends Effect {

	static {
		Skript.registerEffect(Test.class,
			"get plural of %string%",
			"get singular of %string%");
	}

	private Expression<String> expr;
	private boolean singular;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		expr = (Expression<String>) exprs[0];
		singular = matchedPattern == 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		String string = expr.getSingle(event);
		assert string != null;
		String output;
		if (singular) {
			output = Utils.toEnglishPlural(string);
		} else {
			NonNullPair<String, Boolean> test = Utils.getEnglishPlural(string);
			output = test.getKey().toLowerCase(Locale.ENGLISH);
		}
		Skript.adminBroadcast((singular ? "Singular" : "Plural") + ": " + output);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "null";
	}

}
