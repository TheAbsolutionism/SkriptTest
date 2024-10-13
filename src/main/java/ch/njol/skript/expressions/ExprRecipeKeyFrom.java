package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.NamespacedUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Name("Recipe Key From String")
@Description({
	"Gets the end product key from a string",
	"To produce a valid key, the provided string has to be altered to meet the requirements."
})
@Examples({
	"send the recipe key from \"my_recipe\"",
	"loop all custom recipes:",
		"\tif recipe key of loop-recipe = recipe key from \"my_recipe\":",
			"\t\tbroadcast loop-recipe"
})
public class ExprRecipeKeyFrom extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprRecipeKeyFrom.class, String.class, ExpressionType.SIMPLE, "[the] recipe key[s] from %strings%");
	}

	private Expression<String> names;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		names = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		return Arrays.stream(names.getArray(event)).map(name -> NamespacedUtils.getNamespacedKey(name).toString()).toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return names.isSingle();
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "recipe keys from " + names.toString(event, debug);
	}
}
