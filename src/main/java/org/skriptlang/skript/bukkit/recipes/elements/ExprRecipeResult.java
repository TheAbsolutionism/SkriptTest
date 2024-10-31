package org.skriptlang.skript.bukkit.recipes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RegisterRecipeEvent;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.RecipeWrapper;

@Name("Recipe Result")
@Description("The result item for a recipe.")
@Examples({
	"register a new shaped recipe with the key \"my_recipe\":",
		"\tset the recipe ingredients of 1st row to diamond, air and diamond",
		"\tset the recipe result to diamond sword named \"Chosen One\""
})
@Since("INSERT VERSION")
public class ExprRecipeResult extends PropertyExpression<Recipe, ItemStack> {

	static {
		Skript.registerExpression(ExprRecipeResult.class, ItemStack.class, ExpressionType.PROPERTY,
			"[the] recipe result[ing] [item] [of %recipes%]");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!exprs[0].isDefault()) {
			//noinspection unchecked
			setExpr((Expression<? extends Recipe>) exprs[0]);
		} else {
			if (exprs[0] == null) {
				Skript.error("There is no recipe in a '" + getParser().getCurrentEventName() + "' event.");
				return false;
			}
			if (getParser().isCurrentEvent(RegisterRecipeEvent.class))
				isEvent = true;
			setExpr(new EventValueExpression<>(Recipe.class));
		}
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event, Recipe[] source) {
		return get(source, recipe -> {
			if (recipe instanceof RecipeWrapper recipeWrapper)
				return recipeWrapper.getResult();
			return recipe.getResult();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && isEvent)
			return CollectionUtils.array(ItemStack.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof RegisterRecipeEvent recipeEvent))
			return;

		RecipeWrapper recipeWrapper = recipeEvent.getRecipeWrapper();

		ItemStack result = (ItemStack) delta[0];
		recipeWrapper.setResult(result);
	}

	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the recipe result item of " + getExpr().toString(event, debug);
	}

}
