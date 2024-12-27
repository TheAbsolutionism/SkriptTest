package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityBlockStorageUtils;
import ch.njol.skript.bukkitutil.EntityBlockStorageUtils.EntityBlockStorageType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Entities")
@Description({
	"The entities stored inside an entity block storage (i.e. beehive).",
	"This does not return a list of all the entities stored inside the block. This can only return the number of entities stored.",
	"Adding entities into a block must be of the required type (i.e. a bee for beehive).",
	"Requires a Paper server to clear the stored entities inside the block.",
	"Using a specific block storage type will restrict the blocks provided to match the type."
})
@Examples({
	"broadcast the beehive storage stored entities of {_beehive}",
	"add last spawned bee to the beehive storage entities of {_beehive}",
	"clear the beehive storage stored entities of {_beehive} # Require Paper"
})
@Since("INSERT VERSION")
public class ExprEntityStorageEntities extends PropertyExpression<Block, Integer> {

	// Future proofing for any EntityBlockStorage added later on

	private static final boolean SUPPORTS_CLEAR = Skript.methodExists(EntityBlockStorage.class, "clearEntities");
	private static final EntityBlockStorageType[] ENTITY_BLOCK_STORAGE_TYPES = EntityBlockStorageType.values();

	static {
		String[] patterns = new String[ENTITY_BLOCK_STORAGE_TYPES.length];
		for (EntityBlockStorageType type : ENTITY_BLOCK_STORAGE_TYPES) {
			patterns[type.ordinal()] = "[the] " + type.getName() + " [stored] entities [of %blocks%]";
		}
		Skript.registerExpression(ExprEntityStorageEntities.class, Integer.class, ExpressionType.PROPERTY, patterns);
	}

	private EntityBlockStorageType storageType;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		storageType = ENTITY_BLOCK_STORAGE_TYPES[matchedPattern];
		//noinspection unchecked
		setExpr((Expression<Block>) exprs[0]);
		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event, Block[] source) {
		return get(source, block -> {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				return null;
			if (!storageType.isSuperType() && !storageType.getEntityStorageClass().isInstance(blockStorage))
				return null;
			return blockStorage.getEntityCount();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD) {
			return CollectionUtils.array(LivingEntity[].class);
		} else if (mode == ChangeMode.DELETE) {
			if (!SUPPORTS_CLEAR) {
				Skript.error("You can only clear the entities of an entity block storage on a Paper server.");
				return null;
			}
			return CollectionUtils.array(LivingEntity.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		LivingEntity[] entities = delta != null ? (LivingEntity[]) delta : null;
		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;
			EntityBlockStorageType thisType = storageType;
			if (storageType.isSuperType()) {
				thisType = EntityBlockStorageUtils.getEntityBlockStorageType(blockStorage);
				if (thisType == null)
					continue;
			} else if (!storageType.getEntityStorageClass().isInstance(blockStorage))
				continue;
			if (mode == ChangeMode.DELETE) {
				blockStorage.clearEntities();
			} else if (mode == ChangeMode.ADD && entities != null) {
				addEntities(thisType.getEntityStorageClass(), thisType.getEntityClass(), blockStorage, entities);
			}
		}
	}

	private <T extends EntityBlockStorage<R>, R extends Entity> void addEntities(Class<? extends EntityBlockStorage<?>> entityStorageClass, Class<R> entityClass, EntityBlockStorage<?> blockStorage, Entity[] entities) {
		//noinspection unchecked
		T typedStorage = (T) blockStorage;
		for (Entity entity : entities) {
			if (!entityClass.isInstance(entity))
				continue;
			if (typedStorage.getEntityCount() >= typedStorage.getMaxEntities())
				break;
			//noinspection unchecked
			R typedEntity = (R) entity;
			typedStorage.addEntity(typedEntity);
		}
		typedStorage.update(true, false);
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + storageType.getName() + " stored entities of " + getExpr().toString(event, debug);
	}

}
