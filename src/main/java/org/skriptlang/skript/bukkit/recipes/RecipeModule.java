package org.skriptlang.skript.bukkit.recipes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.RecipeUtils;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

import java.io.IOException;

public class RecipeModule {

	public static void load() throws IOException {
		if (!Skript.classExists("org.bukkit.inventory.Recipe"))
			return;

		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit", "recipes");

		Classes.registerClass(new ClassInfo<>(Recipe.class, "recipe")
			.user("recipes?")
			.name("Recipe")
			.description("Represents a recipe type")
			.usage("recipes")
			.examples("all recipes")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(Recipe.class)));

		Classes.registerClass(new EnumClassInfo<>(CraftingBookCategory.class, "craftingbookcategory", "crafting book categories")
			.user("crafting book category")
			.name("Crafting Book Category")
			.description("Represents the different categories of crafting recipes")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(CookingBookCategory.class, "cookingbookcategory", "cooking book categories")
			.user("cooking book category")
			.name("Cooking Book Category")
			.description("Represents the different categories of cooking recipes")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(RecipeUtils.RecipeType.class, "recipetype", "recipe types")
			.user("recipe type")
			.name("Recipe Type")
			.description("Represents recipe types")
			.since("INSERT VERSION")
		);

		Comparators.registerComparator(RecipeUtils.RecipeType.class, RecipeUtils.RecipeType.class, new Comparator<RecipeUtils.RecipeType, RecipeUtils.RecipeType>() {
			@Override
			public Relation compare(RecipeUtils.RecipeType type1, RecipeUtils.RecipeType type2) {
				if (type1.getRecipeClass() != null && type2.getRecipeClass() != null)
					return Relation.get(type2.getRecipeClass().isAssignableFrom(type1.getRecipeClass()));
				return Relation.NOT_EQUAL;
			}
		});
	}

}
