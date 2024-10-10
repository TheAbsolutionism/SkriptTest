package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.RegisterRecipeEvent;
import ch.njol.skript.util.RegisterRecipeEvent.*;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EffRegisterRecipeValues extends Effect {

	enum RegisterRecipeValues {
		RESULT("set [the] recipe result to %itemstack/itemtype%", "recipe result", RegisterRecipeEvent.class),
		GROUP("set [the] recipe group to %string%", "recipe group", new Class[]{CraftingRecipeEvent.class, CookingRecipeEvent.class},
			"This can only be used when registering a Shaped, Shapeless, Blasting, Furnace, Campfire, or Smoking Recipe.");

		private String pattern, toString, error;
		private Class<? extends Event> eventClass;
		private Class<? extends Event>[] eventClasses;

		RegisterRecipeValues(String pattern, String toString, Class<? extends Event> eventClass) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClass = eventClass;
		}

		RegisterRecipeValues(String pattern, String toString, Class<? extends Event>[] eventClasses, String error) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClasses = eventClasses;
			this.error = error;
		}
	}

	private static RegisterRecipeValues[] recipeValues = RegisterRecipeValues.values();

	static {
		String[] patterns = new String[recipeValues.length];
		for (RegisterRecipeValues value : recipeValues) {
			patterns[value.ordinal()] = value.pattern;
		}
		Skript.registerEffect(EffRegisterRecipeValues.class, patterns);
	}

	private RegisterRecipeValues selectedValue;
	private @Nullable Expression<?> itemExpr;
	private @Nullable Expression<String> groupExpr;
	private Node thisNode;
	private String thisScript;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedValue = recipeValues[matchedPattern];
		if (!getParser().isCurrentEvent(RegisterRecipeEvent.class)) {
			Skript.error("You can only use '" + selectedValue.toString + "' in a Register Recipe Section.");
			return false;
		}
		if (selectedValue.eventClass != null && !getParser().isCurrentEvent(selectedValue.eventClass)) {
			Skript.error(selectedValue.error);
			return false;
		} else if (selectedValue.eventClasses != null) {
			boolean classFound = false;
			for (Class<? extends Event> clazz : selectedValue.eventClasses) {
				if (getParser().isCurrentEvent(clazz)) {
					classFound = true;
					break;
				}
			}
			if (!classFound) {
				Skript.error(selectedValue.error);
				return false;
			}
		}
		switch (selectedValue) {
			case RESULT -> itemExpr = exprs[0];
			case GROUP -> groupExpr = (Expression<String>) exprs[0];
		}
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
				Object item = itemExpr.getSingle(event);
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
			case GROUP -> {
				String group = groupExpr.getSingle(event);
				if (group == null || group.isEmpty()) {
					customError("The group can not be null or blank.");
					return;
				}
				if (event instanceof CookingRecipeEvent cookingEvent) {
					cookingEvent.setGroup(group);
				} else if (event instanceof CraftingRecipeEvent craftingEvent) {
					craftingEvent.setGroup(group);
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
