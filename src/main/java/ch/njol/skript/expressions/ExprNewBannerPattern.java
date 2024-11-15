package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

@Name("New Banner Pattern")
@Description("Create a new banner pattern.")
@Examples({
	"set {_pattern} to a new creeper banner pattern colored red",
	"add {_pattern} to banner patterns of {_banneritem}",
	"remove {_pattern} from banner patterns of {_banneritem}",
	"set the 1st banner pattern of block at location(0,0,0) to {_pattern}",
	"clear the 1st banner pattern of block at location(0,0,0)"
})
@Since("INSERT VERSION")
public class ExprNewBannerPattern extends SimpleExpression<Pattern> {

	private static Object[] patternTypes;
	private static Map<Integer, Object> patternCorrelation = new HashMap<>();

	static {
		if (BukkitUtils.registryExists("BANNER_PATTERN")) {
			try {
				Class<?> registryClass = Class.forName("org.bukkit.Registry");
				Object bannerRegistry = registryClass.getField("BANNER_PATTERN").get(null);
				List<?> registryPatterns = ((Stream<?>) registryClass.getMethod("stream").invoke(bannerRegistry)).toList();
				patternTypes = registryPatterns.toArray();
			} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException |
					 InvocationTargetException | NoSuchMethodException e)
			{
				throw new RuntimeException(e);
			}

		} else {
			try {
				//noinspection UnstableApiUsage,removal
				patternTypes = PatternType.values();
			} catch (Exception ignored) {}
		}
		List<String> patterns = new ArrayList<>();
		for (int i = 0; i < patternTypes.length; i++) {
			Object type = patternTypes[i];
			try {
				patterns.add("[a] [new] "
					+ getKey(type)
					+ " [banner] pattern colo[u]red %*color%"
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			patternCorrelation.put(patterns.size(), type);
		}
		Skript.registerExpression(ExprNewBannerPattern.class, Pattern.class, ExpressionType.SIMPLE,
			patterns.toArray(String[]::new));
	}

	private Object selectedPattern;
	private Color selectedColor;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedPattern = patternCorrelation.get(matchedPattern + 1);
		//noinspection unchecked
		Literal<Color> color = (Literal<Color>) exprs[0];
		if (color == null || color.getSingle() == null) {
			Skript.error("You must provide a valid color.");
			return false;
		}
		selectedColor = color.getSingle();
		return true;
	}

	@Override
	protected Pattern @Nullable [] get(Event event) {
		return new Pattern[]{new Pattern(selectedColor.asDyeColor(), (PatternType) selectedPattern)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Pattern> getReturnType() {
		return Pattern.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new " + selectedPattern + " colored " + selectedColor;
	}

	private static String getKey(Object type) throws Exception {
		if (type instanceof Enum<?> enumType) {
			return enumType.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
		} else {
			Method getKeyMethod = type.getClass().getMethod("getKey");
			Object key = getKeyMethod.invoke(type);
			if (!(key instanceof NamespacedKey namespacedKey))
				return null;
			String keyString = namespacedKey.getKey();
			return keyString.toLowerCase(Locale.ENGLISH).replace('_', ' ');
		}
	}

}
