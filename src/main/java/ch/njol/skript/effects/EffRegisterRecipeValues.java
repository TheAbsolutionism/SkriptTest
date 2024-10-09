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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class EffRegisterRecipeValues extends Effect {

	enum RecipeValues {
		RESULT("set [the] recipe result to %itemstack/itemtype%", "recipe result", RegisterRecipeEvent.class);

		private String pattern, toString;
		private Class<? extends Event> eventClass;

		RecipeValues(String pattern, String toString, Class<? extends Event> eventClass) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClass = eventClass;
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
	private Node thisNode;
	private String thisScript;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedValue = recipeValues[matchedPattern];
		if (!getParser().isCurrentEvent(selectedValue.eventClass)) {
			Skript.error("You can only use '" + selectedValue.toString + "' in a Register Recipe Section.");
			return false;
		}
		itemValues = exprs[0];
		thisNode = getParser().getNode();
		thisScript = getParser().getCurrentScript().getConfig().getFileName();
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof RegisterRecipeEvent recipeEvent))
			return;

		switch (selectedValue) {
			case RESULT -> {
				Object item = itemValues.getSingle(event);
				if (item instanceof ItemStack itemStack && itemStack.getType() != Material.AIR) {
					recipeEvent.setResultItem(itemStack);
				} else if (item instanceof ItemType itemType && itemType.getMaterial() != Material.AIR) {
					ItemStack stack = new ItemStack(itemType.getMaterial());
					stack.setItemMeta(itemType.getItemMeta());
					recipeEvent.setResultItem(stack);
				} else {
					customError("The result item can not be null and/or air.");
					recipeEvent.setErrorInEffect();
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
