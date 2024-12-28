package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
	"add last spawned bee to the stored entities of {_beehive}",
	"clear the stored entities of {_beehive} # Requires Paper"
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
		registerDefault(ExprEntityStorageEntities.class, Integer.class, "[max:max[imum]] stored entities", "blocks");
	}

	private boolean withMax;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		withMax = parseResult.hasTag("max");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof EntityBlockStorage<?> blockStorage) {
			if (withMax)
				return blockStorage.getMaxEntities();
			return blockStorage.getEntityCount();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD -> {
				if (withMax) {
					yield CollectionUtils.array(Integer.class);
				} else if (!RUNNING_1_21_0)  {
					Skript.error("Adding entities requires Minecraft version 1.21+");
					yield null;
				}
				yield CollectionUtils.array(LivingEntity[].class);
			}
			case REMOVE, RESET, SET -> {
				if (withMax)
					yield CollectionUtils.array(Integer.class);
				yield null;
			}
			case DELETE -> {
				if (withMax)
					yield null;
				if (!SUPPORTS_CLEAR) {
					Skript.error("You can only clear the entities of an entity block storage on a Paper server.");
					yield null;
				}
				yield CollectionUtils.array(LivingEntity.class);
			}
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		LivingEntity[] entities = null;
		int value = 0;
		if (delta != null) {
			if (withMax) {
                value = (int) delta[0];
			} else {
				entities = (LivingEntity[]) delta;
			}
		}
		LivingEntity[] finalEntities = entities;
		int finalValue = value;
		Consumer<EntityBlockStorage<?>> consumer = null;
		if (withMax) {
			consumer = switch (mode) {
				case SET -> blockStorage -> blockStorage.setMaxEntities(Math2.fit(0, finalValue, Integer.MAX_VALUE));
				case ADD -> blockStorage -> {
					int current = blockStorage.getMaxEntities();
					blockStorage.setMaxEntities(Math2.fit(0, current + finalValue, Integer.MAX_VALUE));
				};
				case REMOVE -> blockStorage -> {
					int current = blockStorage.getMaxEntities();
					blockStorage.setMaxEntities(Math2.fit(0, current - finalValue, Integer.MAX_VALUE));
				};
				case RESET -> blockStorage -> blockStorage.setMaxEntities(3);
				default -> throw new IllegalStateException("Unexpected value: " + mode);
			};
		} else {
			consumer = switch (mode) {
				case DELETE -> EntityBlockStorage::clearEntities;
				case ADD -> {
					assert finalEntities != null;
					yield blockStorage -> {
						//noinspection unchecked
						addEntities(blockStorage.getClass(), Bee.class, blockStorage, finalEntities);
					};
				}
				default -> throw new IllegalStateException("Unexpected value: " + mode);
			};
		}

		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;
			consumer.accept(blockStorage);
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
		return (withMax ? "maximum " : "") + "stored entities";
	}

}
