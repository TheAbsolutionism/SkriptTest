package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityBlockStorageUtils.EntityBlockStorageType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Entity Count")
@Description({
	"Get the number of entities stored inside an entity block storage (i.e. beehive).",
	"Using a specific block storage type will restrict the blocks provided to match the type."
})
@Examples("broadcast the beehive storage entity count of {_beehive}")
@Since("INSERT VERSION")
public class ExprEntityStorageEntiyCount extends PropertyExpression<Block, Integer> {

	private static final EntityBlockStorageType[] ENTITY_BLOCK_STORAGE_TYPES = EntityBlockStorageType.values();

	static {
		String[] patterns = new String[ENTITY_BLOCK_STORAGE_TYPES.length];
		for (EntityBlockStorageType type : ENTITY_BLOCK_STORAGE_TYPES) {
			patterns[type.ordinal()] = "[the] " + type.getCodeName() + " entity (count|amount) [of %blocks%]";
		}
		Skript.registerExpression(ExprEntityStorageEntiyCount.class, Integer.class, ExpressionType.PROPERTY, patterns);
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
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + storageType.getCodeName() + " entity count of " + getExpr().toString(event, debug);
	}

}
