package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.SecRegisterRecipe;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class EffRegisterRecipeValues extends Effect {

	enum RecipeValues {
		PATTERNS("set [the] recipe pattern[s] to %-strings%", "recipe patterns"),
		FIRSTROW("set [the] recipe pattern[s] (first|1st) row to %-string%", "recipe pattern first row"),
		SECONDROW("set [the] recipe pattern[s] (second|2nd) row to %-string%", "recipe pattern second row"),
		THIRDROW("set [the] recipe pattern[s] (third|3rd) row to %-string%", "recipe pattern third row"),
		ITEMSET("set [the] recipe char[acter] %string% to %itemstack/itemtype%", "recipe character"),
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
	private Expression<String> stringValues;
	private @Nullable Expression<?> itemValues;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedValue = recipeValues[matchedPattern];
		if (!getParser().isCurrentEvent(SecRegisterRecipe.RegisterRecipeEvent.class)) {
			Skript.error("You can only use '" + selectedValue.toString + "' in a Register Recipe Section.");
			return false;
		}

		if (selectedValue == RecipeValues.ITEMSET) {
			stringValues = (Expression<String>) exprs[0];
			itemValues = exprs[1];
		} else if (selectedValue == RecipeValues.RESULT) {
			itemValues = exprs[0];
		} else {
			stringValues = (Expression<String>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof SecRegisterRecipe.RegisterRecipeEvent recipeEvent))
			return;

		switch (selectedValue) {
			case PATTERNS -> {
				for (String string : stringValues.getArray(event)) {
					if (string.length() > 3) {
						Skript.error("Recipe pattern can only contain up to 3 characters.");
						return;
					}
				}
				recipeEvent.setRecipePatterns(stringValues.getArray(event), null);
			}
			case FIRSTROW, SECONDROW, THIRDROW -> {
				String string = stringValues.getSingle(event);
				if (string.length() > 3) {
					Skript.error("Recipe pattern can only contain up to 3 characters.");
					return;
				}
				recipeEvent.setRecipePatterns(new String[]{string}, selectedValue.ordinal() - 1);
			}
			case ITEMSET -> {
				String string = stringValues.getSingle(event);
				if (string.length() > 1) {
					Skript.error("You can only set one character at a time to an item.");
					return;
				}
				Character character = string.charAt(0);
				Object item = itemValues.getSingle(event);
				if (item instanceof ItemStack itemStack) {
					recipeEvent.setIngredients(character, itemStack);
				} else if (item instanceof ItemType itemType) {
					ItemStack stack = new ItemStack(itemType.getMaterial());
					stack.setItemMeta(itemType.getItemMeta());
					recipeEvent.setIngredients(character, stack);
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
