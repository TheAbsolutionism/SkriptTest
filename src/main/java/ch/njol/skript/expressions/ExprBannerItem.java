package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Name("Banner Pattern Item")
@Description({
	"Gets the item from a banner pattern type.",
	"NOTE: Not all banner pattern types have an item.",
})
@Examples({
	"set {_item} to creeper charged banner pattern item",
	"set {_item} to snout banner pattern item",
	"set {_item} to thing banner pattern item"
})
@Since("INSERT VERSION")
public class ExprBannerItem extends SimpleExpression<ItemType> {

	private static Map<Object, Material> bannerMaterials = new HashMap<>();

	static {
		Registry<PatternType> patternRegistry = Bukkit.getRegistry(PatternType.class);
		Object[] bannerPatterns;
		if (patternRegistry != null) {
			bannerPatterns  = patternRegistry.stream().toArray();
		} else {
			try {
				Class<?> patternClass = Class.forName("org.bukkit.block.banner.PatternType");
				if (patternClass.isEnum()) {
					//noinspection unchecked,rawtypes
					Class<? extends Enum> enumClass = (Class<? extends Enum>) patternClass;
					bannerPatterns = enumClass.getEnumConstants();
				} else {
					throw new IllegalStateException("PatternType is neither an enum nor a valid registry.");
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		if (bannerPatterns != null) {
			for (Object object : bannerPatterns) {
				Material material = getMaterial(object);
				if (material != null)
					bannerMaterials.put(object, material);
			}
		}

		Skript.registerExpression(ExprBannerItem.class, ItemType.class, ExpressionType.COMBINED,
			"[a[n]] %*bannerpatterntypes% item[s]");
	}

	private Literal<PatternType> patternTypes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		patternTypes = ((Literal<PatternType>) exprs[0]);
		return true;
	}

	@Override
	protected ItemType @Nullable [] get(Event event) {
		List<ItemType> itemTypes = new ArrayList<>();
		for (PatternType type : patternTypes.getArray()) {
			if (!bannerMaterials.containsKey(type))
				continue;
			Material material = bannerMaterials.get(type);
			ItemType itemType = new ItemType(material);
			itemTypes.add(itemType);
		}
		return itemTypes.toArray(new ItemType[0]);
	}

	@Override
	public boolean isSingle() {
		return patternTypes.isSingle();
	}

	@Override
	public Class<ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return patternTypes.toString(event, debug) + " items";
	}

	private static @Nullable Material getMaterial(Object object) {
		if (!(object instanceof PatternType patternType))
			return null;
		NamespacedKey namespacedKey = null;
		try {
			namespacedKey = (NamespacedKey) PatternType.class.getMethod("key").invoke(patternType);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
		if (namespacedKey == null)
			return null;
        String key = namespacedKey.getKey().toUpperCase(Locale.ENGLISH);
		Material material = Material.getMaterial(key +  "_BANNER_PATTERN");
		return material != null ? material : checkAlias(patternType);
	}

	private static @Nullable Material checkAlias(PatternType patternType) {
		if (patternType == PatternType.BRICKS && Material.getMaterial("FIELD_MASONED_BANNER_PATTERN") != null) {
			return Material.FIELD_MASONED_BANNER_PATTERN;
		} else if (patternType == PatternType.BORDER && Material.getMaterial("BORDURE_INDENTED_BANNER_PATTER") != null) {
			return Material.BORDURE_INDENTED_BANNER_PATTERN;
		}
		return null;
	}

}
