package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("BlockState")
@Description({
	"Gets the blockstate of a block.",
	"Can be used to update block back to this state."
})
@Examples({
	"set {_state} to blockstate of event-block",
	"set event-block to air",
	"wait 1 minute",
	"force update {_state} without physics updates"
})
@Since("INSERT VERSION")
// Ported over from SkBee made by ShaneBee (Credits go to him)
public class ExprBlockState extends SimplePropertyExpression<Block, BlockState> {
	static {
		register(ExprBlockState.class, BlockState.class, "(captured|block)[ ]state[s]", "blocks");
	}

	@Override
	public @Nullable BlockState convert(Block block) {
		return block.getState();
	}

	@Override
	public @NotNull Class<? extends BlockState> getReturnType() {
		return BlockState.class;
	}

	@Override
	protected @NotNull String getPropertyName() {
		return "block state";
	}

}
