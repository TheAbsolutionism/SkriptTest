package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Entity Storage Max Entities")
@Description("The max number of entities an entity block storage (i.e. beehive) can store.")
@Examples("set the entity blockstorage max entities of {_beehive} to 100")
@Since("INSERT VERSION")
public class ExprEntityStorageMaxEntities extends SimplePropertyExpression<Block, Integer> {

	static {
		registerDefault(ExprEntityStorageMaxEntities.class, Integer.class, "[entity block storage] max entities", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof EntityBlockStorage<?> blockStorage)
			return blockStorage.getMaxEntities();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE, RESET -> CollectionUtils.array(Integer.class);
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
			case RESET -> blockStorage -> blockStorage.setMaxEntities(3);
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
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
	protected String getPropertyName() {
		return "entity block storage max entities";
	}

}
