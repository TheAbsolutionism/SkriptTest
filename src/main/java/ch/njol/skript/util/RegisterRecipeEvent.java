package ch.njol.skript.util;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;

public class RegisterRecipeEvent extends Event {

	public enum RecipeTypes {
		SHAPED("shaped [crafting]", "shaped", CraftingRecipeEvent.ShapedRecipeEvent.class, ShapedRecipe.class),
		SHAPELESS("shapeless [crafting]", "shapeless", CraftingRecipeEvent.ShapelessRecipeEvent.class, ShapelessRecipe.class),
		BLASTING("blast[ing]", "blasting", CookingRecipeEvent.BlastingRecipeEvent.class, BlastingRecipe.class),
		CAMPFIRE("campfire", "campfire", CookingRecipeEvent.CampfireRecipeEvent.class, CampfireRecipe.class),
		FURNACE("furnace", "furnace", CookingRecipeEvent.FurnaceRecipeEvent.class, FurnaceRecipe.class),
		SMOKING("smoking", "smoking", CookingRecipeEvent.SmokingRecipeEvent.class, SmokingRecipe.class),
		COOKING("cooking", "cooking", CookingRecipeEvent.class, CookingRecipe.class),
		SMITHINGTRANSFORM("smith[ing] transform", "smithing transform", SmithingRecipeEvent.SmithingTransformRecipeEvent.class, SmithingTransformRecipe.class),
		SMITHINGTRIM("smith[ing] trim", "smithing trim", SmithingRecipeEvent.SmithingTrimRecipeEvent.class, SmithingTrimRecipe.class),
		STONECUTTING("stone cutting", "stone cutting", StonecuttingRecipeEvent.class, StonecuttingRecipe.class);


		private String pattern, toString;
		private Class<? extends Event> eventClass;
		private Class<? extends Recipe> recipeClass;

		RecipeTypes(String pattern, String toString, Class<? extends Event> eventClass, Class<? extends Recipe> recipeClass) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClass = eventClass;
			this.recipeClass = recipeClass;
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
		return recipeType.toString;
	}

	public Class<? extends Recipe> getRecipeClass() {
		return recipeType.recipeClass;
	}

	public static class CraftingRecipeEvent extends RegisterRecipeEvent {
		private RecipeChoice[] ingredients = new RecipeChoice[9];
		private CraftingBookCategory category = CraftingBookCategory.MISC;
		private String group;

		public CraftingRecipeEvent(RecipeTypes recipeType) {
			super(recipeType);
		};

		public void setIngredients(int placement, RecipeChoice item) {
			ingredients[placement] = item;
		}

		public void setCategory(CraftingBookCategory category) {
			this.category = category;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public RecipeChoice[] getIngredients() {
			return ingredients;
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
		private RecipeChoice input;
		private CookingBookCategory category = CookingBookCategory.MISC;
		private String group;
		private int cookingTime = 10;
		private float experience = 0;

		public CookingRecipeEvent(RecipeTypes recipeType) {
			super(recipeType);
		};

		public void setInput(RecipeChoice input) {
			this.input = input;
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

		public RecipeChoice getInput() {
			return input;
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
