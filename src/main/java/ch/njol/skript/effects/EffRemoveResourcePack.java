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
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

@Name("Remove Resource Pack")
@Description({
	"Remove all resource packs or a resource pack by uuid sent by the server.",
	"You can only choose to remove all or by uuid, not both",
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
		Skript.registerEffect(EffRemoveResourcePack.class, "remove [:all] resource pack[s] [with [the] [uu]id %-string%] from %players%");
	}

	@Nullable
	private boolean modeAll, modeId;
	@UnknownNullability
	private Expression<String> id;
	@UnknownNullability
	private Expression<Player> recipients;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		modeAll = parseResult.hasTag("all");
		modeId = (!modeAll);
		if (modeAll && exprs[0] != null) {
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
		UUID uuid = null;
		if (modeId) {
			uuid = UUID.fromString(Utils.convertUUID(id.getSingle(event)));
			if (uuid == null) {
				return;
			}
		}
		for (Player player : recipients.getArray(event)) {
			if (modeAll) {
				player.removeResourcePacks();
			} else if (modeId) {
				player.removeResourcePack(uuid);
			}
		}
	}



	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (modeAll) {
			return "remove all resource packs from " + recipients.toString(event, debug);
		} else if (modeId) {
			return "remove resource pack with the uuid " + id.toString(event, debug) + " from " + recipients.toString(event, debug);
		}
		return "null";
	}

}
