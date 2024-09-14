package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Beacon From Block")
@Description("Gets a beacon type from a block")
@Examples({
	"set {_beacon} to beacon from (block at location(0,0,0))",
	"set primary effect of {_beacon} to haste"
})
@Since("INSERT VERSION")
public class ExprBeacon extends SimpleExpression<Beacon> {

	static {
		Skript.registerExpression(ExprBeacon.class, Beacon.class, ExpressionType.SIMPLE, "beacon (from|of|at) %block%");
	}

	private @UnknownNullability Expression<Block> block;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		block = (Expression<Block>) exprs[0];
		return true;
	}

	@Override
	protected Beacon @Nullable [] get(Event event) {
		if (block.getSingle(event).getState() instanceof Beacon beacon) {
			return new Beacon[]{beacon};
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Beacon> getReturnType() {
		return Beacon.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "beacon from " + block.toString(event, debug);
	}

}
