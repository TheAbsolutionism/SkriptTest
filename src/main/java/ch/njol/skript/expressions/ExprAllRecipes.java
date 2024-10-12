package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.RecipeUtils;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Name("All Recipes")
@Description({
	"Retrieve all recipes registered in the server",
	"You can retrieve all recipes of a recipe type",
	"You can retrieve all recipes of an item, including custom items",
	"You can retrieve all minecraft recipes",
	"You can retrieve all custom recipes made by any and all plugins"
})
@Examples({
	"set {_list::*} to all of the recipe of type shaped recipe for netherite ingot",
	"set {_list::*} to all mc recipes of type cooking recipe for raw beef",
	"set {_list::*} to all of the custom recipes of type blasting for raw iron named \"Impure Iron\""
})
@Since("INSERT VERSION")
public class ExprAllRecipes extends SimpleExpression<Recipe> {

	static {
		Skript.registerExpression(ExprAllRecipes.class, Recipe.class, ExpressionType.SIMPLE,
			"all [of the] recipes [types:of type %recipetypes%] [items:for %itemstacks/itemtypes%]",
			"all [of the] (mc|minecraft|vanilla) recipes [types:of type %recipetypes%] [items:for %itemstacks/itemtypes%]",
			"all [of the] custom recipes [types:of type %recipetypes%] [items:for %itemstacks/itemtypes%]");
	}

	private Expression<RecipeUtils.RecipeType> recipeTypeExpr;
	private Expression<?> itemExpr;
	private boolean getMinecraft, getCustom;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		recipeTypeExpr = parseResult.hasTag("types") ? (Expression<RecipeUtils.RecipeType>) exprs[0] : null;
		itemExpr = parseResult.hasTag("items") ? exprs[1] : null;
		getMinecraft = matchedPattern == 1;
		getCustom = matchedPattern == 2;
		return true;
	}

	@Override
	protected Recipe @Nullable [] get(Event event) {

		List<Recipe> recipeList = new ArrayList<>();
		Iterator<Recipe> iterator = null;
		if (itemExpr != null) {
			List<Recipe> itemRecipes = new ArrayList<>();
			for (Object object : itemExpr.getArray(event)) {
				ItemStack stack = null;
				if (object instanceof ItemStack itemStack) {
					stack = itemStack;
				} else if (object instanceof ItemType itemType) {
					stack = new ItemStack(itemType.getMaterial());
					stack.setItemMeta(itemType.getItemMeta());
				}
				if (stack != null)
					itemRecipes.addAll(Bukkit.getRecipesFor(stack));
			}
			if (itemRecipes.isEmpty())
				return null;
			iterator = itemRecipes.iterator();
		} else {
			iterator = Bukkit.recipeIterator();
		}


		iterator.forEachRemaining(recipe -> {
			if (recipe instanceof Keyed keyed) {
				NamespacedKey key = keyed.getKey();
				if (getMinecraft && !key.getNamespace().equalsIgnoreCase("minecraft"))
					return;
				else if (getCustom && key.getNamespace().equalsIgnoreCase("minecraft"))
					return;

				if (recipeTypeExpr != null) {
					for (RecipeUtils.RecipeType type : recipeTypeExpr.getArray(event)) {
						if (recipe.getClass().equals(type.getRecipeClass()))
							recipeList.add(recipe);
					}
				} else {
					recipeList.add(recipe);
				}
			}
		});

		return recipeList.toArray(new Recipe[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Recipe> getReturnType() {
		return Recipe.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all of the " + (getMinecraft ? "minecraft " : getCustom ? "custom " : "") + "recipes" +
			(recipeTypeExpr != null ? " of type " + recipeTypeExpr.toString(event, debug) : "") +
			(itemExpr != null ? " for " + itemExpr.toString(event, debug) : "");
	}
}
