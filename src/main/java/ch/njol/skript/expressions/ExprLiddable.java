package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.EnderChest;
import org.bukkit.block.Barrel;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Lid State")
@Description("Set the lid state of a chest, ender chest, barrel and shulker box.")
@Examples({
	"if the lid state of {_chest} is false:",
	"\tset the lid state of {_chest} to true"
})
@Since("INSERT VERSION")
public class ExprLiddable extends PropertyExpression<Block, Boolean> {

	static {
		Skript.registerExpression(ExprLiddable.class, Boolean.class, ExpressionType.PROPERTY, "[the] lid [state] of %blocks%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Block>) exprs[0]);
		return true;
	}

	@Override
	protected Boolean @Nullable [] get(Event event, Block[] source) {
		List<Boolean> booleanList = new ArrayList<>();
		for (Block block : getExpr().getArray(event)) {
			BlockState state = block.getState();
			if (state instanceof Chest chest) {
				booleanList.add(chest.isOpen());
			} else if (state instanceof EnderChest enderChest) {
				booleanList.add(enderChest.isOpen());
			} else if (state instanceof Barrel barrel) {
				booleanList.add(barrel.isOpen());
			} else if (state instanceof ShulkerBox shulkerBox) {
				booleanList.add(shulkerBox.isOpen());
			}
		}
		return booleanList.toArray(new Boolean[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Boolean.class);
		return null;
	};

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		boolean toOpen = (boolean) delta[0];
		if (toOpen) {
			for (Block block : getExpr().getArray(event)) {
				BlockState state = block.getState();
				if (state instanceof Chest chest) {
					chest.open();
				} else if (state instanceof EnderChest enderChest) {
					enderChest.open();
				} else if (state instanceof Barrel barrel) {
					barrel.open();
				} else if (state instanceof ShulkerBox shulkerBox) {
					shulkerBox.open();
				}
			}
		} else {
			for (Block block : getExpr().getArray(event)) {
				BlockState state = block.getState();
				if (state instanceof Chest chest) {
					chest.close();
				} else if (state instanceof EnderChest enderChest) {
					enderChest.close();
				} else if (state instanceof Barrel barrel) {
					barrel.close();
				} else if (state instanceof ShulkerBox shulkerBox) {
					shulkerBox.close();
				}
			}
		}
	}

	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "lid state of " + getExpr().toString(event, debug);
	}

}
