package org.skriptlang.skript.bukkit.recipes;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RecipeWrapper implements Recipe {

	private final RecipeUtils.RecipeType recipeType;
	private final NamespacedKey key;
	private ItemStack result = null;
	private List<String> errors = new ArrayList<>();

	public RecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
		this.key = key;
		this.recipeType = recipeType;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

	public ItemStack getResult() {
		return result;
	}

	public NamespacedKey getKey() {
		return this.key;
	}

	public RecipeUtils.RecipeType getRecipeType() {
		return recipeType;
	}

	public void addError(String error) {
		errors.add(error);
	}

	public List<String> getErrors() {
		return errors;
	}

	public Recipe create() {
		return null;
	}

    public static class CraftingRecipeWrapper extends RecipeWrapper {
		private RecipeChoice[] ingredients = new RecipeChoice[9];
		private String group;
		private CraftingBookCategory category;

		public CraftingRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
			super(key, recipeType);
		}

		public void setIngredients(int placement, RecipeChoice item) {
			ingredients[placement] = item;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public void setCategory(CraftingBookCategory category) {
			this.category = category;
		}

		public RecipeChoice[] getIngredients() {
			return ingredients;
		}

		public String getGroup() {
			return this.group;
		}

		public CraftingBookCategory getCategory() {
			return this.category;
		}

		public static class ShapedRecipeWrapper extends CraftingRecipeWrapper {

			public ShapedRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
				super(key, recipeType);
			}

			public ShapedRecipe create() {
				if (getResult() == null) {
					addError("You must provide a result item when registering a recipe");
					return null;
				}
				RecipeChoice[] ingredients = getIngredients();
				if (ingredients.length == 0 || Arrays.stream(ingredients).filter(Objects::nonNull).toArray().length < 2) {
					addError("You must have at least 1 ingredient when registering a '" + getRecipeType() + "' recipe.");
					return null;
				}
				ShapedRecipe shapedRecipe = new ShapedRecipe(getKey(), getResult());
				Character[] characters = new Character[]{'a','b','c','d','e','f','g','h','i'};
				shapedRecipe.shape("abc","def","ghi");
				for (int i = 0; i < ingredients.length; i++) {
					RecipeChoice thisChoice = ingredients[i];
					if (thisChoice != null)
						shapedRecipe.setIngredient(characters[i], thisChoice);
				}
				if (getGroup() != null)
					shapedRecipe.setGroup(getGroup());
				if (getCategory() != null)
					shapedRecipe.setCategory(getCategory());
				return shapedRecipe;
			}

		}

		public static class ShapelessRecipeWrapper extends CraftingRecipeWrapper {

			public ShapelessRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
				super(key, recipeType);
			}

			public ShapelessRecipe create() {
				if (getResult() == null) {
					addError("You must provide a result item when registering a recipe");
					return null;
				}
				RecipeChoice[] ingredients = getIngredients();
				if (ingredients.length == 0 || Arrays.stream(ingredients).filter(Objects::nonNull).toArray().length < 2) {
					addError("You must have at least 1 ingredient when registering a '" + getRecipeType() + "' recipe.");
					return null;
				}
				ShapelessRecipe shapelessRecipe = new ShapelessRecipe(getKey(), getResult());
                for (RecipeChoice thisChoice : ingredients) {
                    if (thisChoice != null)
                        shapelessRecipe.addIngredient(thisChoice);
                }
				if (getGroup() != null)
					shapelessRecipe.setGroup(getGroup());
				if (getCategory() != null)
					shapelessRecipe.setCategory(getCategory());
				return shapelessRecipe;
			}
		}
	}

	public static class CookingRecipeWrapper extends RecipeWrapper {
		private RecipeChoice input;
		private String group;
		private CookingBookCategory category;
		private int cookingTime = 10;
		private float experience = 0;

		public CookingRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
			super(key, recipeType);
		}

		public void setInput(RecipeChoice input) {
			this.input = input;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public void setCategory(CookingBookCategory category) {
			this.category = category;
		}

		public void setCookingTime(int cookingTime) {
			this.cookingTime = cookingTime;
		}

		public void setExperience(float experience) {
			this.experience = experience;
		}

		public RecipeChoice getInput() {
			return input;
		}

		public String getGroup() {
			return group;
		}

		public CookingBookCategory getCategory() {
			return category;
		}

		public int getCookingTime() {
			return cookingTime;
		}

		public float getExperience() {
			return experience;
		}

		public CookingRecipe<?> create() {
			if (getResult() == null) {
				addError("You must provide a result item when registering a recipe");
				return null;
			}
			var recipe = switch (getRecipeType()) {
				case BLASTING -> new BlastingRecipe(getKey(), getResult(), getInput(), getExperience(), getCookingTime());
				case FURNACE -> new FurnaceRecipe(getKey(), getResult(), getInput(), getExperience(), getCookingTime());
				case SMOKING -> new SmokingRecipe(getKey(), getResult(), getInput(), getExperience(), getCookingTime());
				case CAMPFIRE -> new CampfireRecipe(getKey(), getResult(), getInput(), getExperience(), getCookingTime());
				default -> throw new IllegalStateException("Unexpected value: " + getRecipeType());
			};
			if (getGroup() != null)
				recipe.setGroup(getGroup());
			if (getCategory() != null)
				recipe.setCategory(getCategory());
			return recipe;
		}

		public static class BlastingRecipeWrapper extends CookingRecipeWrapper {
			public BlastingRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
				super(key, recipeType);
			}

			public BlastingRecipe create() {
				return (BlastingRecipe) super.create();
			}
		}

		public static class FurnaceRecipeWrapper extends CookingRecipeWrapper {
			public FurnaceRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
				super(key, recipeType);
			}

			public FurnaceRecipe create() {
				return (FurnaceRecipe) super.create();
			}
		}

		public static class SmokingRecipeWrapper extends CookingRecipeWrapper {
			public SmokingRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
				super(key, recipeType);
			}

			public SmokingRecipe create() {
				return (SmokingRecipe) super.create();
			}
		}

		public static class CampfireRecipeWrapper extends CookingRecipeWrapper {
			public CampfireRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
				super(key, recipeType);
			}

			public CampfireRecipe create() {
				return (CampfireRecipe) super.create();
			}
		}

	}

	public static class SmithingRecipeWrapper extends RecipeWrapper {
		private RecipeChoice base;
		private RecipeChoice template;
		private RecipeChoice addition;

		public SmithingRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
			super(key, recipeType);
		}

		public void setBase(RecipeChoice base) {
			this.base = base;
		}

		public void setTemplate(RecipeChoice template)  {
			this.template = template;
		}

		public void setAddition(RecipeChoice addition) {
			this.addition = addition;
		}

		public RecipeChoice getBase() {
			return base;
		}

		public RecipeChoice getTemplate() {
			return template;
		}

		public RecipeChoice getAddition() {
			return addition;
		}

		public SmithingRecipe create() {
			if (getResult() == null) {
				addError("You must provide a result item when registering a recipe");
				return null;
			}
			RecipeChoice base = getBase(), addition = getAddition();
			if (base == null || addition == null) {
				addError("Unable to create 'smithing transform' recipe, missing data.");
				return null;
			}
			return new SmithingRecipe(getKey(), getResult(), getBase(), getAddition());
		}

		public static class SmithingTransformRecipeWrapper extends SmithingRecipeWrapper {
			public SmithingTransformRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
				super(key, recipeType);
			}

			public SmithingTransformRecipe create() {
				if (getResult() == null) {
					addError("You must provide a result item when registering a recipe");
					return null;
				}
				RecipeChoice base = getBase(), template = getTemplate(), addition = getAddition();
				if (base == null || template == null || addition == null) {
					addError("Unable to create 'smithing transform' recipe, missing data.");
					return null;
				}
				return new SmithingTransformRecipe(getKey(), getResult(), getTemplate(), getBase(), getAddition());
			}
		}

		public static class SmithingTrimRecipeWrapper extends SmithingRecipeWrapper {
			public SmithingTrimRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
				super(key, recipeType);
			}

			public SmithingTrimRecipe create() {
				RecipeChoice base = getBase(), template = getTemplate(), addition = getAddition();
				if (base == null || template == null || addition == null) {
					addError("Unable to create 'smithing trim' recipe, missing data.");
					return null;
				}
				return new SmithingTrimRecipe(getKey(), getTemplate(), getBase(), getAddition());
			}
		}
	}

	public static class StonecuttingRecipeWrapper extends RecipeWrapper {
		private RecipeChoice input;

		public StonecuttingRecipeWrapper(NamespacedKey key, RecipeUtils.RecipeType recipeType) {
			super(key, recipeType);
		}

		public void setInput(RecipeChoice input) {
			this.input = input;
		}

		public RecipeChoice getInput() {
			return input;
		}

		public StonecuttingRecipe create() {
			if (getResult() == null) {
				addError("You must provide a result item when registering a recipe");
				return null;
			}
			return new StonecuttingRecipe(getKey(), getResult(), input);
		}
	}

}
