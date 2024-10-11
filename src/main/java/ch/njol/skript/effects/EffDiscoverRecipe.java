package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Discover Recipe")
@Description("Discover or undiscover recipes for players.")
@Examples({
	"make player discover recipe \"my_recipe\"",
	"make player undiscover recipe \"my_recipe\"",
	"unlock recipe \"my_recipe\" for all players",
	"lock recipe \"my_recipe\" for all players"
})
@Since("INSERT VERSION")
public class EffDiscoverRecipe extends Effect {

	static {
		Skript.registerEffect(EffDiscoverRecipe.class,
			"make %players% (discover|unlock) recipe[s] %strings%",
			"make %players% (undiscover|lock) recipe[s] %strings%",
			"(discover|unlock) recipe[s] %strings% for %players%",
			"(undiscover|lock) recipe[s] %strings% for %players%");
	}

	private Expression<Player> players;
	private Expression<String> recipes;
	private boolean isDiscover = false;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isDiscover = matchedPattern == 0 || matchedPattern == 2;
		players = (Expression<Player>) (matchedPattern <= 1 ? exprs[0] : exprs[1]);
		recipes = (Expression<String>) (matchedPattern <= 1 ? exprs[1] : exprs[0]);
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Player player : players.getArray(event)) {
			for (String recipe : recipes.getArray(event)) {
				NamespacedKey key = NamespacedKey.fromString(recipe, Skript.getInstance());
				if (Bukkit.getRecipe(key) != null) {
					if (isDiscover)
						player.discoverRecipe(key);
					else
						player.undiscoverRecipe(key);
				} else {
					break;
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + players.toString(event, debug) + (isDiscover ? " discover" : " undiscover") + " recipes " + recipes.toString(event, debug);
	}
}
