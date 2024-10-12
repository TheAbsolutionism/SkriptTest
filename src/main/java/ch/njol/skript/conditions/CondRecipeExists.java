package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Recipe Exists")
@Description("Checks to see if a recipe exists using the name")
@Examples({
	"if the recipe \"my_recipe\" exists:",
		"\tremove the recipe \"my_recipe\" from the server"
})
@Since("INSERT VERSION")
public class CondRecipeExists extends Condition {

	static {
		Skript.registerCondition(CondRecipeExists.class, "[the] recipe[s] %strings% exist[s]");
	}

	private Expression<String> recipes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		recipes = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		return recipes.check(event, recipe -> Bukkit.getRecipe(NamespacedKey.fromString(recipe, Skript.getInstance())) != null);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "recipes " + recipes.toString(event, debug) + "exists";
	}
}
