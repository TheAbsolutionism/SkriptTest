/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.jetbrains.annotations.Nullable;

import static ch.njol.util.Math2.floor;

@Name("Furnace Times")
@Description({
	"You can use these expressions to change the time of a furnace.",
	"<ul>",
	"<li>cook time: The amount of time an item has been smelting for.</li>",
	"<li>total cook time: The amount of time an item is required to smelt for until done.</li>",
	"<li>burn time: The amount of time left of the fuel.</li>",
	"</ul>"
})
@Examples({
	"set the cooking time of {_block} to 10",
	"set the total cooking time of {_block} to 50",
	"set the burning time of {_block} to 100"
})
@Since("INSERT VERSION")
public class ExprFurnaceTime extends PropertyExpression<Block, Timespan> {

	enum FurnaceExpressions {
		COOKTIME("cook[ing] time"),
		TOTALCOOKTIME("total cook[ing] time"),
		BURNTIME("burn[ing] time");

		private String name;

		FurnaceExpressions(String name) {
			this.name = name;
		}

	}


	/*
		COOKTIME
			Through Furnace block
			Get, Set, Remove, Add, Clear
		TOTALCOOKTIME
			Thorugh Furnace Block, Through FurnaceStartSmeltEvent
			Get, Set, Remove, Add, Clear
		BURNTIME
			Through Furnace block, Through FurnaceBurnEvent
			Get, Set, Add, Remove, Clear

		 */

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
		Skript.adminBroadcast("Changing: " + type + "| Mode: " + mode);
		if (delta != null && delta[0] instanceof Timespan span)
			providedTime = (int) span.get(Timespan.TimePeriod.TICK);

		switch (mode) {
			case SET -> {
				if (type == FurnaceExpressions.COOKTIME) {
					for (Block block : getExpr().getArray(event)) {
						Furnace furnace = (Furnace) block.getState();
						furnace.setCookTime((short) providedTime);
						furnace.update(true);
					}
				} else if (type == FurnaceExpressions.TOTALCOOKTIME) {
					if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
						startEvent.setTotalCookTime(providedTime);
					} else {
						for (Block block : getExpr().getArray(event)) {
							Furnace furnace = (Furnace) block.getState();
							furnace.setCookTimeTotal(providedTime);
							furnace.update(true);
						}
					}
				} else if (type == FurnaceExpressions.BURNTIME) {
					if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
						burnEvent.setBurnTime(providedTime);
					} else {
						for (Block block : getExpr().getArray(event)) {
							Skript.adminBroadcast("----- Setting Burn Time: " + providedTime);
							Furnace furnace = (Furnace) block.getState();
							furnace.setBurnTime((short) providedTime);
							furnace.update(true);
						}
					}
				}
			}
			case DELETE -> {
				if (type == FurnaceExpressions.COOKTIME) {
					for (Block block : getExpr().getArray(event)) {
						Furnace furnace = (Furnace) block.getState();
						furnace.setCookTime((short) 0);
						furnace.update(true);
					}
				} else if (type == FurnaceExpressions.TOTALCOOKTIME) {
					if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
						startEvent.setTotalCookTime(0);
					} else {
						for (Block block : getExpr().getArray(event)) {
							Furnace furnace = (Furnace) block.getState();
							furnace.setCookTimeTotal(0);
							furnace.update(true);
						}
					}
				} else if (type == FurnaceExpressions.BURNTIME) {
					if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
						burnEvent.setBurnTime(0);
					} else {
						for (Block block : getExpr().getArray(event)) {
							Furnace furnace = (Furnace) block.getState();
							furnace.setBurnTime((short) 0);
							furnace.update(true);
						}
					}
				}
			}
			case REMOVE -> {
				if (type == FurnaceExpressions.COOKTIME) {
					for (Block block : getExpr().getArray(event)) {
						Furnace furnace = (Furnace) block.getState();
						furnace.setCookTime((short) Math.min(furnace.getCookTime() - providedTime, 0));
						furnace.update(true);
					}
				} else if (type == FurnaceExpressions.TOTALCOOKTIME) {
					if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
						startEvent.setTotalCookTime(Math.min(startEvent.getTotalCookTime() - providedTime, 0));
					} else {
						for (Block block : getExpr().getArray(event)) {
							Furnace furnace = (Furnace) block.getState();
							furnace.setCookTimeTotal(Math.min(furnace.getCookTimeTotal() - providedTime, 0));
							furnace.update(true);
						}
					}
				} else if (type == FurnaceExpressions.BURNTIME) {
					if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
						burnEvent.setBurnTime(Math.min(burnEvent.getBurnTime() - providedTime, 0));
					} else {
						for (Block block : getExpr().getArray(event)) {
							Furnace furnace = (Furnace) block.getState();
							furnace.setBurnTime((short) Math.min(furnace.getBurnTime() - providedTime, 0));
							furnace.update(true);
						}
					}
				}
			}
			case ADD -> {
				if (type == FurnaceExpressions.COOKTIME) {
					for (Block block : getExpr().getArray(event)) {
						Furnace furnace = (Furnace) block.getState();
						furnace.setCookTime((short) (furnace.getCookTime() + providedTime));
						furnace.update(true);
					}
				} else if (type == FurnaceExpressions.TOTALCOOKTIME) {
					if (!explicitlyBlock && event instanceof FurnaceStartSmeltEvent startEvent) {
						startEvent.setTotalCookTime(startEvent.getTotalCookTime() + providedTime);
					} else {
						for (Block block : getExpr().getArray(event)) {
							Furnace furnace = (Furnace) block.getState();
							furnace.setCookTimeTotal(furnace.getCookTimeTotal() + providedTime);
							furnace.update(true);
						}
					}
				} else if (type == FurnaceExpressions.BURNTIME) {
					if (!explicitlyBlock && event instanceof FurnaceBurnEvent burnEvent) {
						burnEvent.setBurnTime(burnEvent.getBurnTime() + providedTime);
					} else {
						for (Block block : getExpr().getArray(event)) {
							Furnace furnace = (Furnace) block.getState();
							furnace.setBurnTime((short) (furnace.getBurnTime() + providedTime));
							furnace.update(true);
						}
					}
				}
			}
		}

	}

	@Override
	public boolean isSingle() {
		if (getExpr() != null) {
			return getExpr().isSingle();
		}
        return true;
	}

	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String result = "";
		switch (type)  {
			case COOKTIME -> {result = "cook time";}
			case TOTALCOOKTIME -> {result = "total cook time";}
			case BURNTIME -> {result = "burn time";}
		}
		if (!explicitlyBlock) {
			result += " of " + event.toString();
		} else if (explicitlyBlock) {
			result += " of " + getExpr().toString(event, debug);
		}
		return result;
	}
	
}
