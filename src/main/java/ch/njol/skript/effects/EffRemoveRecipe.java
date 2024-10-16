package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.NamespacedUtils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Remove Recipe")
@Description({
	"Remove a recipe or multiple recipes from the server",
	"Removing a recipe from a server will cause all players who have discovered the recipe to be undiscovered."
})
@Examples("remove the recipe \"my_recipe\" from the server")
@Since("INSERT VERSION")
public class EffRemoveRecipe extends Effect {

	static {
		Skript.registerEffect(EffRemoveRecipe.class,
			"(remove|delete|clear) [the] recipe[s] [with [the] key] %strings% [from the server]");
	}

	private Expression<String> recipes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		recipes = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (String recipe : recipes.getArray(event)) {
			NamespacedKey key = NamespacedUtils.getNamespacedKey(recipe, false);
			if (Bukkit.getRecipe(key) != null)
				Bukkit.removeRecipe(key);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "remove recipes " + recipes.toString(event, debug) + " from the server";
	}
}
