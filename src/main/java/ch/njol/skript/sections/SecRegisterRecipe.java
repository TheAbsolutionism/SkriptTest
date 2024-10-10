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
import ch.njol.skript.util.RegisterRecipeEvent.RecipeTypes;
import ch.njol.skript.util.RegisterRecipeEvent.*;
import ch.njol.skript.util.RegisterRecipeEvent.CraftingRecipeEvent.*;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecRegisterRecipe extends Section {
	/*
	TODO:
		set category effect
		CondDiscoveredRecipe
		EffDiscoverRecipe
		Tests
		Maybe CraftingBookCategory and CookingBookCategory lang
	 */
	private static final boolean SUPPORT_SHAPED_ITEMSTACK = Skript.methodExists(ShapedRecipe.class, "setIngredient", Character.class, ItemStack.class);
	private static final boolean SUPPORT_SHAPELESS_ITEMSTACK = Skript.methodExists(ShapelessRecipe.class, "addIngredient", ItemStack.class);

	private static final RecipeTypes[] recipeTypes = RecipeTypes.values();

	static {
		String[] patterns = new String[recipeTypes.length];
		for (RecipeTypes type : recipeTypes) {
			patterns[type.ordinal()] = "(register|create) [a] [new] " + type.getPattern() + " recipe with [the] name[space[key]] %string%";
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
		trigger = loadCode(sectionNode, "register recipe", afterLoading, recipeType.getEventClass());
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
		NamespacedKey key = NamespacedKey.fromString(name, Skript.getInstance());
		RegisterRecipeEvent recipeEvent = switch (recipeType) {
			case SHAPED -> new ShapedRecipeEvent(recipeType);
			case SHAPELESS -> new ShapelessRecipeEvent(recipeType);
			case COOKING, BLASTING, FURNACE, CAMPFIRE, SMOKING -> new CookingRecipeEvent(recipeType);
			case SMITHINGTRANSFORM, SMITHINGTRIM -> new SmithingRecipeEvent(recipeType);
			case STONECUTTING -> new StonecuttingRecipeEvent(recipeType);
		};
		Variables.setLocalVariables(recipeEvent, Variables.copyLocalVariables(event));
		TriggerItem.walk(trigger, recipeEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(recipeEvent));
		Variables.removeLocals(recipeEvent);
		if (recipeEvent.getErrorInEffect())
			return super.walk(event, false);
		ItemStack result = recipeEvent.getResultItem();
		if (result == null && (recipeType != RecipeTypes.SMITHINGTRIM)) {
			customError("You must provide a result item when registering a recipe.");
			return super.walk(event, false);
		}
		switch (recipeType) {
			case SHAPED, SHAPELESS -> {
				if (recipeEvent instanceof CraftingRecipeEvent craftingRecipe) {
					ItemStack[] ingredients = craftingRecipe.getIngredients();
					if (ingredients.length < 2 || Arrays.stream(ingredients).filter(Objects::nonNull).toArray().length < 2) {
						customError("You must have at least 2 ingredients when registering a '" + recipeType.getToString() + "' recipe.");
						return super.walk(event, false);
					}
					String group = craftingRecipe.getGroup();
					CraftingBookCategory category = craftingRecipe.getCategory();
					switch (recipeType) {
						case SHAPED -> createShapedRecipe(key, result, ingredients, group, category);
						case SHAPELESS -> createShapelessRecipe(key, result, ingredients, group, category);
					}
				}
			}
			case COOKING, BLASTING, FURNACE, CAMPFIRE, SMOKING -> {
				if (recipeEvent instanceof CookingRecipeEvent cookingRecipe) {
					Material inputItem = cookingRecipe.getInputItem();
					String group = cookingRecipe.getGroup();
					CookingBookCategory category = cookingRecipe.getCategory();
					int cookingTime = cookingRecipe.getCookingTime();
					float experience = cookingRecipe.getExperience();
					createCookingRecipe(recipeType, key, result, inputItem, group, category, cookingTime, experience);
				}
			}
			case SMITHINGTRANSFORM, SMITHINGTRIM -> {
				if (recipeEvent instanceof SmithingRecipeEvent smithingRecipe) {
					RecipeChoice base = smithingRecipe.getBase();
					RecipeChoice template = smithingRecipe.getTemplate();
					RecipeChoice addition = smithingRecipe.getAddition();
					createSmithingRecipe(recipeType, key, result, base, template, addition);
				}
			}
			case STONECUTTING -> {
				if (recipeEvent instanceof StonecuttingRecipeEvent stonecuttingRecipe) {
					RecipeChoice input = stonecuttingRecipe.getInput();
					createStonecuttingRecipe(key, result, input);
				}
			}
		}

		return super.walk(event, false);
	}

	private void completeRecipe(NamespacedKey key, Recipe recipe) {
		if (Bukkit.getRecipe(key) != null)
			Bukkit.removeRecipe(key);
		Bukkit.addRecipe(recipe);
	}

	private void createShapedRecipe(NamespacedKey key, ItemStack result, ItemStack[] ingredients, String group, CraftingBookCategory category) {
		ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
		if (category != null)
			shapedRecipe.setCategory(category);
		if (group != null && !group.isEmpty())
			shapedRecipe.setGroup(group);
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
		completeRecipe(key, shapedRecipe);
	}

	private void createShapelessRecipe(NamespacedKey key, ItemStack result, ItemStack[] ingredients, String group, CraftingBookCategory category) {
		ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, result);
		if (category != null)
			shapelessRecipe.setCategory(category);
		if (group != null && !group.isEmpty())
			shapelessRecipe.setGroup(group);
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
		completeRecipe(key, shapelessRecipe);
	}

	private void createCookingRecipe(RecipeTypes recipeType, NamespacedKey key, ItemStack result, Material inputItem, String group, CookingBookCategory category, int cookingTime, float experience) {
		var recipe = switch (recipeType) {
			case BLASTING -> new BlastingRecipe(key, result, inputItem, experience, cookingTime);
			case CAMPFIRE -> new CampfireRecipe(key, result, inputItem, experience, cookingTime);
			case FURNACE -> new FurnaceRecipe(key, result, inputItem, experience, cookingTime);
			case SMOKING -> new SmokingRecipe(key, result, inputItem, experience, cookingTime);
			default -> null;
		};
		if (recipe == null)
			return;
		if (category != null)
			recipe.setCategory(category);
		if (group != null && !group.isEmpty())
			recipe.setGroup(group);
		completeRecipe(key, recipe);
	}

	private void createSmithingRecipe(RecipeTypes recipeType, NamespacedKey key, ItemStack result, RecipeChoice base, RecipeChoice template, RecipeChoice addition) {
		if (base == null || template == null || addition == null) {
			customError("Unable to create " + recipeType.getToString() + " recipe, missing data.");
			return;
		}
		var recipe = switch (recipeType) {
			case SMITHINGTRANSFORM -> new SmithingTransformRecipe(key, result, template, base, addition);
			case SMITHINGTRIM -> new SmithingTrimRecipe(key, template, base, addition);
			default -> null;
		};
		if (recipe == null)
			return;
		completeRecipe(key, recipe);
	}

	private void createStonecuttingRecipe(NamespacedKey key, ItemStack result, RecipeChoice input) {
		if (input == null) {
			customError("Unable to create a stonecutting recipe, missing data.");
			return;
		}
		StonecuttingRecipe recipe = new StonecuttingRecipe(key, result, input);
		completeRecipe(key, recipe);
	}

	private void customError(String message) {
		Skript.info("Line " + thisNode.getLine() + ": (" + thisScript + ")\n\t" + message + "\n\t" + thisNode.getKey());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "register a new " + recipeType.getToString() + " recipe with the namespacekey " + providedName.toString(event, debug);
	}

}
