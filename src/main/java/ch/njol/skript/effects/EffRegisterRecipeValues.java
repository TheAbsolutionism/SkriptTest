package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.SecRegisterRecipe;
import ch.njol.util.Kleenean;
import ch.njol.util.Predicate;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class EffRegisterRecipeValues extends Effect {

	enum RecipeValues {
		INGREDIENTS("set [the] recipe ingredient[s] to %itemstacks/itemtypes%", "recipe ingredients"),
		FIRSTROW("set [the] recipe ingredients of (first|1st) row to %itemstacks/itemtypes%", "recipe pattern first row"),
		SECONDROW("set [the] recipe ingredients of (second|2nd) row to %itemstacks/itemtypes%", "recipe pattern second row"),
		THIRDROW("set [the] recipe ingredients of (third|3rd) row to %itemstacks/itemtypes%", "recipe pattern third row"),
		RESULT("set [the] recipe result to %itemstack/itemtype%", "recipe result");

		private String pattern, toString;

		RecipeValues(String pattern, String toString) {
			this.pattern = pattern;
			this.toString = toString;
		}
	}

	private static RecipeValues[] recipeValues = RecipeValues.values();

	static {
		String[] patterns = new String[recipeValues.length];
		for (RecipeValues value : recipeValues) {
			patterns[value.ordinal()] = value.pattern;
		}
		Skript.registerEffect(EffRegisterRecipeValues.class, patterns);
	}

	private RecipeValues selectedValue;
	private Expression<?> itemValues;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedValue = recipeValues[matchedPattern];
		if (!getParser().isCurrentEvent(SecRegisterRecipe.RegisterRecipeEvent.class)) {
			Skript.error("You can only use '" + selectedValue.toString + "' in a Register Recipe Section.");
			return false;
		}
		itemValues = exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof SecRegisterRecipe.RegisterRecipeEvent recipeEvent))
			return;

		switch (selectedValue) {
			case INGREDIENTS -> {
				Object[] items = itemValues.getArray(event);
				if (items.length > recipeEvent.getMaxIngredients()) {
					Skript.adminBroadcast("You can only provide up to " + recipeEvent.getMaxIngredients() +
						" items when setting the ingredients for a '" + recipeEvent.getRecipeType() + "' recipe.");
					return;
				}
				for (int i = 0; i < items.length; i++) {
					Object thisItem = items[i];
					if (thisItem instanceof ItemStack itemStack) {
						recipeEvent.setIngredients(i, itemStack);
					} else if (thisItem instanceof ItemType itemType) {
						ItemStack stack = new ItemStack(itemType.getMaterial());
						stack.setItemMeta(itemType.getItemMeta());
						recipeEvent.setIngredients(i, stack);
					}
				}
			}
			case FIRSTROW, SECONDROW, THIRDROW -> {
				Object[] items = itemValues.getArray(event);
				if (items.length > recipeEvent.getMaxRowIngredients()) {
					if (recipeEvent.getMaxRowIngredients() == 0) {
						Skript.error("You can not use '" + selectedValue.toString + "' when registering a '" + recipeEvent.getRecipeType() + "' recipe.");
					} else {
						Skript.error("You can only provide up to " + recipeEvent.getMaxRowIngredients() +
							" items when setting the ingredients of a row for a '" + recipeEvent.getRecipeType() + "' recipe.");
					}
					return;
				}
				for (int i = 0; i  < items.length; i++) {
					Object thisItem = items[i];
					int placement = (3 * (selectedValue.ordinal() - 1)) + i;
					if (thisItem instanceof ItemStack itemStack) {
						recipeEvent.setIngredients(placement, itemStack);
					} else if (thisItem instanceof ItemType itemType) {
						ItemStack stack = new ItemStack(itemType.getMaterial());
						stack.setItemMeta(itemType.getItemMeta());
						recipeEvent.setIngredients(placement, stack);
					}
				}
			}
			case RESULT -> {
				Object item = itemValues.getSingle(event);
				if (item instanceof ItemStack itemStack) {
					recipeEvent.setResultItem(itemStack);
				} else if (item instanceof ItemType itemType) {
					ItemStack stack = new ItemStack(itemType.getMaterial());
					stack.setItemMeta(itemType.getItemMeta());
					recipeEvent.setResultItem(stack);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}
}
