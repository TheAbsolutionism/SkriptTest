package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("BlockState - Update")
@Description({"Updates the blockstate of a block to cached state or selected block",
	"`force`: Will force the update of the block",
	"`without physics`: Does not send updates to surrounding blocks"})
@Examples({"set {_state} to blockstate of event-block",
	"set event-block to air",
	"wait 1 minute",
	"force update {_state} without physics updates",
	"",
	"force update event-block as stone without physics updates"
})
@Since("INSERT VERSION")
// Ported over from SkBee made by ShaneBee
public class EffBlockUpdate extends Effect {

	static {
		Skript.registerEffect(EffBlockUpdate.class,
			"[:force] update %blockstates% [physics:without (neighbour|physics) updates]",
					"[:force] update %blocks% as %blockdata% [physics:without (neighbour|physics) updates]");
	}

	private boolean force;
	private boolean physics;
	private Expression<BlockState> blockStates;
	private Expression<Block> blocks;
	private Expression<BlockData> blockData;

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.force = parseResult.hasTag("force");
		this.physics = !parseResult.hasTag("physics");
		if (matchedPattern == 0) {
			this.blockStates = (Expression<BlockState>) exprs[0];
		} else {
			this.blocks = (Expression<Block>) exprs[0];
			this.blockData = (Expression<BlockData>) exprs[1];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (this.blockStates != null) {
			for (BlockState blockState : this.blockStates.getArray(event)) {
				blockState.update(this.force, this.physics);
			}
		} else {
			for (Block block : this.blocks.getArray(event)) {
				BlockState state = block.getState();
				state.setBlockData(this.blockData.getSingle(event));
				state.update(this.force, this.physics);
			}
		}
	}

	@Override
	public @NotNull String toString(@Nullable Event event, boolean bool) {
		String result = this.force ? "force " : "";
		if (this.blockStates != null)
			result += this.blockStates.toString(event, bool);
		else
			result += this.blocks.toString(event, bool) + " " + this.blockData.toString(event, bool);
		result += this.physics ? " without neighbour updates" : "";
		return result;
	}
}
