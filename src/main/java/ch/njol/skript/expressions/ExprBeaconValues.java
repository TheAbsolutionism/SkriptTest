package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import io.papermc.paper.event.block.BeaconActivatedEvent;
import io.papermc.paper.event.block.BeaconDeactivatedEvent;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import static ch.njol.util.Math2.floor;

@Name("Beacon Effects")
@Description({
	"The active effects of the beacon.",
	"The secondary effect can only be set to regeneration and it can only be set when the beacon has achieved the highest tier.",
	"You can only change the range on Paper."
})
@Examples({
	"broadcast tier of {_block}",
	"set primary beacon effect of {_block} to haste",
	"add 1 to range of {_block}"
})
@Since("INSERT VERSION")
public class ExprBeaconValues extends PropertyExpression<Block, Object> {

	enum BeaconValues {
		PRIMARY("primary beacon effect"),
		SECONDARY("secondary beacon effect"),
		RANGE("[beacon] range"),
		TIER("[beacon] tier");

		private final String pattern;

		BeaconValues(String pattern) {
			this.pattern = pattern;
		}
	}

	private static final boolean PAPER_EVENTS = Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent");
	private static final boolean PAPER_RANGE = Skript.methodExists(Beacon.class, "getEffectRange");
	private static BeaconValues[] beaconValues = BeaconValues.values();

	static {
		int size = beaconValues.length;
		String[] patterns = new String[size * 2];
		for (BeaconValues value : beaconValues) {
			patterns[2 * value.ordinal()] = "%blocks%['s] " + value.pattern;
			patterns[2 * value.ordinal() + 1] = value.pattern + " [of %blocks%]";
		}

		Skript.registerExpression(ExprBeaconValues.class, Object.class, ExpressionType.PROPERTY, patterns);
	}

	private BeaconValues valueType;
	private boolean isEffect;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		valueType = beaconValues[(int) floor(matchedPattern/2)];
		if (valueType == BeaconValues.RANGE && !PAPER_RANGE) {
			Skript.error(valueType.pattern + "can only be used on Paper.");
			return false;
		}
		if (exprs[0] != null) {
			setExpr((Expression<Block>) exprs[0]);
		} else {
			if (!PAPER_EVENTS)
				return false;
			if (!getParser().isCurrentEvent(PlayerChangeBeaconEffectEvent.class, BeaconEffectEvent.class, BeaconActivatedEvent.class, BeaconDeactivatedEvent.class)) {
				Skript.error("There is no beacon in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			setExpr(new EventValueExpression<>(Block.class));
		}
		if (valueType == BeaconValues.PRIMARY || valueType == BeaconValues.SECONDARY)
			isEffect = true;

		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event, Block[] source) {
		return get(source, new Getter<Object, Block>() {
			@Override
			public @Nullable Object get(Block block) {
				Beacon beacon  = (Beacon) block.getState();
				switch (valueType) {
					case PRIMARY -> {
						if (PAPER_EVENTS && event instanceof PlayerChangeBeaconEffectEvent changeEvent) {
							return changeEvent.getPrimary();
						} else if (beacon.getPrimaryEffect() != null) {
							return beacon.getPrimaryEffect().getType();
						}
					}
					case SECONDARY-> {
						if (PAPER_EVENTS && event instanceof PlayerChangeBeaconEffectEvent changeEvent) {
							return changeEvent.getSecondary();
						} else if (beacon.getSecondaryEffect() != null) {
							return beacon.getSecondaryEffect().getType();
						}
					}
					case RANGE -> {return beacon.getEffectRange();}
					case TIER -> {return beacon.getTier();}
				};
				return null;
			}
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || valueType == BeaconValues.TIER) {
			return null;
		}
		if (isEffect && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)) {
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
			} else if (valueType == BeaconValues.RANGE) {
				providedRange = (double) ((long) delta[0]);
			}
		}
		switch (valueType) {
			case RANGE -> {changeRange(event, providedRange, mode);}
			case PRIMARY -> {changePrimary(event, providedEffect, mode);}
			case SECONDARY -> {changeSecondary(event, providedEffect, mode);}
		}
	}

	private void changeRange(Event event, double providedRange, ChangeMode mode) {
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
				for (Block block : getExpr().getArray(event)) {
					Beacon beacon = (Beacon) block.getState();
					beacon.resetEffectRange();
					beacon.update(true);
				}
			}
			case SET -> {
				for (Block block : getExpr().getArray(event)) {
					Beacon beacon = (Beacon) block.getState();
					beacon.setEffectRange(providedRange);
					beacon.update(true);
				}
			}
		}
	}

	private void changePrimary(Event event, PotionEffectType providedEffect, ChangeMode mode) {
		switch (mode) {
			case SET -> {
				for (Block block : getExpr().getArray(event)) {
					Beacon beacon = (Beacon) block.getState();
					beacon.setPrimaryEffect(providedEffect);
					beacon.update(true);
				}
			}
			case DELETE, RESET -> {
				for (Block block : getExpr().getArray(event)) {
					Beacon beacon = (Beacon) block.getState();
					beacon.setPrimaryEffect(null);
					beacon.update(true);
				}
			}
		}
	}

	private void changeSecondary(Event event, PotionEffectType providedEffect, ChangeMode mode) {
		switch (mode) {
			case SET -> {
				for (Block block : getExpr().getArray(event)) {
					Beacon beacon = (Beacon) block.getState();
					beacon.setSecondaryEffect(providedEffect);
					beacon.update(true);
				}
			}
			case DELETE, RESET -> {
				for (Block block : getExpr().getArray(event)) {
					Beacon beacon = (Beacon) block.getState();
					beacon.setSecondaryEffect(null);
					beacon.update(true);
				}
			}
		}
	}


	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (valueType) {
			case PRIMARY -> "primary beacon effect";
			case SECONDARY -> "secondary beacon effect";
			case RANGE -> "beacon range";
			case TIER -> "beacon tier";
		} + " of " + getExpr().toString(event, debug);
	}

}
