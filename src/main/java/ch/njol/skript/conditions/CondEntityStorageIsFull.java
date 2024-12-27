package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityBlockStorageUtils.EntityBlockStorageType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Storage Is Full")
@Description({
	"Checks to see if an entity block storage is full (i.e beehive).",
	"Using a specific block storage type will restrict the blocks provided to match the type.",
	"Any blocks provided not matching the block storage type will fail the condition."
})
@Examples({
	"if the beehive storage of {_beehive} is full:",
		"release the beehive storage of {_beehive}"
})
@Since("INSERT VERSION")
public class CondEntityStorageIsFull extends Condition {

	// Future proofing for any EntityBlockStorage added later on

	private static final EntityBlockStorageType[] ENTITY_BLOCK_STORAGE_TYPES = EntityBlockStorageType.values();

	static {
		String[] patterns = new String[ENTITY_BLOCK_STORAGE_TYPES.length * 2];
		for (EntityBlockStorageType type : ENTITY_BLOCK_STORAGE_TYPES) {
			patterns[type.ordinal() * 2] = "[the] " + type.getCodeName() + " of %blocks% (is|are) full";
			patterns[(type.ordinal() * 2) + 1] = "[the] " + type.getCodeName() + " of %blocks% (isn't|is not|aren't|are not) full";
		}
		Skript.registerCondition(CondEntityStorageIsFull.class, ConditionType.PROPERTY, patterns);
	}

	private EntityBlockStorageType storageType;
	private Expression<Block> blocks;
	private boolean checkFull;

	@Override
	public boolean init(Expression<?>[] exrps, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		storageType = ENTITY_BLOCK_STORAGE_TYPES[matchedPattern / 2];
		checkFull = matchedPattern % 2 == 0;
		//noinspection unchecked
		blocks = (Expression<Block>) exrps[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		return blocks.check(event, block -> {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				return false;
			if (!storageType.isSuperType() && !storageType.getEntityStorageClass().isInstance(blockStorage))
				return false;
			return blockStorage.isFull() == checkFull;
		});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the", storageType.getCodeName(), "of", blocks);
		if (blocks.isSingle()) {
			builder.append("is");
		} else {
			builder.append("are");
		}
		if (!checkFull)
			builder.append("not");
		builder.append("full");
		return builder.toString();
	}

}
