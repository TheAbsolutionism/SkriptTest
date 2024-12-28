package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Entities")
@Description({
	"The entities stored inside an entity block storage (i.e. beehive).",
	"This does not return a list of all the entities stored inside the block. This can only return the number of entities stored.",
	"Adding entities into a block must be of the required type (i.e. a bee for beehive).",
	"Due to unstable behavior on older versions, adding entities to an entity block storage requires Minecraft version 1.21+.",
	"Requires a Paper server to clear the stored entities inside the block."
})
@Examples({
	"broadcast the stored entities of {_beehive}",
	"add last spawned bee to the entity block storage stored entities of {_beehive}",
	"clear the stored entities of {_beehive} # Require Paper"
})
@Since("INSERT VERSION")
public class ExprEntityStorageEntities extends SimplePropertyExpression<Block, Integer> {

	/*
		Minecraft versions 1.19.4 -> 1.20.6 have unstable behavior.
		Entity is either not added, or added but still exists.
		Releasing entities on these versions is also unstable.
		Either entities are not released or are released and not clearing the stored entities.
	 */

	private static final boolean SUPPORTS_CLEAR = Skript.methodExists(EntityBlockStorage.class, "clearEntities");
	private static final boolean RUNNING_1_21_0 = Skript.isRunningMinecraft(1, 21, 0);

	static {
		registerDefault(ExprEntityStorageEntities.class, Integer.class, "[entity block storage] stored entities", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof EntityBlockStorage<?> blockStorage)
			return blockStorage.getEntityCount();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD) {
			if (!RUNNING_1_21_0) {
				Skript.error("Adding entities requires Minecraft version 1.21+");
				return null;
			}
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
			if (mode == ChangeMode.DELETE) {
				blockStorage.clearEntities();
			} else if (mode == ChangeMode.ADD && entities != null) {
				addEntities(blockStorage.getClass(), Bee.class, blockStorage, entities);
			}
		}
	}

	private <T extends EntityBlockStorage<R>, R extends Entity> void addEntities(Class<T> entityStorageClass, Class<R> entityClass, EntityBlockStorage<?> blockStorage, Entity[] entities) {
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
	protected String getPropertyName() {
		return "entity block storage stored entities";
	}

}
