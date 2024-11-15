package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Name("Banner Patterns")
@Description("Banner patterns of a banner.")
@Examples({
	"broadcast banner patterns of {_banneritem}",
	"broadcast 1st banner pattern of block at location(0,0,0)",
	"clear banner patterns of {_banneritem}"
})
@Since("INSERT VERSION")
public class ExprBannerPatterns extends PropertyExpression<Object, Pattern> {

	static {
		Skript.registerExpression(ExprBannerPatterns.class, Pattern.class, ExpressionType.PROPERTY,
			"[the] banner pattern[s] of %itemstacks/itemtypes/slots/blocks%",
			"[the] %integer%[st|nd|rd] [banner] pattern of %itemstacks/itemtypes/slots/blocks%");
	}

	private Expression<?> objects;
	private Expression<Integer> patternNumber;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			objects = exprs[0];
		} else {
			//noinspection unchecked
			patternNumber = (Expression<Integer>) exprs[0];
			objects = exprs[1];
		}
		setExpr(objects);
		return true;
	}

	@Override
	protected Pattern @Nullable [] get(Event event, Object[] source) {
		List<Pattern> patterns = new ArrayList<>();
		Integer placement = patternNumber != null ? patternNumber.getSingle(event) : null;
		for (Object object : objects.getArray(event)) {
			if (object instanceof Block block && block.getState() instanceof Banner banner) {
				if (placement != null) {
					patterns.add(banner.getPattern(placement));
				} else {
					patterns.addAll(banner.getPatterns());
				}
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null || !(itemStack.getItemMeta() instanceof BannerMeta bannerMeta))
					continue;
				if (placement != null) {
					patterns.add(bannerMeta.getPattern(placement));
				} else {
					patterns.addAll(bannerMeta.getPatterns());
				}
			}
		}
		return patterns.toArray(new Pattern[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (patternNumber != null) {
			if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
				return CollectionUtils.array(Pattern.class);
		} else if (mode == ChangeMode.SET || mode == ChangeMode.REMOVE || mode == ChangeMode.ADD || mode == ChangeMode.DELETE) {
			return CollectionUtils.array(Pattern[].class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Pattern[] patterns = null;
		Pattern pattern = null;
		Integer placement = null;
		if (patternNumber != null) {
			placement = patternNumber.getSingle(event);
			if (delta != null && delta[0] != null)
				pattern = (Pattern) delta[0];
		} else if (delta != null) {
			patterns = (Pattern[]) delta;
		}
		Integer finalPlacement = placement;
		Pattern finalPattern = pattern;
		List<Pattern> patternList = patterns != null ? Arrays.stream(patterns).toList() : new ArrayList<>();

		Consumer<BannerMeta> metaChanger = null;
		Consumer<Banner> blockChanger = null;
		switch (mode) {
			case SET -> {
				if (placement != null) {
					metaChanger = bannerMeta -> {
						bannerMeta.setPattern(finalPlacement, finalPattern);
					};
					blockChanger = banner -> {
						banner.setPattern(finalPlacement, finalPattern);
					};
				} else {
					metaChanger = bannerMeta -> {
						bannerMeta.setPatterns(patternList);
					};
					blockChanger = banner -> {
						banner.setPatterns(patternList);
					};
				}
			}
			case DELETE -> {
				if (placement != null) {
					metaChanger = bannerMeta -> {
						bannerMeta.removePattern(finalPlacement);
					};
					blockChanger = banner -> {
						banner.removePattern(finalPlacement);
					};
				} else {
					metaChanger = bannerMeta -> {
						bannerMeta.setPatterns(patternList);
					};
					blockChanger = banner -> {
						banner.setPatterns(patternList);
					};
				}
			}
			case REMOVE -> {
				metaChanger = bannerMeta -> {
					List<Pattern> current = bannerMeta.getPatterns();
					current.removeAll(patternList);
					bannerMeta.setPatterns(current);
				};
				blockChanger = banner -> {
					List<Pattern> current = banner.getPatterns();
					current.removeAll(patternList);
					banner.setPatterns(current);
				};
			}
			case ADD -> {
				metaChanger = bannerMeta -> {
					patternList.forEach(bannerMeta::addPattern);
				};
				blockChanger = banner -> {
					patternList.forEach(banner::addPattern);
				};
			}
		}

		for (Object object : objects.getArray(event)) {
			if (object instanceof Block block && block.getState() instanceof Banner banner) {
				blockChanger.accept(banner);
				banner.update(true, false);
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null || !(itemStack.getItemMeta() instanceof BannerMeta bannerMeta))
					continue;
				metaChanger.accept(bannerMeta);
				itemStack.setItemMeta(bannerMeta);
				if (object instanceof Slot slot) {
					slot.setItem(itemStack);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(bannerMeta);
				} else if (object instanceof ItemStack itemStack1) {
					itemStack1.setItemMeta(bannerMeta);
				}
			}
		}

	}

	@Override
	public Class<Pattern> getReturnType() {
		return Pattern.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (patternNumber != null) {
			builder.append(patternNumber, "banner pattern");
		} else {
			builder.append("banner patterns");
		}
		builder.append(objects);
		return builder.toString();
	}

}
