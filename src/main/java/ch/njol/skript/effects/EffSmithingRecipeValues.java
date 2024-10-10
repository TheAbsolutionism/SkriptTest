package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.RegisterRecipeEvent;
import ch.njol.skript.util.RegisterRecipeEvent.*;
import ch.njol.skript.util.RegisterRecipeEvent.CraftingRecipeEvent.*;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EffSmithingRecipeValues extends Effect {

	enum SmithingRecipeValues {
		BASE("set [the] recipe base item[s] to %itemstacks/itemtypes%", "recipe base item", SmithingRecipeEvent.class,
			"This can only be used when registering a Smithing Transform or Smithing Trim Recipe."),
		TEMPLATE("set [the] recipe template item[s] to %itemstacks/itemtypes%", "recipe template item", SmithingRecipeEvent.class,
			"This can only be used when registering a Smithing Transform or Smithing Trim Recipe."),
		ADDITION("set [the] recipe addition[al] item[s] to %itemstacks/itemtypes%", "recipe additional item", SmithingRecipeEvent.class,
			"This can only be used when registering a Smithing Transform or Smithing Trim Recipe.");

		private String pattern, toString, error;
		private Class<? extends Event> eventClass;

		SmithingRecipeValues(String pattern, String toString, Class<? extends Event> eventClass, String error) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClass = eventClass;
			this.error = error;
		}
	}

	private static SmithingRecipeValues[] recipeValues = SmithingRecipeValues.values();

	static {
		String[] patterns = new String[recipeValues.length];
		for (SmithingRecipeValues value : recipeValues) {
			patterns[value.ordinal()] = value.pattern;
		}
		Skript.registerEffect(EffSmithingRecipeValues.class, patterns);
	}

	private SmithingRecipeValues selectedValue;
	private Expression<?> itemExpr;
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
		itemExpr = exprs[0];
		thisNode = getParser().getNode();
		thisScript = getParser().getCurrentScript().getConfig().getFileName();
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof SmithingRecipeEvent smithingEvent))
			return;
		List<ItemStack> items = new ArrayList<>();
		for (Object item : itemExpr.getArray(event)) {
			if (item instanceof ItemStack itemStack) {
				items.add(itemStack);
			} else if (item instanceof ItemType itemType) {
				ItemStack stack = new ItemStack(itemType.getMaterial());
				stack.setItemMeta(itemType.getItemMeta());
				items.add(stack);
			}
		}
		if (items.isEmpty()) {
			customError("You must provide valid items when setting the " + selectedValue.toString + " of a recipe.");
			return;
		}
		RecipeChoice choice = new RecipeChoice.ExactChoice(items);
		switch (selectedValue) {
			case BASE -> smithingEvent.setBase(choice);
			case TEMPLATE -> smithingEvent.setTemplate(choice);
			case ADDITION -> smithingEvent.setAddition(choice);
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
