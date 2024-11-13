package org.skriptlang.skript.bukkit.recipes;

import ch.njol.skript.Skript;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.*;
import org.skriptlang.skript.bukkit.recipes.RecipeWrapper.*;
import org.skriptlang.skript.bukkit.recipes.RecipeWrapper.CraftingRecipeWrapper.*;
import org.skriptlang.skript.bukkit.recipes.RecipeWrapper.CookingRecipeWrapper.*;
import org.skriptlang.skript.bukkit.recipes.RecipeWrapper.SmithingRecipeWrapper.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utils used for getting data from {@link RecipeType}
 */
public class RecipeUtils {

	public enum RecipeType {
		SHAPED(ShapedRecipe.class, RegisterRecipeEvent.CraftingRecipeEvent.ShapedRecipeEvent.class),
		SHAPELESS(ShapelessRecipe.class, RegisterRecipeEvent.CraftingRecipeEvent.ShapelessRecipeEvent.class),
		CRAFTING(getCraftingRecipeClass(), RegisterRecipeEvent.CraftingRecipeEvent.class),
		BLASTING(BlastingRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.BlastingRecipeEvent.class),
		FURNACE(FurnaceRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.FurnaceRecipeEvent.class),
		CAMPFIRE(CampfireRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.CampfireRecipeEvent.class),
		SMOKING(SmokingRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.SmokingRecipeEvent.class),
		COOKING(CookingRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.class), // Having 'COOKING' under the subclasses allows for proper ExprRecipeType
		SMITHING_TRANSFORM(SmithingTransformRecipe.class, RegisterRecipeEvent.SmithingRecipeEvent.SmithingTransformRecipeEvent.class),
		SMITHING_TRIM(SmithingTrimRecipe.class, RegisterRecipeEvent.SmithingRecipeEvent.SmithingTrimRecipeEvent.class),
		SMITHING(SmithingRecipe.class, RegisterRecipeEvent.SmithingRecipeEvent.class), // Having 'SMITHING' under the subclasses allows for proper ExprRecipeType
		STONECUTTING(StonecuttingRecipe.class, RegisterRecipeEvent.StonecuttingRecipeEvent.class),
		COMPLEX(ComplexRecipe.class, null);

		private final @Nullable Class<? extends Recipe> recipeClass;
		private final @Nullable Class<? extends Event> eventClass;

		RecipeType(@Nullable Class<? extends Recipe> recipeClass, @Nullable Class<? extends Event> eventClass) {
			this.recipeClass = recipeClass;
			this.eventClass = eventClass;
		}

		public @Nullable Class<? extends Recipe> getRecipeClass() {
			return recipeClass;
		}

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

	public static RecipeType getRecipeTypeFromRecipeClass(Class<? extends Recipe> providedClass) {
		for (RecipeType type : RecipeType.values()) {
			if (type.recipeClass != null && type.recipeClass.isAssignableFrom(providedClass)) {
				return type;
			}
		}
		return null;
	}

	public static RecipeType getRecipeTypeFromRecipe(Recipe providedRecipe) {
		return getRecipeTypeFromRecipeClass(providedRecipe.getClass());
	}

	// Custom Events used for SecRegisterRecipe
	public static class RegisterRecipeEvent extends Event {
		private boolean errorInEffect = false;
		private RecipeType recipeType;
		private RecipeWrapper recipeWrapper;

		public RegisterRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			this.recipeType = recipeType;
			this.recipeWrapper = switch (recipeType) {
				case SHAPED -> new ShapedRecipeWrapper(key, recipeType);
				case SHAPELESS -> new ShapelessRecipeWrapper(key, recipeType);
				case BLASTING -> new BlastingRecipeWrapper(key, recipeType);
				case FURNACE -> new FurnaceRecipeWrapper(key, recipeType);
				case SMOKING -> new SmokingRecipeWrapper(key, recipeType);
				case CAMPFIRE -> new CampfireRecipeWrapper(key, recipeType);
				case SMITHING -> new SmithingRecipeWrapper(key, recipeType);
				case SMITHING_TRANSFORM -> new SmithingTransformRecipeWrapper(key, recipeType);
				case SMITHING_TRIM -> new SmithingTrimRecipeWrapper(key, recipeType);
				case STONECUTTING -> new StonecuttingRecipeWrapper(key, recipeType);
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

		public RecipeWrapper getRecipeWrapper() {
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

}
