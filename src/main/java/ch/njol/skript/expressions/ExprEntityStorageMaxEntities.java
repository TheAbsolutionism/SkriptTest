package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
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
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Entity Storage Max Entities")
@Description({
	"The max number of entities an entity block storage can store (i.e. beehive).",
	"Using a specific block storage type will restrict the blocks provided to match the type."
})
@Examples("set the beehive storage max entities of {_beehive} to 100")
@Since("INSERT VERSION")
public class ExprEntityStorageMaxEntities extends PropertyExpression<Block, Integer> {

	// Future proofing for any EntityBlockStorage added later on

	private static final EntityBlockStorageType[] ENTITY_BLOCK_STORAGE_TYPES = EntityBlockStorageType.values();

	static {
		String[] patterns = new String[ENTITY_BLOCK_STORAGE_TYPES.length];
		for (EntityBlockStorageType type : ENTITY_BLOCK_STORAGE_TYPES) {
			patterns[type.ordinal()] = "[the] " + type.getCodeName() + " max entities [of %blocks%]";
		}
		Skript.registerExpression(ExprEntityStorageMaxEntities.class, Integer.class, ExpressionType.PROPERTY, patterns);
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
			return blockStorage.getMaxEntities();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int value = delta != null ? (int) delta[0] : 0;
		Consumer<EntityBlockStorage<?>> consumer = switch (mode) {
			case SET -> blockStorage -> blockStorage.setMaxEntities(Math2.fit(0, value, Integer.MAX_VALUE));
			case ADD -> blockStorage -> {
				int current = blockStorage.getMaxEntities();
				blockStorage.setMaxEntities(Math2.fit(0, current + value, Integer.MAX_VALUE));
			};
			case REMOVE -> blockStorage -> {
				int current = blockStorage.getMaxEntities();
				blockStorage.setMaxEntities(Math2.fit(0, current - value, Integer.MAX_VALUE));
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;
			if (!storageType.isSuperType() && !storageType.getEntityStorageClass().isInstance(blockStorage))
				continue;
			consumer.accept(blockStorage);
			blockStorage.update(true, false);
		}
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + storageType.getCodeName() + " max entities of " + getExpr().toString(event, debug);
	}

}
