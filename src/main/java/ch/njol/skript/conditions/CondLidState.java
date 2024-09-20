package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Lid is Open")
@Description("Check to see if the lid of blocks are open or closed.")
@Examples({
	"if the lid of {_chest} is closed:",
		"\topen the lid of {_block}"
})
@Since("INSERT VERSION")
public class CondLidState extends PropertyCondition<Block> {

	static {
		Skript.registerCondition(CondLidState.class, ConditionType.PROPERTY,
			"[the] lid [state] of %blocks% (is|are) open[ed]",
			"[the] lid [state] of %blocks% (isn't|aren't) open[ed]",
			"[the] lid [state] of %blocks% (is|are) close[d]",
			"[the] lid [state] of %blocks% (isn't|aren't) close[d]"
		);


	}

	private boolean checkOpen;
	private Expression<Block> blocks;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkOpen = matchedPattern <= 1;
		blocks = (Expression<Block>) exprs[0];
		setExpr((Expression<Block>) exprs[0]);
		if (matchedPattern == 1 || matchedPattern == 3)
			setNegated(true);
		return true;
	}

	@Override
	public boolean check(Block block) {
		if (!(block.getState() instanceof Lidded lidBlock))
			return false;
		return lidBlock.isOpen() == checkOpen;
	}

	@Override
	protected String getPropertyName() {
		return (checkOpen ? "opened" : "closed") + "lid state";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (checkOpen ? "opened" : "closed") + "lid of " + blocks.toString(event, debug);
	}

}
