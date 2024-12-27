package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityBlockStorageUtils;
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
import org.bukkit.block.BlockState;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Insert")
@Description({
	"Add an entity to an entity block storage (i.e. beehive).",
	"The added entity must match the required type of the entity block storage (i.e. a bee).",
	"Using a specific block storage type will restrict the blocks provided to match the type."
})
@Examples("add last spawned bee into the beehive storage of {_beehive}")
@Since("INSERT VERSION")
public class EffInsertEntityStorage extends Effect {

	private static final EntityBlockStorageType[] ENTITY_BLOCK_STORAGE_TYPES = EntityBlockStorageType.values();

	static {
		String[] patterns = new String[ENTITY_BLOCK_STORAGE_TYPES.length];
		for (EntityBlockStorageType type : ENTITY_BLOCK_STORAGE_TYPES) {
			patterns[type.ordinal()] = "(add|insert) %livingentities% in[ ]to [the] " + type.getCodeName() + " of %blocks%";
		}
		Skript.registerEffect(EffInsertEntityStorage.class, patterns);
	}

	private Expression<? extends Entity> entities;
	private Expression<Block> blocks;
	private EntityBlockStorageType storageType;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		storageType = ENTITY_BLOCK_STORAGE_TYPES[matchedPattern];
		//noinspection unchecked
		entities = (Expression<? extends Entity>) exprs[0];
		//noinspection unchecked
		blocks = (Expression<Block>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Entity[] entities = this.entities.getArray(event);
		for (Block block : blocks.getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;

			if (storageType.isSuperType()) {
				EntityBlockStorageType newType = EntityBlockStorageUtils.getEntityBlockStorageType(blockStorage);
				if (newType == null)
					continue;
				addEntities(newType.getEntityStorageClass(), newType.getEntityClass(), blockStorage, entities);
			} else if (storageType.getEntityStorageClass().isInstance(block.getState())) {
				addEntities(storageType.getEntityStorageClass(), storageType.getEntityClass(), block.getState(), entities);
			}
		}
	}

	private <T extends EntityBlockStorage<R>, R extends Entity> void addEntities(Class<? extends EntityBlockStorage<?>> entityStorageClass, Class<R> entityClass, BlockState blockState, Entity[] entities) {
		//noinspection unchecked
		T blockStorage = (T) blockState;
		for (Entity entity : entities) {
			if (!entityClass.isInstance(entity))
				continue;
			if (blockStorage.getEntityCount() >= blockStorage.getMaxEntities())
				break;
			//noinspection unchecked
			R typedEntity = (R) entity;
			blockStorage.addEntity(typedEntity);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "add " + entities.toString(event, debug) + " into the " + storageType.getCodeName() + " of " + blocks.toString(event, debug);
	}

}
