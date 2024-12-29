package org.skriptlang.skript.bukkit.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;

/**
 * Event class used with ExprSecCreateRecipe to allow the creation of MutableRecipe
 */
public class CreateRecipeEvent extends Event {
	private boolean errorInSection = false;
	private final RecipeType recipeType;
	private final MutableRecipe mutableRecipe;

	public CreateRecipeEvent(NamespacedKey key, RecipeType recipeType) {
		this.recipeType = recipeType;
		this.mutableRecipe = recipeType.createMutableRecipe(key);
	}

	public void setErrorInSection() {
		this.errorInSection = true;
	}

	public boolean getErrorInSection() {
		return errorInSection;
	}

	public RecipeType getRecipeType() {
		return recipeType;
	}

	public MutableRecipe getMutableRecipe() {
		return mutableRecipe;
	}

	/**
	 * Specific event to determine if the mutableRecipe is of MutableCraftingRecipe
	 */
	public static class CraftingRecipeEvent extends CreateRecipeEvent {

		public CraftingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		};

		/**
		 * Specific event to determine if the mutableRecipe is of MutableShapedRecipe
		 */
		public static class ShapedRecipeEvent extends CraftingRecipeEvent {
			public ShapedRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SHAPED);
			};
		}

		/**
		 * Specific event to determine if the mutableRecipe is of MutableShapelessRecipe
		 */
		public static class ShapelessRecipeEvent extends CraftingRecipeEvent {
			public ShapelessRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SHAPELESS);
			};
		}
	}

	/**
	 * Specific event to determine if the mutableRecipe is of MutableCookingRecipe
	 */
	public static class CookingRecipeEvent extends CreateRecipeEvent {

		public CookingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		};

		public static class BlastingRecipeEvent extends CookingRecipeEvent {
			public BlastingRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.BLASTING);
			}
		}

		public static class CampfireRecipeEvent extends CookingRecipeEvent {
			public CampfireRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.CAMPFIRE);
			}
		}

		public static class FurnaceRecipeEvent extends CookingRecipeEvent {
			public FurnaceRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.FURNACE);
			}
		}

		public static class SmokingRecipeEvent extends CookingRecipeEvent {
			public SmokingRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SMOKING);
			}
		}
	}

	public static class SmithingRecipeEvent extends CreateRecipeEvent {
		public SmithingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		}

		public static class SmithingTransformRecipeEvent extends SmithingRecipeEvent {
			public SmithingTransformRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SMITHING_TRANSFORM);
			}
		}

		public static class SmithingTrimRecipeEvent extends SmithingRecipeEvent {
			public SmithingTrimRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SMITHING_TRIM);
			}
		}
	}

	public static class StonecuttingRecipeEvent extends CreateRecipeEvent {
		public StonecuttingRecipeEvent(NamespacedKey key) {
			super(key, RecipeType.STONECUTTING);
		}
	}

	public static class TransmuteRecipeEvent extends CreateRecipeEvent {
		public TransmuteRecipeEvent(NamespacedKey key) {
			super(key, RecipeType.TRANSMUTE);
		}
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}
}
