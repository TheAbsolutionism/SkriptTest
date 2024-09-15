package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Effects")
@Description({
	"The active effects of the beacon.",
	"The secondary effect can only be set to regeneration and it can only be set when the beacon has achieved the highest tier.",
	"You can only change the range and clear the primary and secondary effects on Paper."
})
@Examples({
	"broadcast tier of {_block}",
	"set primary effect of {_block} to haste",
	"add 1 to range of {_block}"
})
@Since("INSERT VERSION")
public class ExprBeaconValues extends PropertyExpression<Block, Object> {

	private static boolean PAPER_METHOD = false;

	static {
		if (Skript.methodExists(Beacon.class, "getTier") && Skript.methodExists(Beacon.class, "setPrimaryEffect", PotionEffectType.class)) {
			PAPER_METHOD = true;
		}

		Skript.registerExpression(ExprBeaconValues.class, Object.class, ExpressionType.PROPERTY,
			"%blocks%['s] primary effect",
			"primary effect of %blocks%",
			"%blocks%['s] secondary effect",
			"secondary effect of %blocks%",
			"%blocks%['s] range",
			"range of %blocks%",
			"%blocks%['s] tier",
			"tier of %blocks%"
		);
	}

	private int pattern;
	private boolean isEffect, isRange, isTier, isPrimary, isSecondary;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Block>) exprs[0]);
		if (matchedPattern <= 3) {
			isEffect = true;
			if (matchedPattern <= 1) {
				isPrimary = true;
			} else {
				isSecondary = true;
			}
		} else if (matchedPattern <= 5) {
			if (!PAPER_METHOD) {
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
	protected Object @Nullable [] get(Event event, Block[] source) {
		return get(source, new Getter<Object, Block>() {
			@Override
			public @Nullable Object get(Block block) throws IllegalStateException {
				Beacon beacon  = (Beacon) block.getState();
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
		if (isEffect && !PAPER_METHOD && (mode == ChangeMode.RESET || mode == ChangeMode.DELETE)) {
			Skript.error("You can only clear 'primary' and 'secondary' effects of a beacon on Paper.");
			return null;
		}
		return CollectionUtils.array(Object.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		PotionEffectType providedEffect = null;
		double providedRange = 0;
		if (delta != null && delta[0] != null) {
			if (isEffect && delta[0] instanceof PotionEffectType potionEffectType) {
				providedEffect = potionEffectType;
			} else if (isRange) {
				providedRange = (double) ((long) delta[0]);
			}
		}
		switch (mode) {
			case ADD -> {
				for (Block block : getExpr().getArray(event)) {
					Beacon beacon = (Beacon) block.getState();
					beacon.setEffectRange(beacon.getEffectRange() + providedRange);
					beacon.update(true);
				}
			}
			case REMOVE -> {
				for (Block block : getExpr().getArray(event)) {
					Beacon beacon = (Beacon) block.getState();
					beacon.setEffectRange(Math.max(beacon.getEffectRange() - providedRange, 0));
					beacon.update(true);
				}
			}
			case DELETE, RESET -> {
				if (isRange) {
					for (Block block : getExpr().getArray(event)) {
						Beacon beacon = (Beacon) block.getState();
						beacon.resetEffectRange();
						beacon.update(true);
					}
				} else if (isEffect) {
					if (isPrimary) {
						for (Block block : getExpr().getArray(event)) {
							Beacon beacon = (Beacon) block.getState();
							beacon.setPrimaryEffect(null);
							beacon.update(true);
						}
					} else if (isSecondary) {
						for (Block block : getExpr().getArray(event)) {
							Beacon beacon = (Beacon) block.getState();
							beacon.setSecondaryEffect(null);
							beacon.update(true);
						}
					}
				}
			}
			case SET -> {
				if (isRange) {
					for (Block block : getExpr().getArray(event)) {
						Beacon beacon = (Beacon) block.getState();
						beacon.setEffectRange(providedRange);
						beacon.update(true);
					}
				} else if (isEffect) {
					if (isPrimary) {
						for (Block block : getExpr().getArray(event)) {
							Beacon beacon = (Beacon) block.getState();
							beacon.setPrimaryEffect(providedEffect);
							beacon.update(true);
						}
					} else if (isSecondary) {
						for (Block block : getExpr().getArray(event)) {
							Beacon beacon = (Beacon) block.getState();
							beacon.setSecondaryEffect(providedEffect);
							beacon.update(true);
						}
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
