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

@Name("Release Entity Storage")
@Description({
	"Release the entities stored in an entity block storage (i.e. beehive).",
	"Using a specific block storage type will restrict the blocks provided to match the type.",
	"Please note that releasing entities without `and clear` will effectively duplicate the entities."
})
@Examples({
	"release the beehive storage of {_beehive}",
	"release and clear the beehive storage stored entities of {_hive}"
})
@Since("INSERT VERSION")
public class EffReleaseEntityStorage extends Effect {

	// Future proofing for any EntityBlockStorage added later on

	private static final EntityBlockStorageType[] ENTITY_BLOCK_STORAGE_TYPES = EntityBlockStorageType.values();

	static {
		String[] patterns = new String[ENTITY_BLOCK_STORAGE_TYPES.length];
		for (EntityBlockStorageType type : ENTITY_BLOCK_STORAGE_TYPES) {
			patterns[type.ordinal()] = "release [clear:and (clear|empty)] [the] " + type.getName() + " [stored entities] of %blocks%";
		}
		Skript.registerEffect(EffReleaseEntityStorage.class, patterns);
	}

	private EntityBlockStorageType storageType;
	private Expression<Block> blocks;
	private boolean clear;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		storageType = ENTITY_BLOCK_STORAGE_TYPES[matchedPattern];
		//noinspection unchecked
		blocks = (Expression<Block>) exprs[0];
		clear = parseResult.hasTag("clear");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;
			if (!storageType.isSuperType() && !storageType.getEntityStorageClass().isInstance(blockStorage))
				continue;
			blockStorage.releaseEntities();
			if (clear)
				blockStorage.clearEntities();
			blockStorage.update(true, false);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "release the "  + storageType.getName() + " stored entities of " + blocks.toString(event, debug);
	}

}
