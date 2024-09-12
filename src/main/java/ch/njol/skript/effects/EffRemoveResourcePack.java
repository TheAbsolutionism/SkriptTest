package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Remove Resource Pack")
@Description({"Remove all resource packs or a resource pack by uuid sent by the server.",
	"",
	"You can allow up to 32 alphanumeric characters in the uuid."
})
@Examples({
	"remove all resource packs from all players",
	"remove resource pack with the uuid \"1\" from all players"
})
@Since("INSERT VERSION")
public class EffRemoveResourcePack extends Effect {

	static {
		Skript.registerEffect(EffRemoveResourcePack.class, "remove [:all] resource pack[s] [id:with [the] [uu]id %string%] from %players%");
	}

	@Nullable
	private boolean modeAll, modeId;
	@Nullable
	private Expression<String> id;
	@Nullable
	private Expression<Player> recipients;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		modeAll = parseResult.hasTag("all");
		modeId = parseResult.hasTag("id");
		if (modeAll && modeId) {
			Skript.error("You cannot remove all and by uuid");
			return false;
		}
		if (modeId) {
			id = (Expression<String>) exprs[0];
		}
		recipients = (Expression<Player>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		String uuid = null;
		if (modeId) {
			uuid = Utils.convertUUID(id.getSingle(event));
		}
		for (Player player : recipients.getArray(event)) {
			if (modeAll) {
				player.removeResourcePacks();
			} else if (modeId && uuid != null) {
				player.removeResourcePack(UUID.fromString(uuid));
			}
		}
	}



	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (modeAll) {
			return "remove all recource packs from " + recipients.toString(event, debug);
		} else if (modeId) {
			return "remove resource pack with the uuid " + id.toString(event, debug) + " from " + recipients.toString(event, debug);
		}
		return null;
	}

}
