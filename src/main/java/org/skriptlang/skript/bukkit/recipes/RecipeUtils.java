package org.skriptlang.skript.bukkit.recipes;

import ch.njol.skript.Skript;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.CookingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.CookingRecipeEvent.BlastingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.CookingRecipeEvent.CampfireRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.CookingRecipeEvent.FurnaceRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.CookingRecipeEvent.SmokingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.CraftingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.CraftingRecipeEvent.ShapedRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.CraftingRecipeEvent.ShapelessRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.SmithingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.SmithingRecipeEvent.SmithingTransformRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.SmithingRecipeEvent.SmithingTrimRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.RegisterRecipeEvent.StonecuttingRecipeEvent;

/**
 * Utils used for getting data from {@link RecipeType}
 */
public class RecipeUtils {

	/**
	 * Enum for storing all types of Recipes (until Bukkit makes an enum or registry)
	 */
	public enum RecipeType {
		SHAPED(ShapedRecipe.class, ShapedRecipeEvent.class),
		SHAPELESS(ShapelessRecipe.class, ShapelessRecipeEvent.class),
		CRAFTING(getCraftingRecipeClass(), CraftingRecipeEvent.class),
		BLASTING(BlastingRecipe.class, BlastingRecipeEvent.class),
		FURNACE(FurnaceRecipe.class, FurnaceRecipeEvent.class),
		CAMPFIRE(CampfireRecipe.class, CampfireRecipeEvent.class),
		SMOKING(SmokingRecipe.class, SmokingRecipeEvent.class),
		COOKING(CookingRecipe.class, CookingRecipeEvent.class), // Having 'COOKING' under the subclasses allows for proper ExprRecipeType
		SMITHING_TRANSFORM(SmithingTransformRecipe.class, SmithingTransformRecipeEvent.class),
		SMITHING_TRIM(SmithingTrimRecipe.class, SmithingTrimRecipeEvent.class),
		SMITHING(SmithingRecipe.class, SmithingRecipeEvent.class), // Having 'SMITHING' under the subclasses allows for proper ExprRecipeType
		STONECUTTING(StonecuttingRecipe.class, StonecuttingRecipeEvent.class),
		COMPLEX(ComplexRecipe.class, null);

		private final @Nullable Class<? extends Recipe> recipeClass;
		private final @Nullable Class<? extends Event> eventClass;

		RecipeType(@Nullable Class<? extends Recipe> recipeClass, @Nullable Class<? extends Event> eventClass) {
			this.recipeClass = recipeClass;
			this.eventClass = eventClass;
		}

		/**
		 * Gets the Bukkit recipe type class.
		 * @return Bukkit recipe class
		 */
		public @Nullable Class<? extends Recipe> getRecipeClass() {
			return recipeClass;
		}

		/**
		 * Gets the custom event used when creating a new recipe.
		 * @return Custom event class
		 */
		public @Nullable Class<? extends Event> getEventClass() {
			return eventClass;
		}

		// Due to 1.19 not having 'CraftingRecipe.class'
		private static @Nullable Class<? extends Recipe> getCraftingRecipeClass() {
			if (Skript.classExists("org.bukkit.inventory.CraftingRecipe"))
				return CraftingRecipe.class;
			return null;
		}
	}

	/**
	 * Gets {@link RecipeType} from provided recipe class.
	 * @param providedClass Bukkit recipe class
	 * @return Recipe Type
	 */
	public static RecipeType getRecipeTypeFromRecipeClass(Class<? extends Recipe> providedClass) {
		for (RecipeType type : RecipeType.values()) {
			if (type.recipeClass != null && type.recipeClass.isAssignableFrom(providedClass)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Gets {@link RecipeType} from provided recipe.
	 * @param providedRecipe Recipe
	 * @return Recipe Type
	 */
	public static RecipeType getRecipeTypeFromRecipe(Recipe providedRecipe) {
		return getRecipeTypeFromRecipeClass(providedRecipe.getClass());
	}

}
