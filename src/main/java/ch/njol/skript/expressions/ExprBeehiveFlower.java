package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Beehive Flower Target")
@Description("The flower a beehive has selected to pollinate from.")
@Examples({
	"set the flower target of {_beehive} to block at location(0, 0, 0)",
	"clear the flower target of {_beehive}"
})
@Since("INSERT VERSION")
public class ExprBeehiveFlower extends SimplePropertyExpression<Block, Block> {

	static {
		registerDefault(ExprBeehiveFlower.class, Block.class, "flower target", "blocks");
	}

	@Override
	public @Nullable Block convert(Block block) {
		if (!(block.getState() instanceof Beehive beehive))
			return null;
		Location location = beehive.getFlower();
		return location != null ? location.getBlock() : null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Location.class, Block.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Location location = null;
		if (delta != null) {
			if (delta[0] instanceof Location loc) {
				location = loc;
			} else if (delta[0] instanceof Block block) {
				location = block.getLocation();
			}
		}
		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof Beehive beehive))
				continue;
			beehive.setFlower(location);
			beehive.update(true, false);
		}
	}

	@Override
	public Class<Block> getReturnType() {
		return Block.class;
	}

	@Override
	protected String getPropertyName() {
		return "flower target";
	}

}
