package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Is Full")
@Description("Checks to see if the stored entities of an entity block storage (i.e beehive) is full.")
@Examples({
	"if the stored entities of {_beehive} is full:",
		"\trelease the stored entities of {_beehive}"
})
@Since("INSERT VERSION")
public class CondEntityStorageIsFull extends Condition {

	static {
		Skript.registerCondition(CondEntityStorageIsFull.class, ConditionType.PROPERTY,
			"[the] stored entities of %blocks% (is|are) full",
			"[the] stored entities of %blocks% (isn't|is not|aren't|are not) full");
	}

	private Expression<Block> blocks;
	private boolean checkFull;

	@Override
	public boolean init(Expression<?>[] exrps, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkFull = matchedPattern % 2 == 0;
		//noinspection unchecked
		blocks = (Expression<Block>) exrps[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		return blocks.check(event, block -> {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				return false;
			return blockStorage.isFull() == checkFull;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the stored entities of", blocks);
		if (blocks.isSingle()) {
			builder.append("is");
		} else {
			builder.append("are");
		}
		if (!checkFull)
			builder.append("not");
		builder.append("full");
		return builder.toString();
	}

}
