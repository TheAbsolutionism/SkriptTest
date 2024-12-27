package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityBlockStorageUtils.EntityBlockStorageType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Clear Entity Storage")
@Description({
	"Clear the stored entities of an entity block storage (i.e. beehive).",
	"Using a specific block storage type will restrict the blocks provided to match the type.",
})
@Examples("clear the beehive storage of {_beehive}")
@Since("INSERT VERSION")
public class EffClearEntityStorage extends Effect {

	private static final EntityBlockStorageType[] ENTITY_BLOCK_STORAGE_TYPES = EntityBlockStorageType.values();

	static {
		String[] patterns = new String[ENTITY_BLOCK_STORAGE_TYPES.length];
		for (EntityBlockStorageType type : ENTITY_BLOCK_STORAGE_TYPES) {
			patterns[type.ordinal()] = "(clear|empty) [the] " + type.getCodeName() + " [stored entities] of %blocks%";
		}
		Skript.registerEffect(EffClearEntityStorage.class, patterns);
	}

	private EntityBlockStorageType storageType;
	private Expression<Block> blocks;
	private boolean clear;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		storageType = ENTITY_BLOCK_STORAGE_TYPES[matchedPattern];
		//noinspection unchecked
		blocks = (Expression<Block>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;
			if (!storageType.isSuperType() && !storageType.getEntityStorageClass().isInstance(blockStorage))
				continue;
			blockStorage.clearEntities();
			blockStorage.update(true, false);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "clear the "  + storageType.getCodeName() + " stored entities of " + blocks.toString(event, debug);
	}

}
