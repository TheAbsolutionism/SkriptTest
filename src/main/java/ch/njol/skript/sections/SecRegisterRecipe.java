package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.RegisterRecipeEvent;
import ch.njol.skript.util.RegisterRecipeEvent.*;
import ch.njol.skript.util.RegisterRecipeEvent.CraftingRecipeEvent.*;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecRegisterRecipe extends Section {

	private static final boolean SUPPORT_SHAPED_ITEMSTACK = Skript.methodExists(ShapedRecipe.class, "setIngredient", Character.class, ItemStack.class);
	private static final boolean SUPPORT_SHAPELESS_ITEMSTACK = Skript.methodExists(ShapelessRecipe.class, "addIngredient", ItemStack.class);

	public enum RecipeTypes {
		SHAPED("shaped", 9, 3, 2, "shaped", ShapedRecipeEvent.class),
		SHAPELESS("shapeless", 9, 0, 2, "shapeless", ShapelessRecipeEvent.class);


		private String pattern, toString;
		private int maxIngredients, maxRowIngredients, minIngredients;
		private Class<? extends Event> eventClass;

		RecipeTypes(String pattern, int maxIngredients, int maxRowIngredients, int minIngredients, String toString, Class<? extends Event> eventClass) {
			this.pattern = pattern;
			this.maxIngredients = maxIngredients;
			this.maxRowIngredients = maxRowIngredients;
			this.minIngredients = minIngredients;
			this.toString  = toString;
			this.eventClass = eventClass;
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

		public int getMinIngredients() {
			return minIngredients;
		}
	}

	private static final RecipeTypes[] recipeTypes = RecipeTypes.values();

	static {
		String[] patterns = new String[recipeTypes.length];
		for (RecipeTypes type : recipeTypes) {
			patterns[type.ordinal()] = "(register|create) [a] [new] " + type.pattern + " recipe with [the] name[space[key]] %string%";
		}
		Skript.registerSection(SecRegisterRecipe.class, patterns);
	}

	private RecipeTypes recipeType;
	private Expression<String> providedName;
	private Trigger trigger;
	private Node thisNode;
	private String thisScript;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		recipeType = recipeTypes[matchedPattern];
		providedName = (Expression<String>) exprs[0];
		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		trigger = loadCode(sectionNode, "register recipe", afterLoading, recipeType.eventClass);
		if (delayed.get()) {
			Skript.error("Delays cannot be used within a 'register recipe' section.");
			return false;
		}
		thisNode = getParser().getNode();
		thisScript = getParser().getCurrentScript().getConfig().getFileName();
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		String name = providedName.getSingle(event);
		NamespacedKey namespacedKey = NamespacedKey.fromString(name, Skript.getInstance());
		RegisterRecipeEvent recipeEvent = switch (recipeType) {
			case SHAPED -> new ShapedRecipeEvent(recipeType);
			case SHAPELESS -> new ShapelessRecipeEvent(recipeType);
		};
		Variables.setLocalVariables(recipeEvent, Variables.copyLocalVariables(event));
		TriggerItem.walk(trigger, recipeEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(recipeEvent));
		Variables.removeLocals(recipeEvent);
		if (recipeEvent.getErrorInEffect())
			return super.walk(event, false);
		ItemStack result = recipeEvent.getResultItem();
		if (result == null) {
			customError("You must provide a result item when registering a recipe.");
			return super.walk(event, false);
		}
		if (Bukkit.getRecipe(namespacedKey) != null)
			Bukkit.removeRecipe(namespacedKey);
		switch (recipeType) {
			case SHAPED, SHAPELESS -> {
				if (!(recipeEvent instanceof CraftingRecipeEvent craftingRecipe))
					return super.walk(event, false);
				ItemStack[] ingredients = craftingRecipe.getIngredients();
				if (ingredients.length < recipeType.minIngredients || Arrays.stream(ingredients).filter(Objects::nonNull).toArray().length < recipeType.minIngredients) {
					customError("You must have at least " + recipeType.minIngredients + " ingredients when registering a '" + recipeType.toString + "' recipe.");
					return super.walk(event, false);
				}
				switch (recipeType) {
					case SHAPED -> createShapedRecipe(namespacedKey, result, ingredients);
					case SHAPELESS -> createShapelessRecipe(namespacedKey, result, ingredients);
				}
			}
		}





		return super.walk(event, false);
	}

	private void createShapedRecipe(NamespacedKey key, ItemStack result, ItemStack[] ingredients) {
		ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
		Character[] characters = new Character[]{'a','b','c','d','e','f','g','h','i'};
		shapedRecipe.shape("abc","def","ghi");
		Skript.adminBroadcast("Ingredients: " + Arrays.toString(ingredients));
		for (int i = 0; i < ingredients.length; i++) {
			ItemStack thisItem = ingredients[i];
			if (thisItem != null) {
				if (SUPPORT_SHAPED_ITEMSTACK) {
					shapedRecipe.setIngredient(characters[i], thisItem);
				} else {
					shapedRecipe.setIngredient(characters[i], thisItem.getType());
				}
			}
		}
		Bukkit.addRecipe(shapedRecipe);
	}

	private void createShapelessRecipe(NamespacedKey key, ItemStack result, ItemStack[] ingredients) {
		ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, result);
		for (int i = 0; i < ingredients.length; i++) {
			ItemStack thisItem = ingredients[i];
			if (thisItem != null) {
				if (SUPPORT_SHAPELESS_ITEMSTACK) {
					shapelessRecipe.addIngredient(thisItem);
				} else {
					shapelessRecipe.addIngredient(thisItem.getAmount(), thisItem.getType());
				}
			}
		}
		Bukkit.addRecipe(shapelessRecipe);
	}

	private void customError(String message) {
		Skript.info("Line " + thisNode.getLine() + ": (" + thisScript + ")\n\t" + message + "\n\t" + thisNode.getKey());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "register a new " + recipeType.toString + " recipe with the namespacekey " + providedName.toString(event, debug);
	}

}
