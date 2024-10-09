package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecRegisterRecipe extends Section {

	private static final boolean SUPPORT_SHAPED_ITEMSTACK = Skript.methodExists(ShapedRecipe.class, "setIngredient", Character.class, ItemStack.class);
	private static final boolean SUPPORT_SHAPELESS_ITEMSTACK = Skript.methodExists(ShapelessRecipe.class, "addIngredient", ItemStack.class);

	enum RecipeTypes {
		SHAPED("shaped", 9, 3, 2, "shaped"),
		SHAPELESS("shapeless", 9, 0, 2, "shapeless");


		private String pattern, toString;
		private int maxIngredients, maxRowIngredients, minIngredients;

		RecipeTypes(String pattern, int maxIngredients, int maxRowIngredients, int minIngredients, String toString) {
			this.pattern = pattern;
			this.maxIngredients = maxIngredients;
			this.maxRowIngredients = maxRowIngredients;
			this.minIngredients = minIngredients;
			this.toString  = toString;
		}
	}

	private static final RecipeTypes[] recipeTypes = RecipeTypes.values();

	public static class RegisterRecipeEvent extends Event {
		private ItemStack resultItem;
		private ItemStack[] ingredients = new ItemStack[9];
		private RecipeTypes recipeType;

		@SuppressWarnings("ClassEscapesDefinedScope")
		public RegisterRecipeEvent(RecipeTypes recipeType) {
			this.recipeType = recipeType;
		}

		public void setIngredients(int placement, ItemStack item) {
			ingredients[placement] = item;
		}

		public void setResultItem(ItemStack item) {
			resultItem = item;
		}

		public ItemStack[] getIngredients() {
			return ingredients;
		}

		public ItemStack getResultItem() {
			return resultItem;
		}

		public RecipeTypes getRecipeType() {
			return recipeType;
		}

		public int getMaxIngredients() {
			return recipeType.maxIngredients;
		}

		public int getMaxRowIngredients() {
			return recipeType.maxRowIngredients;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

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

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		recipeType = recipeTypes[matchedPattern];
		providedName = (Expression<String>) exprs[0];
		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		trigger = loadCode(sectionNode, "register recipe", afterLoading, RegisterRecipeEvent.class);
		if (delayed.get()) {
			Skript.error("Delays cannot be used within a 'register recipe' section.");
			return false;
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		String name = providedName.getSingle(event);
		NamespacedKey namespacedKey = NamespacedKey.fromString(name, Skript.getInstance());
		if (Bukkit.getRecipe(namespacedKey) != null) {
			Skript.error("The namespace '" + name + "' is already registered.");
			return null;
		}
		RegisterRecipeEvent recipeEvent = new RegisterRecipeEvent(recipeType);
		Variables.setLocalVariables(recipeEvent, Variables.copyLocalVariables(event));
		TriggerItem.walk(trigger, recipeEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(recipeEvent));
		Variables.removeLocals(recipeEvent);

		ItemStack result = recipeEvent.getResultItem();
		if (result == null) {
			Skript.error("Registering a recipe requires a resulting item");
			return null;
		}
		ItemStack[] ingredients = recipeEvent.getIngredients();
		if (ingredients.length < recipeType.minIngredients || Arrays.stream(ingredients).filter(Objects::nonNull).toArray().length < recipeType.minIngredients) {
			Skript.error("You must have at least " + recipeType.minIngredients + " ingredients when registering a '" + recipeType.toString + "' recipe.");
			return null;
		}
		switch (recipeType) {
			case SHAPED -> createShapedRecipe(namespacedKey, result, ingredients);
			case SHAPELESS -> createShapelessRecipe(namespacedKey, result, ingredients);
		}

		return super.walk(event, false);
	}

	private void createShapedRecipe(NamespacedKey key, ItemStack result, ItemStack[] ingredients) {
		ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
		Character[] characters = new Character[]{'a','b','c','d','e','f','g','h','i'};
		shapedRecipe.shape("abc","def","ghi");
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
				if (SUPPORT_SHAPED_ITEMSTACK) {
					shapelessRecipe.addIngredient(thisItem);
				} else {
					shapelessRecipe.addIngredient(thisItem.getAmount(), thisItem.getType());
				}
			}
		}
		Bukkit.addRecipe(shapelessRecipe);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
