package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent;
import ch.njol.skript.util.RecipeUtils.RegisterRecipeEvent.*;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

@Name("Recipe Category")
@Description("The recipe category of a Shaped, Shapeless, Cooking, Blasting, Furnace, Campfire and Smoking Recipe.")
@Examples({
	"register a new shaped recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients to diamond, air, diamond, air, emerald, air, diamond, air and diamond",
		"\tset the recipe crafting category to crafting misc",
		"\tset the recipe result item to nether star",
	"",
	"register a new blasting recipe with the id \"my_recipe\":",
		"\tset the recipe input item to coal",
		"\tset the recipe cooking category to cooking misc",
		"\tset the recipe result item to gunpowder",
	"",
	"loop all recipes:",
		"\tif recipe type of loop-recipe is shaped recipe:",
			"\t\tbroadcast recipe crafting category of loop-recipe",
		"\telse if recipe type of loop-recipe is cooking recipe:",
			"\t\tbroadcast recipe cooking category of loop-recipe"
})
public class ExprRecipeCategory extends PropertyExpression<Recipe, String> {

	static {
		Skript.registerExpression(ExprRecipeCategory.class, String.class, ExpressionType.PROPERTY,
			"[the] [recipe] crafting category [of %-recipes%]",
			"[the] [recipe] cooking category [of %-recipes%]");
	}

	private boolean isCrafting = false;
	private boolean isEvent = false;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isCrafting = matchedPattern == 0;
		if (exprs[0] != null) {
			setExpr((Expression<? extends Recipe>) exprs[0]);
		} else {
			if (!getParser().isCurrentEvent(RegisterRecipeEvent.class)) {
				Skript.error("There is no 'recipe' in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			if (isCrafting && !getParser().isCurrentEvent(CraftingRecipeEvent.class)) {
				Skript.error("This can only be used when registering a Shaped or Shapeless Recipe.");
				return false;
			} else if (!isCrafting && !getParser().isCurrentEvent(CookingRecipeEvent.class)) {
				Skript.error("This can only be used when registering a Cooking, Blasting, Furnace, Campfire or Smoking Recipe.");
				return false;
			}
			isEvent = true;
			setExpr(new EventValueExpression<>(Recipe.class));
		}
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event, Recipe[] source) {
		if (isEvent)
			return null;

		return get(source, recipe -> {
			if (isCrafting && recipe instanceof CraftingRecipe craftingRecipe) {
				return craftingRecipe.getCategory().name();
			} else if (!isCrafting && recipe instanceof CookingRecipe<?> cookingRecipe) {
				return cookingRecipe.getCategory().name();
			}
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && isEvent) {
			if (isCrafting)
				return CollectionUtils.array(CraftingBookCategory.class);
			return CollectionUtils.array(CookingBookCategory.class);
		}

		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (isCrafting && event instanceof RegisterRecipeEvent.CraftingRecipeEvent craftingEvent) {
			if (!(delta[0] instanceof CraftingBookCategory category))
				return;
			craftingEvent.setCategory(category);
		} else if (!isCrafting && event instanceof RegisterRecipeEvent.CookingRecipeEvent cookingEvent) {
			if (!(delta[0] instanceof CookingBookCategory category))
				return;
			cookingEvent.setCategory(category);
		}
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe " + (isCrafting ? "crafting" : "cooking") + " category of " + getExpr().toString(event, debug);
	}
}
