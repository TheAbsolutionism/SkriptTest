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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecRegisterRecipe extends Section {

	private static final boolean SUPPORT_SHAPED_ITEMSTACK = Skript.methodExists(ShapedRecipe.class, "setIngredient", Character.class, ItemStack.class);

	public static class RegisterRecipeEvent extends Event {
		private String[] recipePatterns = new String[3];
		private Map<Character, ItemStack> ingredients = new HashMap<>();
		private ItemStack resultItem;

		public RegisterRecipeEvent() {}

		public void setRecipePatterns(String @Nullable [] pattern, @Nullable Integer row) {
			if (row != null) {
				recipePatterns[row] = pattern[0];
			} else {
				recipePatterns = pattern;
			}
		}

		public void setIngredients(Character character, ItemStack item) {
			ingredients.put(character, item);
		}

		public void setResultItem(ItemStack item) {
			resultItem = item;
		}

		public String[] getRecipePatterns() {
			return recipePatterns;
		}

		public Map<Character, ItemStack> getIngredients() {
			return ingredients;
		}

		public ItemStack getResultItem() {
			return resultItem;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(SecRegisterRecipe.class,
			"(register|create) [a] [new] shaped recipe with [the] name[space[key]] %string%");
	}

	private Expression<String> providedName;
	private Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
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
		RegisterRecipeEvent recipeEvent = new RegisterRecipeEvent();
		Variables.setLocalVariables(recipeEvent, Variables.copyLocalVariables(event));
		TriggerItem.walk(trigger, recipeEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(recipeEvent));
		Variables.removeLocals(recipeEvent);

		String[] patterns = recipeEvent.getRecipePatterns();
		Map<Character, ItemStack> ingredients = recipeEvent.getIngredients();
		for (String string : patterns) {
			for (Character character : string.toCharArray()) {
				if (ingredients.get(character) == null) {
					ingredients.put(character, new ItemStack(Material.AIR));
				}
			}
		}
		ItemStack result = recipeEvent.getResultItem();
		if (result == null) {
			Skript.error("Registering a recipe requires a resulting item");
			return null;
		}
		ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);
		shapedRecipe.shape(patterns);
		for (Character character : ingredients.keySet()) {
			if (SUPPORT_SHAPED_ITEMSTACK) {
				shapedRecipe.setIngredient(character, ingredients.get(character));
			} else {
				shapedRecipe.setIngredient(character, ingredients.get(character).getType());
			}
		}
		Bukkit.addRecipe(shapedRecipe);

		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
