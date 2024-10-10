package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.RegisterRecipeEvent;
import ch.njol.skript.util.RegisterRecipeEvent.*;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.Nullable;

public class EffCookingRecipeValues extends Effect {

	enum CookingRecipeValues {
		EXPERIENCE("set [the] recipe experience to %integer%", "recipe experience", CookingRecipeEvent.class,
			"This can only be used when registering a Blasting, Furnace, Campfire, or Smoking Recipe."),
		COOKINGTIME("set [the] recipe cook[ing] time to %timespan%", "recipe cooking time", CookingRecipeEvent.class,
			"This can only be used when registering a Blasting, Furnace, Campfire, or Smoking Recipe."),
		INPUT("set [the] recipe (input|source) [item] to %itemstack/itemtype%", "recipe input item", new Class[]{CookingRecipeEvent.class, StonecuttingRecipeEvent.class},
			"This can only be used when registering a Blasting, Furnace, Campfire, Smoking, or Stonecutting Recipe.");

		private String pattern, toString, error;
		private Class<? extends Event> eventClass;
		private Class<? extends Event>[] eventClasses;

		CookingRecipeValues(String pattern, String toString, Class<? extends Event> eventClass, String error) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClass = eventClass;
			this.error = error;
		}

		CookingRecipeValues(String pattern, String toString, Class<? extends Event>[] eventClasses, String error) {
			this.pattern = pattern;
			this.toString = toString;
			this.eventClasses = eventClasses;
			this.error = error;
		}
	}

	private static CookingRecipeValues[] recipeValues = CookingRecipeValues.values();

	static {
		String[] patterns = new String[recipeValues.length];
		for (CookingRecipeValues value : recipeValues) {
			patterns[value.ordinal()] = value.pattern;
		}
		Skript.registerEffect(EffCookingRecipeValues.class, patterns);
	}

	private CookingRecipeValues selectedValue;
	private @Nullable Expression<?> itemExpr;
	private @Nullable Expression<Integer> intExpr;
	private @Nullable Expression<Timespan> timeExpr;
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
			case INPUT -> itemExpr = exprs[0];
			case EXPERIENCE -> intExpr = (Expression<Integer>) exprs[0];
			case COOKINGTIME -> timeExpr = (Expression<Timespan>) exprs[0];
		}
		thisNode = getParser().getNode();
		thisScript = getParser().getCurrentScript().getConfig().getFileName();
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (event instanceof CookingRecipeEvent cookingEvent) {
			switch (selectedValue) {
				case EXPERIENCE -> {
					Integer experience = intExpr.getSingle(event);
					if (experience != null)
						cookingEvent.setExperience(experience.floatValue());
				}
				case INPUT -> {
					Object inputItem = itemExpr.getSingle(event);
					if (inputItem instanceof ItemStack itemStack) {
						cookingEvent.setInputItem(itemStack.getType());
					} else if (inputItem instanceof ItemType itemType) {
						cookingEvent.setInputItem(itemType.getMaterial());
					}
				}
				case COOKINGTIME -> {
					Timespan cookingTime = timeExpr.getSingle(event);
					if (cookingTime != null) {
						cookingEvent.setCookingTime((int) cookingTime.getAs(Timespan.TimePeriod.TICK));
					}
				}
			}
		} else if (event instanceof StonecuttingRecipeEvent stonecuttingEvent) {
			if (selectedValue == CookingRecipeValues.INPUT) {
				RecipeChoice choice = null;
				Object inputItem = itemExpr.getSingle(event);
				if (inputItem instanceof ItemStack itemStack) {
					choice = new RecipeChoice.ExactChoice(itemStack);
				} else if (inputItem instanceof ItemType itemType) {
					ItemStack stack = new ItemStack(itemType.getMaterial());
					stack.setItemMeta(itemType.getItemMeta());
					choice = new RecipeChoice.ExactChoice(stack);
				}
				if (choice != null) {
					stonecuttingEvent.setInput(choice);
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
