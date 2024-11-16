package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Name("Banner Pattern Item")
@Description({
	"Gets the item from a banner pattern type.",
	"NOTE: Not all banner patterns have an item. Currently there are:",
	"<ul>",
	"<li>creeper/creeper charged</li>",
	"<li>bricks/field masoned</li>",
	"<li>piglin/snout</li>",
	"<li>mojang/thing</li>",
	"<li>border/bordure indented</li>",
	"<li>flow, flower, globe, guster, skull</li>",
	"</ul>"
})
@Examples({
	"set {_item} to creeper charged banner pattern item",
	"set {_item} to snout banner pattern item",
	"set {_item} to thing banner pattern item"
})
@Since("INSERT VERSION")
public class ExprBannerItem extends SimpleExpression<ItemType> {

	static {
		Skript.registerExpression(ExprBannerItem.class, ItemType.class, ExpressionType.SIMPLE,
			"%bannerpatterns% item[s]");
	}

	private Expression<PatternType> patternTypes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		patternTypes = (Expression<PatternType>) exprs[0];
		return true;
	}

	@Override
	protected ItemType @Nullable [] get(Event event) {
		List<ItemType> itemTypes = new ArrayList<>();
		for (PatternType type : patternTypes.getArray(event)) {
			Material material = getMaterial(type);
			if (material == null)
				continue;
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

	private Material getMaterial(PatternType patternType) {
		String key = patternType.key().value().toUpperCase(Locale.ENGLISH);
		Material material = Material.getMaterial(key +  "_BANNER_PATTERN");
		return material != null ? material : checkAlias(patternType);
	}

	private Material checkAlias(PatternType patternType) {
		if (patternType == PatternType.BRICKS) {
			return Material.FIELD_MASONED_BANNER_PATTERN;
		} else if (patternType == PatternType.BORDER) {
			return Material.BORDURE_INDENTED_BANNER_PATTERN;
		}
		return null;
	}

}
