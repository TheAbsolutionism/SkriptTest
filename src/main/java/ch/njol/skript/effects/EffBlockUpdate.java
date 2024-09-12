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
@Description({
	"Updates the blockstate of a block to cached state or selected block",
	"`force`: Will force the update of the block",
	"`without physics`: Does not send updates to surrounding blocks"
})
@Examples({
	"set {_state} to blockstate of event-block",
	"set event-block to air",
	"wait 1 minute",
	"force update {_state} without physics updates",
	"",
	"force update event-block as stone without physics updates"
})
@Since("INSERT VERSION")
// Originally sourced from SkBee by ShaneBee (https://github.com/ShaneBeee/SkBee/blob/master/src/main/java/com/shanebeestudios/skbee/elements/other/effects/EffBlockstateUpdate.java)
public class EffBlockUpdate extends Effect {

	static {
		Skript.registerEffect(EffBlockUpdate.class,
			"[:force] update %blockstates% [physics:without [neighbo[u]r[ing]|adjacent] [physic[s]] update[s]]",
			"[:force] update %blockstates% [physics:without [neighbo[u]r[ing]|adjacent] [physic[s]] update[s]]");
	}

	private boolean force, physics;
	private Expression<BlockState> blockStates;
	private Expression<Block> blocks;
	private Expression<BlockData> blockData;

	@Override
	@SuppressWarnings("unchecked")
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
	public @NotNull String toString(@Nullable Event event, boolean debug) {
		String result = this.force ? "force " : "";
		if (this.blockStates != null) {
			result += this.blockStates.toString(event, debug);
		} else {
			result += this.blocks.toString(event, debug) + " " + this.blockData.toString(event, debug);
		}
		result += this.physics ? " without neighbour updates" : "";
		return result;
	}

}
