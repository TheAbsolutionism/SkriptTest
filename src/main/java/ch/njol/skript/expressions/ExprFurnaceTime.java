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
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static ch.njol.util.Math2.floor;

@Name("Furnace Times")
@Description({
	"The cook time, total cook time, and burn time of a furnace. Can be changed.",
	"<ul>",
	"<li>cook time: The amount of time an item has been smelting for.</li>",
	"<li>total cook time: The amount of time required to finish smelting an item.</li>",
	"<li>burn time: The amount of time left for the current fuel until consumption of another fuel item.</li>",
	"</ul>"
})
@Examples({
	"set the cooking time of {_block} to 10",
	"set the total cooking time of {_block} to 50",
	"set the burning time of {_block} to 100",
	"on smelt:",
		"\tif the fuel slot is charcoal:",
			"\t\tadd 5 seconds to the burn time"
})
@Since("INSERT VERSION")
public class ExprFurnaceTime extends PropertyExpression<Block, Timespan> {

	enum FurnaceExpressions {
		COOKTIME("cook[ing] time", "cook time"),
		TOTALCOOKTIME("total cook[ing] time", "total cook time"),
		BURNTIME("fuel burn[ing] time", "burn time");

		private String name, toString;

		FurnaceExpressions(String name, String toString) {
			this.name = name;
			this.toString = toString;
		}

	}

	private static final FurnaceExpressions[] furnaceExprs = FurnaceExpressions.values();
	
	static {

		int size = furnaceExprs.length;
		String[] patterns = new String[size * 2];
		for (FurnaceExpressions value : furnaceExprs) {
			patterns[2 * value.ordinal()] = "[the] [furnace] " + value.name + " of %blocks%";
			patterns[2 * value.ordinal() + 1] = "%blocks%['s]" + value.name;
		}

		Skript.registerExpression(ExprFurnaceTime.class, Timespan.class, ExpressionType.PROPERTY, patterns);
	}

	private FurnaceExpressions type;
	private boolean explicitlyBlock;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = furnaceExprs[(int) floor(matchedPattern / 2)];
		if (exprs[0] != null) {
			explicitlyBlock = true;
			setExpr((Expression<Block>) exprs[0]);
		} else {
			if (!getParser().isCurrentEvent(FurnaceBurnEvent.class, FurnaceStartSmeltEvent.class, FurnaceExtractEvent.class, FurnaceSmeltEvent.class)) {
				Skript.error("There's no furnace in a " + getParser().getCurrentEventName() + " event.");
				return false;
			}
			setExpr(new EventValueExpression<>(Block.class));
		}
		return true;
	}

	@Override
	protected Timespan @Nullable [] get(Event event, Block[] source) {
		return get(source, new Getter<Timespan, Block>() {
			@Override
			public @Nullable Timespan get(Block block) {
				Furnace furnace = (block != null && block.getState() instanceof Furnace furnaceCheck) ? furnaceCheck : null;
				switch (type) {
					case COOKTIME -> {
						return new Timespan(Timespan.TimePeriod.TICK, (int) furnace.getCookTime());
					}
					case TOTALCOOKTIME -> {
						if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
							return new Timespan(Timespan.TimePeriod.TICK, startEvent.getTotalCookTime());
						} else {
							return new Timespan(Timespan.TimePeriod.TICK, furnace.getCookTimeTotal());
						}
					}
					case BURNTIME -> {
						if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
							return new Timespan(Timespan.TimePeriod.TICK, burnEvent.getBurnTime());
						} else {
							return new Timespan(Timespan.TimePeriod.TICK, (int) furnace.getBurnTime());
						}
					}
				}
				return null;
			}
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;

		return CollectionUtils.array(Timespan.class);
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedTime = 0;
		if (delta != null && delta[0] instanceof Timespan span)
			providedTime = (int) span.get(Timespan.TimePeriod.TICK);

		switch (type) {
			case COOKTIME -> changeCookTime(event, providedTime, mode);
			case TOTALCOOKTIME -> changeTotalCookTime(event, providedTime, mode);
			case BURNTIME -> changeBurnTime(event, providedTime, mode);
		}
	}

	private void changeFurnaces(Event event, Consumer<Furnace> changer) {
		for (Block block : getExpr().getArray(event)) {
			Furnace furnace = (Furnace) block.getState();
			changer.accept(furnace);
			furnace.update(true);
		}
	}

	private void changeCookTime(Event event, int providedTime, ChangeMode mode) {
		switch (mode) {
			case SET -> changeFurnaces(event, furnace -> furnace.setCookTime((short) providedTime));
			case DELETE -> changeFurnaces(event, furnace -> furnace.setCookTime((short) 0));
			case REMOVE -> changeFurnaces(event, furnace -> furnace.setCookTime((short) Math2.fit(0, furnace.getCookTime() - providedTime, Integer.MAX_VALUE)));
			case ADD -> changeFurnaces(event, furnace -> furnace.setCookTime((short) Math2.fit(0, furnace.getCookTime() + providedTime, Integer.MAX_VALUE)));
		}
	}

	private void changeTotalCookTime(Event event, int providedTime, ChangeMode mode) {
		switch (mode) {
			case SET -> {
				if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
					startEvent.setTotalCookTime(providedTime);
				} else {
					changeFurnaces(event, furnace -> furnace.setCookTimeTotal(providedTime));
				}
			}
			case DELETE -> {
				if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
					startEvent.setTotalCookTime(0);
				} else {
					changeFurnaces(event, furnace -> furnace.setCookTimeTotal(0));
				}
			}
			case REMOVE -> {
				if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
					startEvent.setTotalCookTime(Math.min(startEvent.getTotalCookTime() - providedTime, 0));
				} else {
					changeFurnaces(event, furnace -> furnace.setCookTimeTotal(Math2.fit(0, furnace.getCookTimeTotal() - providedTime, Integer.MAX_VALUE)));
				}
			}
			case ADD -> {
				if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
					startEvent.setTotalCookTime(startEvent.getTotalCookTime() + providedTime);
				} else {
					changeFurnaces(event, furnace -> furnace.setCookTimeTotal(Math2.fit(0, furnace.getCookTimeTotal() + providedTime, Integer.MAX_VALUE)));
				}
			}
		}
	}

	private void changeBurnTime(Event event, int providedTime, ChangeMode mode) {
		switch (mode) {
			case SET -> {
				if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
					burnEvent.setBurnTime(providedTime);
				} else {
					changeFurnaces(event, furnace -> furnace.setBurnTime((short) providedTime));
				}
			}
			case DELETE -> {
				if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
					burnEvent.setBurnTime(0);
				} else {
					changeFurnaces(event, furnace -> furnace.setBurnTime((short) 0));
				}
			}
			case REMOVE -> {
				if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
					burnEvent.setBurnTime(Math.min(burnEvent.getBurnTime() - providedTime, 0));
				} else {
					changeFurnaces(event, furnace -> furnace.setBurnTime((short) Math2.fit(0, furnace.getBurnTime() - providedTime, Integer.MAX_VALUE)));
				}
			}
			case ADD -> {
				if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
					burnEvent.setBurnTime(burnEvent.getBurnTime() + providedTime);
				} else {
					changeFurnaces(event, furnace -> furnace.setBurnTime((short) Math2.fit(0, furnace.getBurnTime() + providedTime, Integer.MAX_VALUE)));
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return type.toString + " of "  + getExpr().toString(event, debug);
	}
	
}
