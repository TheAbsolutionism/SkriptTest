package ch.njol.skript.util;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;

public class RegisterRecipeEvent extends Event {

	public enum RecipeTypes {
		SHAPED("shaped [crafting]", 9, 3, "shaped", CraftingRecipeEvent.ShapedRecipeEvent.class),
		SHAPELESS("shapeless [crafting]", 9, 0, "shapeless", CraftingRecipeEvent.ShapelessRecipeEvent.class),
		BLASTING("blast[ing]", "blasting", CookingRecipeEvent.BlastingRecipeEvent.class),
		CAMPFIRE("campfire", "campfire", CookingRecipeEvent.CampfireRecipeEvent.class),
		FURNACE("furnace", "furnace", CookingRecipeEvent.FurnaceRecipeEvent.class),
		SMOKING("smoking", "smoking", CookingRecipeEvent.SmokingRecipeEvent.class),
		COOKING("cooking", "cooking", CookingRecipeEvent.class),
		SMITHINGTRANSFORM("smith[ing] transform", "smithing transform", SmithingRecipeEvent.SmithingTransformRecipeEvent.class),
		SMITHINGTRIM("smith[ing] trim", "smithing trim", SmithingRecipeEvent.SmithingTrimRecipeEvent.class),
		STONECUTTING("stone cutting", "stone cutting", StonecuttingRecipeEvent.class);


		private String pattern, toString;
		private int maxIngredients, maxRowIngredients;
		private Class<? extends Event> eventClass;

		RecipeTypes(String pattern, int maxIngredients, int maxRowIngredients, String toString, Class<? extends Event> eventClass) {
			this.pattern = pattern;
			this.maxIngredients = maxIngredients;
			this.maxRowIngredients = maxRowIngredients;
			this.toString = toString;
			this.eventClass = eventClass;
		}

		RecipeTypes(String pattern, String toString, Class<? extends Event> eventClass) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClass = eventClass;
		}

		public String getPattern() {
			return pattern;
		}

		public Class<? extends Event> getEventClass() {
			return eventClass;
		}

		public String getToString() {
			return toString;
		}

		public int getMaxIngredients() {
			return maxIngredients;
		}

		public int getMaxRowIngredients() {
			return maxRowIngredients;
		}

	}

	private ItemStack resultItem;
	private boolean errorInEffect = false;
	private RecipeTypes recipeType;

	public RegisterRecipeEvent(RecipeTypes recipeType) {
		this.recipeType = recipeType;
	}

	public void setResultItem(ItemStack resultItem) {
		this.resultItem = resultItem;
	}

	public void setErrorInEffect() {
		this.errorInEffect = true;
	}

	public ItemStack getResultItem() {
		return resultItem;
	}

	public boolean getErrorInEffect() {
		return errorInEffect;
	}

	public RecipeTypes getRecipeType() {
		return recipeType;
	}

	public String getRecipeName() {
		return recipeType.getToString();
	}

	public static class CraftingRecipeEvent extends RegisterRecipeEvent {
		private ItemStack[] ingredients = new ItemStack[9];
		private CraftingBookCategory category = CraftingBookCategory.MISC;
		private String group;

		public CraftingRecipeEvent(RecipeTypes recipeType) {
			super(recipeType);
		};

		public void setIngredients(int placement, ItemStack item) {
			ingredients[placement] = item;
		}

		public void setCategory(CraftingBookCategory category) {
			this.category = category;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public ItemStack[] getIngredients() {
			return ingredients;
		}

		public int getMaxIngredients() {
			return getRecipeType().getMaxIngredients();
		}

		public int getMaxRowIngredients() {
			return getRecipeType().getMaxRowIngredients();
		}

		public CraftingBookCategory getCategory() {
			return category;
		}

		public String getGroup() {
			return group;
		}

		public static class ShapedRecipeEvent extends CraftingRecipeEvent {
			public ShapedRecipeEvent(RecipeTypes recipeType) {
				super(recipeType);
			};
		}

		public static class ShapelessRecipeEvent extends CraftingRecipeEvent {
			public ShapelessRecipeEvent(RecipeTypes recipeType) {
				super(recipeType);
			};
		}
	}

	public static class CookingRecipeEvent extends RegisterRecipeEvent {
		private Material inputItem;
		private CookingBookCategory category = CookingBookCategory.MISC;
		private String group;
		private int cookingTime = 10;
		private float experience = 0;

		public CookingRecipeEvent(RecipeTypes recipeType) {
			super(recipeType);
		};

		public void setInputItem(Material item) {
			inputItem = item;
		}

		public void setCategory(CookingBookCategory category) {
			this.category = category;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public void setCookingTime(int cookingTime) {
			this.cookingTime = cookingTime;
		}

		public void setExperience(float experience) {
			this.experience = experience;
		}

		public CookingBookCategory getCategory() {
			return category;
		}

		public String getGroup() {
			return group;
		}

		public Material getInputItem() {
			return inputItem;
		}

		public int getCookingTime() {
			return cookingTime;
		}

		public float getExperience() {
			return experience;
		}

		public static class BlastingRecipeEvent extends CookingRecipeEvent {
			public BlastingRecipeEvent(RecipeTypes recipeType) {
				super(recipeType);
			}
		}

		public static class CampfireRecipeEvent extends CookingRecipeEvent {
			public CampfireRecipeEvent(RecipeTypes recipeType) {
				super(recipeType);
			}
		}

		public static class FurnaceRecipeEvent extends CookingRecipeEvent {
			public FurnaceRecipeEvent(RecipeTypes recipeType) {
				super(recipeType);
			}
		}

		public static class SmokingRecipeEvent extends CookingRecipeEvent {
			public SmokingRecipeEvent(RecipeTypes recipeType) {
				super(recipeType);
			}
		}
	}

	public static class SmithingRecipeEvent extends RegisterRecipeEvent {

		private RecipeChoice base, addition, template;

		public SmithingRecipeEvent(RecipeTypes recipeType) {
			super(recipeType);
		}

		public void setBase(RecipeChoice base) {
			this.base = base;
		}

		public void setAddition(RecipeChoice addition) {
			this.addition = addition;
		}

		public void setTemplate(RecipeChoice template) {
			this.template = template;
		}

		public RecipeChoice getBase() {
			return base;
		}

		public RecipeChoice getAddition() {
			return base;
		}

		public RecipeChoice getTemplate() {
			return base;
		}

		public static class SmithingTransformRecipeEvent extends SmithingRecipeEvent {
			public SmithingTransformRecipeEvent(RecipeTypes recipeType) {
				super(recipeType);
			}
		}

		public static class SmithingTrimRecipeEvent extends SmithingRecipeEvent {
			public SmithingTrimRecipeEvent(RecipeTypes recipeType) {
				super(recipeType);
			}
		}
	}

	public static class StonecuttingRecipeEvent extends RegisterRecipeEvent {

		private RecipeChoice input;

		public StonecuttingRecipeEvent(RecipeTypes recipeType) {
			super(recipeType);
		}

		public void setInput(RecipeChoice input) {
			this.input = input;
		}

		public RecipeChoice getInput() {
			return input;
		}
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}
}
