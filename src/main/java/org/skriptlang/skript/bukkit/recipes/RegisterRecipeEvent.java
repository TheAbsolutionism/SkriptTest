package org.skriptlang.skript.bukkit.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableBlastingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableCampfireRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableFurnaceRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableSmokingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe.MutableShapedRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe.MutableShapelessRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTransformRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTrimRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableStonecuttingRecipe;

public class RegisterRecipeEvent extends Event {
	private boolean errorInEffect = false;
	private RecipeType recipeType;
	private MutableRecipe recipeWrapper;

	public RegisterRecipeEvent(NamespacedKey key, RecipeType recipeType) {
		this.recipeType = recipeType;
		this.recipeWrapper = switch (recipeType) {
			case SHAPED -> new MutableShapedRecipe(key, recipeType);
			case SHAPELESS -> new MutableShapelessRecipe(key, recipeType);
			case BLASTING -> new MutableBlastingRecipe(key, recipeType);
			case FURNACE -> new MutableFurnaceRecipe(key, recipeType);
			case SMOKING -> new MutableSmokingRecipe(key, recipeType);
			case CAMPFIRE -> new MutableCampfireRecipe(key, recipeType);
			case SMITHING -> new MutableSmithingRecipe(key, recipeType);
			case SMITHING_TRANSFORM -> new MutableSmithingTransformRecipe(key, recipeType);
			case SMITHING_TRIM -> new MutableSmithingTrimRecipe(key, recipeType);
			case STONECUTTING -> new MutableStonecuttingRecipe(key, recipeType);
			default -> null;
		};
	}

	public void setErrorInEffect() {
		this.errorInEffect = true;
	}


	public boolean getErrorInEffect() {
		return errorInEffect;
	}

	public RecipeType getRecipeType() {
		return recipeType;
	}

	public MutableRecipe getRecipeWrapper() {
		return recipeWrapper;
	}

	public static class CraftingRecipeEvent extends RegisterRecipeEvent {

		public CraftingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		};

		public static class ShapedRecipeEvent extends CraftingRecipeEvent {
			public ShapedRecipeEvent(NamespacedKey key, RecipeType recipeType) {
				super(key, recipeType);
			};
		}

		public static class ShapelessRecipeEvent extends CraftingRecipeEvent {
			public ShapelessRecipeEvent(NamespacedKey key, RecipeType recipeType) {
				super(key, recipeType);
			};
		}
	}

	public static class CookingRecipeEvent extends RegisterRecipeEvent {

		public CookingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		};

		public static class BlastingRecipeEvent extends CookingRecipeEvent {
			public BlastingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
				super(key, recipeType);
			}
		}

		public static class CampfireRecipeEvent extends CookingRecipeEvent {
			public CampfireRecipeEvent(NamespacedKey key, RecipeType recipeType) {
				super(key, recipeType);
			}
		}

		public static class FurnaceRecipeEvent extends CookingRecipeEvent {
			public FurnaceRecipeEvent(NamespacedKey key, RecipeType recipeType) {
				super(key, recipeType);
			}
		}

		public static class SmokingRecipeEvent extends CookingRecipeEvent {
			public SmokingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
				super(key, recipeType);
			}
		}
	}

	public static class SmithingRecipeEvent extends RegisterRecipeEvent {

		public SmithingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		}

		public static class SmithingTransformRecipeEvent extends SmithingRecipeEvent {
			public SmithingTransformRecipeEvent(NamespacedKey key, RecipeType recipeType) {
				super(key, recipeType);
			}
		}

		public static class SmithingTrimRecipeEvent extends SmithingRecipeEvent {
			public SmithingTrimRecipeEvent(NamespacedKey key, RecipeType recipeType) {
				super(key, recipeType);
			}
		}
	}

	public static class StonecuttingRecipeEvent extends RegisterRecipeEvent {

		public StonecuttingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		}
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}
}
