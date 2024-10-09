package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.RegisterRecipeEvent;
import ch.njol.skript.util.RegisterRecipeEvent.*;
import ch.njol.skript.util.RegisterRecipeEvent.CraftingEventRecipe.*;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class EffCraftingRecipeValues extends Effect {

	enum RecipeValues {
		INGREDIENTS("set [the] recipe ingredients to %itemstacks/itemtypes%", "recipe ingredients",
			CraftingEventRecipe.class, "This can only be used when registering a Crafting, Shaped, or Shapeless Recipe"),
		FIRSTROW("set [the] recipe ingredients of (first|1st) row to %itemstacks/itemtypes%", "recipe ingredients first row",
			ShapedRecipeEvent.class, "This can only be used when registering a Shaped Recipe."),
		SECONDROW("set [the] recipe ingredients of (second|2nd) row to %itemstacks/itemtypes%", "recipe ingredients second row",
			ShapedRecipeEvent.class, "This can only be used when registering a Shaped Recipe."),
		THIRDROW("set [the] recipe ingredients of (third|3rd) row to %itemstacks/itemtypes%", "recipe ingredients third row",
			ShapedRecipeEvent.class, "This can only be used when registering a Shaped Recipe.");

		private String pattern, toString, error;
		private Class<? extends Event> eventClass;

		RecipeValues(String pattern, String toString, Class<? extends Event> eventClass, String error) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClass = eventClass;
			this.error = error;
		}
	}

	private static RecipeValues[] recipeValues = RecipeValues.values();

	static {
		String[] patterns = new String[recipeValues.length];
		for (RecipeValues value : recipeValues) {
			patterns[value.ordinal()] = value.pattern;
		}
		Skript.registerEffect(EffCraftingRecipeValues.class, patterns);
	}

	private RecipeValues selectedValue;
	private Expression<?> itemValues;
	private Node thisNode;
	private String thisScript;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedValue = recipeValues[matchedPattern];
		if (!getParser().isCurrentEvent(RegisterRecipeEvent.class)) {
			Skript.error("You can only use '" + selectedValue.toString + "' in a Register Recipe Section.");
			return false;
		}
		if (!getParser().isCurrentEvent(selectedValue.eventClass)) {
			Skript.error(selectedValue.error);
			return false;
		}
		itemValues = exprs[0];
		thisNode = getParser().getNode();
		thisScript = getParser().getCurrentScript().getConfig().getFileName();
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof CraftingEventRecipe recipeEvent))
			return;

		switch (selectedValue) {
			case INGREDIENTS -> {
				Object[] items = itemValues.getArray(event);
				if (items.length > recipeEvent.getMaxIngredients()) {
					customError("You can only provide up to " + recipeEvent.getMaxIngredients() +
						" items when setting the ingredients for a '" + recipeEvent.getRecipeName() + "' recipe.");
					recipeEvent.setErrorInEffect();
					return;
				}
				for (int i = 0; i < items.length; i++) {
					Object thisItem = items[i];
					if (thisItem instanceof ItemStack itemStack && itemStack.getType() != Material.AIR) {
						recipeEvent.setIngredients(i, itemStack);
					} else if (thisItem instanceof ItemType itemType && itemType.getMaterial() != Material.AIR) {
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
						customError("You can not use '" + selectedValue.toString + "' when registering a '" + recipeEvent.getRecipeName() + "' recipe.");
					} else {
						customError("You can only provide up to " + recipeEvent.getMaxRowIngredients() +
							" items when setting the ingredients of a row for a '" + recipeEvent.getRecipeName() + "' recipe.");
					}
					recipeEvent.setErrorInEffect();
					return;
				}
				for (int i = 0; i < items.length; i++) {
					Object thisItem = items[i];
					int placement = (3 * (selectedValue.ordinal() - 1)) + i;
					if (thisItem instanceof ItemStack itemStack && itemStack.getType() != Material.AIR) {
						recipeEvent.setIngredients(placement, itemStack);
					} else if (thisItem instanceof ItemType itemType && itemType.getMaterial() != Material.AIR) {
						ItemStack stack = new ItemStack(itemType.getMaterial());
						stack.setItemMeta(itemType.getItemMeta());
						recipeEvent.setIngredients(placement, stack);
					}
				}
			}
		}
	}

	private void customError(String message) {
		Skript.info("Line " + thisNode.getLine() + ": (" + thisScript + ")\n\t" + message + "\n\t" + thisNode.getKey());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}
}
