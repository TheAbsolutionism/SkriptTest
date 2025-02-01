package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Persistent")
@Description({
	"Make entities, players, or leaves be persistent.",
	"Persistence of entities is whether they are retained through server restarts.",
	"Persistence of leaves is whether they should decay.",
	"Persistence of players is if the player's playerdata should be saved when they leave the server. "
		+ "Players persistence is reset back to 'true' when they join the server.",
	"Any entity riding an entity that is not persistent, will make the rider not persistent.",
	"By default, all entities are persistent."
})
@Examples({
	"make all entities not persistent",
	"force {_leaves} to persist",
	"",
	"command /kickcheater <cheater: player>:",
		"\tpermission: op",
		"\ttrigger:",
			"\t\tmake {_cheater} not persistent",
			"\t\tkick {_cheater}"
})
@Since("INSERT VERSION")
public class EffPersistent extends Effect {

	static {
		Skript.registerEffect(EffPersistent.class,
			"make %entities/blocks% [:not] persist[ent]",
			"force %entities/blocks% to [:not] persist");
	}

	private Expression<?> source;
	private boolean persist;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		source = exprs[0];
		persist = !parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : source.getArray(event)) {
			if (object instanceof Entity entity) {
				entity.setPersistent(persist);
			} else if (object instanceof Block block && block.getBlockData() instanceof Leaves leaves) {
				leaves.setPersistent(persist);
				block.setBlockData(leaves);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", source);
		if (!persist)
			builder.append("not");
		builder.append("persistent");
		return builder.toString();
	}

}
