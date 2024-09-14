package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Beacon;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Beacon Values")
@Description({
	"Values of a Beacon",
	"Secondary effect can only be set to regeneration.",
	"Secondary effect can only be set when beacon tier is maxxed.",
	"You can only change the range on Paper.",
	"You can only set primary and secondary effects to null (clear) on Paper."
})
@Examples({
	"broadcast tier of (beacon from {_block})",
	"set primary effect of (beacon from {_block}) to haste",
	"add 1 to range of (beacon from {_block})"
})
@Since("INSERT VERSION")
public class ExprBeaconValues extends PropertyExpression<Beacon, Object> {

	private static boolean PAPER_LOADED = false;

	static {
		if (Skript.methodExists(Beacon.class, "getTier")) {
			PAPER_LOADED = true;
		}

		Skript.registerExpression(ExprBeaconValues.class, Object.class, ExpressionType.PROPERTY,
			"%-beacon%['s] primary effect",
			"primary effect of %-beacon%",
			"%-beacon%['s] secondary effect",
			"secondary effect of %-beacon%",
			"%-beacon%['s] range",
			"range of %-beacon%",
			"%-beacon%['s] tier",
			"tier of %-beacon%"
		);
	}

	private int pattern;
	private boolean isEffect, isRange, isTier, isPrimary, isSecondary;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<Beacon>) exprs[0]);
		if (matchedPattern <= 3) {
			isEffect = true;
			if (matchedPattern <= 1) {
				isPrimary = true;
			} else {
				isSecondary = true;
			}
		} else if (matchedPattern <= 5) {
			if (!PAPER_LOADED) {
				Skript.error("This can only be used on Paper.");
				return false;
			}
			isRange = true;
		} else {
			isTier = true;
		}
		pattern = matchedPattern;
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event, Beacon[] source) {
		return get(source, new Getter<Object, Beacon>() {
			@Override
			public @Nullable Object get(final Beacon beacon) throws IllegalStateException {
				switch (pattern) {
					case 0,1 -> {
						if (beacon.getPrimaryEffect() != null)
							return beacon.getPrimaryEffect().getType();
					}
					case 2,3 -> {
						if (beacon.getSecondaryEffect() != null) {
							return beacon.getSecondaryEffect().getType();
						}
                    }
					case 4,5 -> {return beacon.getEffectRange();}
					case 6,7 -> {return beacon.getTier();}
                };
				return null;
			}
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || isTier) {
			return null;
		}
		if (isEffect && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)) {
			return null;
		}
		if (isEffect && !PAPER_LOADED && (mode == ChangeMode.RESET || mode == ChangeMode.DELETE)) {
			Skript.error("You can only clear 'primary' and 'secondary' effects of a beacon on Paper.");
			return null;
		}
		return CollectionUtils.array(Object.class);
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final ChangeMode mode) {
		PotionEffectType providedEffect = null;
		double providedRange = 0;
		if (delta != null && delta[0] != null) {
			if (isEffect && delta[0] instanceof PotionEffectType potionEffectType) {
				providedEffect = potionEffectType;
			} else if (isRange) {
				providedRange = (double) ((long) delta[0]);
			}
		}
        assert getExpr() != null;
        Beacon beacon = getExpr().getSingle(event);
		assert beacon != null;
		switch (mode) {
			case ADD -> {
				beacon.setEffectRange(beacon.getEffectRange() + providedRange);
				beacon.update(true);
			}
			case REMOVE -> {
				beacon.setEffectRange(Math.max(beacon.getEffectRange() - providedRange, 0));
				beacon.update(true);
			}
			case DELETE, RESET -> {
				if (isRange) {
					beacon.resetEffectRange();
					beacon.update(true);
				} else if (isEffect) {
					if (isPrimary) {
						beacon.setPrimaryEffect(null);
						beacon.update(true);
					} else if (isSecondary) {
						beacon.setSecondaryEffect(null);
						beacon.update(true);
					}
				}
			}
			case SET -> {
				if (isRange) {
					beacon.setEffectRange(providedRange);
					beacon.update(true);
				} else if (isEffect) {
					if (isPrimary) {
						beacon.setPrimaryEffect(providedEffect);
						beacon.update(true);
					} else if (isSecondary) {
						beacon.setSecondaryEffect(providedEffect);
						beacon.update(true);
					}
				}
			}
		}
	}


	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String result = "";
		if (isEffect) {
			if (isPrimary) {
				result = "primary effect";
			} else {
				result = "secondary effect";
			}
		} else if (isRange) {
			result = "range";
		} else if (isTier) {
			result = "tier";
		}
		return result + " of " + getExpr().toString(event, debug);
	}
}
